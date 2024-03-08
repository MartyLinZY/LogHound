package io.github.martylinzy;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;

public class MethodCallGraphBuilder {

    private HashMap<String, HashSet<String>> callGraph = new HashMap<>();

    public void analyzeJavaFile(String filePath) {
        try {
            // 解析Java文件
            FileInputStream in = new FileInputStream(filePath);
            CompilationUnit cu = JavaParser.parse(in);

            // 配置符号解析
            CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
            combinedSolver.add(new ReflectionTypeSolver());
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
            cu.setData(Node.SYMBOL_RESOLVER_KEY, symbolSolver);

            // 访问AST并提取方法调用
            MethodCallExtractor extractor = new MethodCallExtractor();
            extractor.visit(cu, null);

            // 更新调用图
            updateCallGraph(extractor.getMethodCalls());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCallGraph(HashMap<String, HashSet<String>> methodCalls) {
        methodCalls.forEach((caller, callees) -> {
            if (!callGraph.containsKey(caller)) {
                callGraph.put(caller, new HashSet<>());
            }
            callGraph.get(caller).addAll(callees);
        });
    }

    public class MethodCallExtractor extends VoidVisitorAdapter<Void> {
        private HashMap<String, HashSet<String>> methodCalls = new HashMap<>();

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            super.visit(n, arg);
            String methodName = n.getNameAsString();
            methodCalls.putIfAbsent(methodName, new HashSet<>());
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            n.resolve().getQualifiedSignature(); // 获取调用的方法签名
            String callerMethod = n.findAncestor(MethodDeclaration.class).map(MethodDeclaration::getNameAsString).orElse("Unknown");
            String calleeMethod = n.getNameAsString();
            methodCalls.getOrDefault(callerMethod, new HashSet<>()).add(calleeMethod);
        }

        public HashMap<String, HashSet<String>> getMethodCalls() {
            return methodCalls;
        }
    }

    public static void main(String[] args) {
        MethodCallGraphBuilder builder = new MethodCallGraphBuilder();
        builder.analyzeJavaFile("Path/To/Your/JavaFile.java");

        // 打印调用图
        builder.callGraph.forEach((key, value) -> System.out.println(key + " calls " + value));
    }
}
