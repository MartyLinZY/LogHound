package io.github.martylinzy.AST;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.*;
import java.nio.file.Paths;

public class AST {
    public static void main(String[] args) {
        // 指定要解析的文件路径
        String filePath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/ipc/Server.java";

        try {
            // 解析文件
            CompilationUnit cu = JavaParser.parse(Paths.get(filePath));

            // 打印整个AST
            System.out.println(cu);

            // 也可以使用更详细的方式打印AST
            new MyVisitor().visit(cu, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class MyVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // 这里可以自定义访问逻辑，例如只打印方法声明
            System.out.println("Method Name: " + n.getName());
            super.visit(n, arg);
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            // 打印类或接口声明
            System.out.println("Class/Interface Name: " + n.getName());
            super.visit(n, arg);
        }
    }
}
