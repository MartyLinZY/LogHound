package io.github.martylinzy.AST;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.FileInputStream;
public class MethodVisitor extends VoidVisitorAdapter<Void> {
    private int level = 0; // 用于表示当前节点在树中的层次

    @Override
    public void visit(MethodDeclaration n, Void arg) {
        // 打印缩进和方法签名
        String indent = " ".repeat(level * 2); // 创建一个由空格组成的字符串，长度根据层级而定
        System.out.println(indent + "Method: " + n.getSignature());

        level++; // 增加层次深度
        super.visit(n, arg); // 访问子节点
        level--; // 回退层次深度
    }
}

 class ASTPrinter {
    public static void main(String[] args) {
        try {
            // 替换为你的 Java 文件路径
            FileInputStream in = new FileInputStream("src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/Server.java");

            // 解析文件
            CompilationUnit cu = JavaParser.parse(in);

            // 使用自定义访问者遍历方法
            MethodVisitor methodVisitor = new MethodVisitor();
            methodVisitor.visit(cu, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
