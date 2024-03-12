package io.github.martylinzy;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.github.martylinzy.AST.MethodCallInfo;
import io.github.martylinzy.AST.MethodInfo;
import io.github.martylinzy.graph.MethodCallGraphPrinter;

import org.slf4j.*;
import java.io.File;
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Iterate through all the Java files of a given Java project.
 * Parse the methods and method call relationships in each file.
 * Store the mapping relationship between files and methods in fileMethodMap and the method call relationship in methodCallMap.
 * Print mapping relationship between files-methods and the method call
 * Generate Method call graph and give visualization presentation.*/
public class JavaMethodCallAnalyzer {
    Logger log=LoggerFactory.getLogger(JavaMethodCallAnalyzer.class);
    // Store the mapping of files to methods
    private Map<String, List<MethodInfo>> fileMethodMap = new HashMap<>();
    // Store the mapping relationships for method calls
    private Map<String, List<MethodCallInfo>> methodCallMap = new HashMap<>();

    /**
     * Analyse whole java project
     * @param projectPath project path
     */
    public void analyzeProject(String projectPath) {
        // build symbol solver for project

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(new File(projectPath)));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        // workflow
        analyzeDirectory(new File(projectPath));
        printFileMethodMap();

        //print maps

        for (Map.Entry<String, List<MethodCallInfo>> entry : methodCallMap.entrySet()) {
            List<MethodCallInfo> methodCallList = entry.getValue();
            // 使用 Set 去重，并筛选以 "org.apache.hadoop." 开头的元素
            Set<MethodCallInfo> filteredMethodCallSet = methodCallList.stream()
                    .filter(methodCallInfo -> methodCallInfo.packageName.startsWith("org.apache.hadoop."))
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // 将筛选和去重后的结果转换回 List，并更新 Map 中的值
            entry.setValue(new ArrayList<>(filteredMethodCallSet));
        }

        MethodCallGraphPrinter graphprinter=new MethodCallGraphPrinter(fileMethodMap,methodCallMap);
        graphprinter.printFileMethodMap();
        graphprinter.printMethodCallMap();
//        graphprinter.generateCallGraph(projectPath);
    }

    /**
     * Iterate through files under the path
     * @param dir Directory
     */
    private void analyzeDirectory(File dir) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                analyzeDirectory(file);
            } else if (file.getName().endsWith(".java")) {
                analyzeJavaFile(file);
            }
        }
    }

    /**
     * Analyse single Java file
     * @param file Java file path
     */
    private void analyzeJavaFile(File file) {
        try {
            CompilationUnit cu = StaticJavaParser.parse(file);
            // 遍历文件中的所有方法
            cu.findAll(MethodDeclaration.class).forEach(method -> {
                String className = method.findAncestor(ClassOrInterfaceDeclaration.class).get().getNameAsString();
                String methodName = method.getNameAsString();
                String fileName = file.getPath();
                addMethodToFile(fileName, new MethodInfo(className, methodName));

                // 将方法信息添加到文件-方法映射中
                method.findAll(MethodCallExpr.class).forEach(methodCall -> {
                    try {
                        ResolvedMethodDeclaration resolvedMethod = methodCall.resolve();
                        String calledClassName = resolvedMethod.getClassName();
                        String calledMethodName = resolvedMethod.getName();
                        String calledPackageName = resolvedMethod.getPackageName();
                        // 将方法调用信息添加到方法调用映射中
                        addMethodCall(className + "." + methodName,
                                new MethodCallInfo(calledClassName, calledMethodName, calledPackageName));
                    } catch (Exception e) {
                        //System.err.println("Error resolving method call: " + methodCall.toString());
                        log.error(e.getLocalizedMessage());
                    }
                });
            });
        } catch (Exception e) {
            System.err.println("Error parsing " + file.getPath() + ": " + e.getMessage());
        }
    }

    /**
     * 添加方法到文件-方法映射
     * @param fileName 文件名
     * @param methodInfo 方法信息
     */
    private void addMethodToFile(String fileName, MethodInfo methodInfo) {
        if (!fileMethodMap.containsKey(fileName)) {
            fileMethodMap.put(fileName, new ArrayList<>());
        }
        fileMethodMap.get(fileName).add(methodInfo);
    }
    /**
     * 添加方法调用到方法调用映射
     * @param caller 调用方法
     * @param callInfo 被调用方法信息
     */
    private void addMethodCall(String caller, MethodCallInfo callInfo) {
        if (!methodCallMap.containsKey(caller)) {
            methodCallMap.put(caller, new ArrayList<>());
        }
        methodCallMap.get(caller).add(callInfo);
    }
    /**
     * 打印文件-方法映射
     */
//    private void printFileMethodMap() {
//        System.out.println("Methods by File:");
//        String projectPath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";
//        fileMethodMap.forEach((fileName, methodInfos) -> {
//            System.out.println( fileName.replace(projectPath,"") + ":");
//            methodInfos.forEach(methodInfo ->
//                    System.out.println("  " + methodInfo.className + "." + methodInfo.methodName)
//            );
//        });
//    }
    private void printFileMethodMap() {
        String outputFileName = "method_map.txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFileName))) {
            writer.println("Methods by File:");

            String projectPath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";

            fileMethodMap.forEach((fileName, methodInfos) -> {
                writer.println(fileName.replace(projectPath, "") + ":");

                methodInfos.forEach(methodInfo ->
                        writer.println(" " + methodInfo.className + "." + methodInfo.methodName)
                );
            });

            System.out.println("Method map written to " + outputFileName);
        } catch (IOException e) {
            System.err.println("Error writing method map to file: " + e.getMessage());
        }
    }
    public static void main(String[] args) {

        String projectPath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";
        JavaMethodCallAnalyzer analyzer = new JavaMethodCallAnalyzer();
        analyzer.analyzeProject(projectPath);
    }
}