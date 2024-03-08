package io.github.martylinzy;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;

public class CallGraphBuilder {

    private static final HashMap<String, HashSet<String>> callGraph = new HashMap<>();
    private static final Logger logger = Logger.getLogger(CallGraphBuilder.class);
    public static void main(String[] args) {
        try {
            String filePath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/Server.java"; // 更改为你的Java文件路径
            CompilationUnit cu = JavaParser.parse(new FileInputStream(filePath));

            cu.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(MethodDeclaration n, Void arg) {
                    super.visit(n, arg);
                    // 为每个方法声明初始化调用列表
                    callGraph.putIfAbsent(n.getNameAsString(), new HashSet<>());

                }

                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    super.visit(n, arg);
                    // 获取当前方法调用的所在方法
                    n.findAncestor(MethodDeclaration.class).ifPresent(caller -> {
                        String callerName = caller.getNameAsString();
                        String calleeName = n.getNameAsString();
                        // 根据解析的caller和callee更新调用图
                        callGraph.computeIfAbsent(callerName, k -> new HashSet<>()).add(calleeName);
                    });
                }
            }, null);
            logger.info("Print the reference graph.......");
            // 打印调用图
            callGraph.forEach((method, calls) -> {
                if (!calls.isEmpty()) {
                    System.out.println("["+method + "] calls " + calls);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

