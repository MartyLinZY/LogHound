package io.github.martylinzy.graph;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.*;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.ext.JGraphXAdapter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    public void generateCallGraphViaGraphviz(String projectPath) {
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
            AbstractGraphvizEngine cmdlnEngine = new  GraphvizCmdLineEngine().timeout(2,TimeUnit.MINUTES);
            Graphviz.fromGraph(graph).width(1200).render(Format.PNG).toFile(new File(outputPath));
            System.out.println("\nMethod call graph generated at: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error generating method call graph: " + e.getMessage());
        }
    }
    private void generateCallGraphViaJGraphT(String projectPath) {
        Graph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        methodCallMap.forEach((caller, callInfos) -> {
            graph.addVertex(caller);

            callInfos.forEach(callInfo -> {
                String callee = callInfo.packageName + "." + callInfo.className + "." + callInfo.methodName;
                graph.addVertex(callee);
                graph.addEdge(caller, callee);
            });
        });

        String outputPath = projectPath + "/method_call_graph.dot";
//        try {
//            DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>();
//            exporter.setVertexAttributeProvider((v) -> {
//                Map<String, Attribute> attrs = new HashMap<>();
//                attrs.put("label", DefaultAttribute.createAttribute(v));
//                return attrs;
//            });
//
//            Writer writer = new StringWriter();
//            exporter.exportGraph(graph, writer);
//            String dot = writer.toString();
//
//            File file = new File(outputPath);
//            Files.writeString(file.toPath(), dot);
//
//            System.out.println("\nMethod call graph generated at: " + outputPath);
//        } catch (IOException e) {
//            System.err.println("Error generating method call graph: " + e.getMessage());
//        }
    }

    public void generateCallGraph(String projectPath) {

        DefaultDirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

        methodCallMap.forEach((caller, callInfos) -> {
            graph.addVertex(caller);

            callInfos.forEach(callInfo -> {
                String callee = callInfo.packageName + "." + callInfo.className + "." + callInfo.methodName;
                graph.addVertex(callee);
                graph.addEdge(caller, callee);
            });
        });

        JGraphXAdapter<String, DefaultEdge> adapter = new JGraphXAdapter<>(graph);
        mxGraphComponent graphComponent = new mxGraphComponent(adapter);
        mxGraph mxGraph = graphComponent.getGraph();

        mxHierarchicalLayout layout = new mxHierarchicalLayout(mxGraph);
        layout.setInterHierarchySpacing(50);
        layout.setInterRankCellSpacing(50);
        layout.setIntraCellSpacing(30);
        layout.execute(mxGraph.getDefaultParent());
        System.out.println("\nStart Generate graph...");
        BufferedImage image = mxGraphToImage(mxGraph);

        String outputPath = projectPath + "/method_call_graph.png";
        try {
            File outputFile = new File(outputPath);
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("\nMethod call graph generated at: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error generating method call graph: " + e.getMessage());
        }
    }

    private BufferedImage mxGraphToImage(mxGraph graph) {
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        Dimension dimension = graphComponent.getPreferredSize();

        BufferedImage image = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphComponent.paint(graphics);
        graphics.dispose();

        return image;
    }
}
