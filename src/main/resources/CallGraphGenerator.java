package io.github.martylinzy.demo;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import io.github.martylinzy.App;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;

public class CallGraphGenerator {

    private static final HashMap<String, HashSet<String>> callGraph = new HashMap<>();
    private static final Logger logger = Logger.getLogger(CallGraphGenerator.class);
    public static void main(String[] args) {
        logger.info("Initialize......");
        String projectRoot = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/"; // Change this to your project path
        logger.info("Project root is "+projectRoot);
        File projectDir = new File(projectRoot);
        processDirectory(projectDir);
        printCallGraph();
    }

    private static void processDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    processDirectory(file);
                } else if (file.getName().endsWith(".java")) {
                    processJavaFile(file);
                }
            }
        }
    }

    private static void processJavaFile(File file) {
        try {
            FileInputStream in = new FileInputStream(file);
            CompilationUnit cu = JavaParser.parse(in);

            // Configure JavaParser to solve symbols (types, methods, etc.)
            CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
            combinedSolver.add(new ReflectionTypeSolver());
            JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
            cu.setData(com.github.javaparser.ast.Node.SYMBOL_RESOLVER_KEY, symbolSolver);

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration n, Void arg) {
                    super.visit(n, arg);
                    callGraph.putIfAbsent(n.getNameAsString(), new HashSet<>());
                }

                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    super.visit(n, arg);
                    n.findAncestor(MethodDeclaration.class).ifPresent(caller -> {
                        String callerName = caller.getNameAsString();
                        String calleeName = n.getNameAsString();
                        callGraph.computeIfAbsent(callerName, k -> new HashSet<>()).add(calleeName);
                    });
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printCallGraph() {
        callGraph.forEach((caller, callees) -> {
            System.out.println(caller + " calls: " + callees);
        });
    }
}
