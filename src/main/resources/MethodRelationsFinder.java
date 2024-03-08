package io.github.martylinzy;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Paths;
import java.nio.file.Path;
public class MethodRelationsFinder {

    private static Map<String, List<String>> methodCalls = new HashMap<>();
    private static final Logger logger = Logger.getLogger(MethodRelationsFinder.class);
    public static void main(String[] args) throws Exception {
        String path="src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";
        Path basePath = Paths.get(path);
        logger.info("Path: "+path);
        List<File> javaFiles = new ArrayList<>();
        findJavaFiles(basePath.toFile(), javaFiles); // 使用 basePath 转换的 File 对象

        ParserConfiguration configuration = new ParserConfiguration();
        JavaParser javaParser = new JavaParser(configuration);

        for (File file : javaFiles) {

            FileInputStream inputStream = new FileInputStream(file);
            CompilationUnit cu = javaParser.parse(inputStream);
            Path relativePath = basePath.relativize(file.toPath());
            String filePath = relativePath.toString(); // 使用相对路径
            logger.info("Filename: "+ filePath);
            cu.accept(new MethodDeclarationVisitor(), filePath);
        }

        // 打印方法调用关系
        methodCalls.forEach((methodSignature, calls) -> System.out.println(methodSignature + " calls: " + calls));
    }

    private static void findJavaFiles(File directory, List<File> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFiles(file, javaFiles);
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
    }

    private static class MethodDeclarationVisitor extends VoidVisitorAdapter<String> {
        @Override
        public void visit(MethodDeclaration n, String filePath) {
            super.visit(n, filePath);
            String methodSignature = n.getSignature().asString() + " in " + filePath;
            MethodCallVisitor methodCallVisitor = new MethodCallVisitor();
            n.accept(methodCallVisitor, methodSignature);
            n.findAncestor(CompilationUnit.class).ifPresent(cu -> {
                cu.getPackageDeclaration().ifPresent(pkg -> {
                    System.out.println("Method Name: " + n.getName());
                    System.out.println("Package: " + pkg.getName().asString());
                });
            });
        }
    }

    private static class MethodCallVisitor extends VoidVisitorAdapter<String> {
        @Override
        public void visit(MethodCallExpr n, String parentMethodSignature) {
            super.visit(n, parentMethodSignature);
            // 记录调用的方法名和参数（尽可能记录参数类型会更准确，但这里仅用参数个数简化展示）
            String callSignature = n.getNameAsString()+"("+n.getArguments().size()+")";
            methodCalls.computeIfAbsent(parentMethodSignature, k -> new ArrayList<>()).add(callSignature);


        }
    }
}
//
//import com.github.javaparser.JavaParser;
//import com.github.javaparser.ParserConfiguration;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.expr.MethodCallExpr;
//import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class MethodRelationsFinder {
//
//    private static Map<String, List<String>> methodCalls = new HashMap<>();
//
//    public static void main(String[] args) throws Exception {
//        String projectPath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";
//        List<File> javaFiles = new ArrayList<>();
//        findJavaFiles(new File(projectPath), javaFiles);
//
//        ParserConfiguration configuration = new ParserConfiguration();
//        JavaParser javaParser = new JavaParser(configuration);
//
//        for (File file : javaFiles) {
//            FileInputStream inputStream = new FileInputStream(file);
//            CompilationUnit cu = javaParser.parse(inputStream);
//            cu.accept(new MethodDeclarationVisitor(), null);
//        }
//
//        // 打印方法调用关系
//        methodCalls.forEach((method, calls) -> System.out.println(method + " call " + calls));
//    }
//
//    private static void findJavaFiles(File directory, List<File> javaFiles) {
//        File[] files = directory.listFiles();
//        if (files != null) {
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    findJavaFiles(file, javaFiles);
//                } else if (file.getName().endsWith(".java")) {
//                    javaFiles.add(file);
//                }
//            }
//        }
//    }
//
//    private static class MethodDeclarationVisitor extends VoidVisitorAdapter<Void> {
//        @Override
//        public void visit(MethodDeclaration n, Void arg) {
//            super.visit(n, arg);
//            // 构建方法签名，包括方法名和参数类型
//            String methodName = n.getNameAsString();
//            String signature = methodName + n.getParameters().toString();
//            MethodCallVisitor methodCallVisitor = new MethodCallVisitor();
//            n.accept(methodCallVisitor, signature);
//        }
//    }
//
//    private static class MethodCallVisitor extends VoidVisitorAdapter<String> {
//        @Override
//        public void visit(MethodCallExpr n, String parentMethodSignature) {
//            super.visit(n, parentMethodSignature);
//            // 记录调用的方法名和参数（尽可能记录参数类型会更准确，但这里仅用参数个数简化展示）
//            String callSignature = n.getNameAsString() + n.getArguments().size();
//            methodCalls.computeIfAbsent(parentMethodSignature, k -> new ArrayList<>()).add(callSignature);
//        }
//    }
//}
