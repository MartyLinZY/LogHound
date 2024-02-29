package io.github.martylinzy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CallGraphVisualizer {

    public static void visualizeToDotFormat(Map<String, HashSet<String>> callGraph) {
        StringBuilder dotGraph = new StringBuilder("digraph CallGraph {\n");

        // 遍历调用图，为每个调用关系生成DOT格式的边
        for (Map.Entry<String, HashSet<String>> entry : callGraph.entrySet()) {
            String caller = entry.getKey();
            HashSet<String> callees = entry.getValue();
            for (String callee : callees) {
                dotGraph.append(String.format("    \"%s\" -> \"%s\";\n", caller, callee));
            }
        }

        dotGraph.append("}\n");

        // 打印调用图
        System.out.println(dotGraph.toString());
    }

    public static void main(String[] args) {
        CallGraphVisualizer visualizer=new CallGraphVisualizer();
        System.out.println("todo");
    }
}
