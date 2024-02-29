package io.github.martylinzy;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class JavaFileParser {

    private final Map<String, HashSet<String>> callGraph = new HashMap<>();

    public JavaFileParser() {
        // Configure JavaParser to solve symbols (types, methods, etc.)
        CombinedTypeSolver combinedSolver = new CombinedTypeSolver(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
    }

    public Map<String, HashSet<String>> parseFile(String filePath) {
        try {
            CompilationUnit cu = JavaParser.parse(new FileInputStream(filePath));

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration n, Void arg) {
                    super.visit(n, arg);
                    // Ensure a method entry is created in the call graph, even if it calls no methods
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

        return callGraph;
    }
}
