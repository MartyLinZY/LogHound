package io.github.martylinzy;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.text.NumberFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Objects;
import java.util.*;
import java.util.Arrays;
import com.github.javaparser.ast.expr.*;
public class HadoopLogStatementMatcher {
    public static void main(String[] args) throws IOException {
        // 设置 Hadoop 项目的根目录路径
        String projectRoot = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";

        // 创建 JavaSymbolSolver 用于解析项目依赖
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(new File(projectRoot)));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        // 获取所有的 Java 源文件
        List<File> javaFiles = Files.walk(Paths.get(projectRoot))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        // 存储日志打印语句的列表
        List<String> logStatements = javaFiles.stream()
                .flatMap(file -> {
                    List<String> statements = extractLogStatements(file);
                    if (statements.isEmpty()) {
                        System.out.println("No log statements found in file: " + file.getPath());
                    }
                    return statements.stream();
                })
                .collect(Collectors.toList());
        String logDirectory = "src/main/resources/application_log";
        List<File> logFiles = Files.walk(Paths.get(logDirectory))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
        float total_correct=0;
        if (!logStatements.isEmpty()) {
            for (File logFile : logFiles) {
                //List<String> logRecords = Files.readAllLines(Paths.get("src/main/resources/test.log"));
                List<String> logRecords = Files.readAllLines(logFile.toPath());
                int counter=0;
                System.out.println("Matching log statements in file: " + logFile.getName());
                for (String logRecord : logRecords) {
                    for (String logStatement : logStatements) {
                        if (logRecord.contains(logStatement)) {
//                            System.out.println("Matched log statement: " + logStatement);
//                            System.out.println("Log record: " + logRecord);
//                            System.out.println("--------------------");
                            counter++;
                            break;

                        }
                    }
                }
                System.out.println("Record number: "+logRecords.size());
                System.out.println("Match number: "+counter+", Rate: "+counter/(float)logRecords.size());
                total_correct+=counter/(float)logRecords.size();
            }
        } else {
            System.out.println("No log statements found in the project.");
        }
        System.out.println("Average: "+total_correct/(float)logFiles.size());

    }

    // 从 Java 文件中提取日志打印语句
    private static List<String> extractLogStatements(File file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            List<MethodCallExpr> methodCalls = cu.findAll(MethodCallExpr.class);
            String logKeywordsRegex = ".*\\b(LOG|log|LOGGER|logger|trace|debug|info|warn|error|fatal)\\b.*";
            return methodCalls.stream()
                    .filter(call -> {
                        Optional<Expression> scope = call.getScope();
                        return scope.map(expr -> expr.toString().matches(logKeywordsRegex)).orElse(false);
                    })
                    .map(HadoopLogStatementMatcher::extractLogMessage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private static String extractLogMessage(MethodCallExpr call) {
        String[] logLevels = {"trace", "debug", "info", "warn", "error", "fatal"};
        String methodName = call.getNameAsString().toLowerCase();

        if (Arrays.asList(logLevels).contains(methodName)) {
            return call.getArgument(0).toString();
        }

        return null;
    }
    public static String formatToPercentage(double number) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMinimumFractionDigits(2);
        return percentFormat.format(number / 100);
    }
}