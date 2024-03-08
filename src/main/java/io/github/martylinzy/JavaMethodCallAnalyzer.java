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
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

/**
 * 遍历指定Java项目的所有Java文件，解析每个文件中的方法和方法调用关系。
 * 将文件与方法的映射关系存储在fileMethodMap中，将方法调用关系存储在methodCallMap中。
 * 打印文件-方法映射关系和方法调用关系。
 * 生成方法调用图，将调用关系可视化展示。
 */
public class JavaMethodCallAnalyzer {

    // 存储文件与方法的映射关系
    private Map<String, List<MethodInfo>> fileMethodMap = new HashMap<>();
    // 存储方法调用的映射关系
    private Map<String, List<MethodCallInfo>> methodCallMap = new HashMap<>();

    /**
     * 分析整个Java项目
     * @param projectPath 项目路径
     */
    public void analyzeProject(String projectPath) {
        // 创建符号解析器
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        typeSolver.add(new JavaParserTypeSolver(new File(projectPath)));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);

        analyzeDirectory(new File(projectPath));
        printFileMethodMap();
        printMethodCallMap();
        generateCallGraph(projectPath);
    }

    /**
     * 递归分析目录下的Java文件
     * @param dir 目录
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
     * 分析单个Java文件
     * @param file Java文件
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
                        System.err.println("Error resolving method call: " + methodCall.toString());
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
    private void printFileMethodMap() {
        System.out.println("Methods by File:");
        fileMethodMap.forEach((fileName, methodInfos) -> {
            System.out.println(fileName + ":");
            methodInfos.forEach(methodInfo ->
                    System.out.println("  " + methodInfo.className + "." + methodInfo.methodName)
            );
        });
    }
    /**
     * 打印方法调用映射
     */
    private void printMethodCallMap() {
        System.out.println("\nMethod Call Relations:");
        methodCallMap.forEach((caller, callInfos) -> {
            System.out.println(caller + " calls:");
            callInfos.forEach(callInfo -> System.out.println("  " +
                    callInfo.packageName + "." + callInfo.className + "." + callInfo.methodName));
        });
    }
    /**
     * 生成方法调用图
     * @param projectPath 项目路径
     */
    private void generateCallGraph(String projectPath) {
        MutableGraph graph = mutGraph("Method Call Graph").setDirected(true);
        Map<String, MutableNode> nodeMap = new HashMap<>();

        methodCallMap.forEach((caller, callInfos) -> {
            MutableNode callerNode = nodeMap.computeIfAbsent(caller, k -> mutNode(caller).add(Color.BLACK, Style.FILLED));

            callInfos.forEach(callInfo -> {
                String callee = callInfo.packageName + "." + callInfo.className + "." + callInfo.methodName;
                MutableNode calleeNode = nodeMap.computeIfAbsent(callee, k -> mutNode(callee));
                graph.add(callerNode.addLink(calleeNode));
            });
        });

        String outputPath = projectPath + "/method_call_graph.png";
        try {
            Graphviz.fromGraph(graph).width(1200).render(Format.PNG).toFile(new File(outputPath));
            System.out.println("\nMethod call graph generated at: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error generating method call graph: " + e.getMessage());
        }
    }
    /**
     * 方法信息类
     */
    private static class MethodInfo {
        String className;
        String methodName;

        MethodInfo(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }
    }
    /**
     * 方法调用信息类
     */
    private static class MethodCallInfo {
        String className;
        String methodName;
        String packageName;

        MethodCallInfo(String className, String methodName, String packageName) {
            this.className = className;
            this.methodName = methodName;
            this.packageName = packageName;
        }
    }
    public static void main(String[] args) {
        String projectPath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";
        JavaMethodCallAnalyzer analyzer = new JavaMethodCallAnalyzer();
        analyzer.analyzeProject(projectPath);
    }
}