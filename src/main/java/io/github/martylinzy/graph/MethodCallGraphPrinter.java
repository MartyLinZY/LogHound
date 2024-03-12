package io.github.martylinzy.graph;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

import io.github.martylinzy.AST.MethodCallInfo;
import io.github.martylinzy.AST.MethodInfo;
public class MethodCallGraphPrinter {

    // Store the mapping of files to methods
    private Map<String, List<MethodInfo>> fileMethodMap = new HashMap<>();
    // Store the mapping relationships for method calls
    private Map<String, List<MethodCallInfo>> methodCallMap = new HashMap<>();
    public MethodCallGraphPrinter(Map<String, List<MethodInfo>> fileMethodMap, Map<String, List<MethodCallInfo>> methodCallMap){
        this.fileMethodMap=fileMethodMap;
        this.methodCallMap=methodCallMap;
    }
    public void printFileMethodMap() {
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
    public void printMethodCallMap() {
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
    public void generateCallGraph(String projectPath) {
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

}
