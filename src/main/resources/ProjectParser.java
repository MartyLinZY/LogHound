package io.github.martylinzy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

public class ProjectParser
{
    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        ProjectParser projectParser = new ProjectParser();
        File file = projectParser.getFile("test.java");
        //FileInputStream in = new FileInputStream("test.java");

        // Parse the file
        CompilationUnit cu = JavaParser.parse(file);
        //CompilationUnit cu = JavaParser.parse(in);

        // Visit and print method names
        cu.accept(new MethodVisitor(), null);
    }

    private File getFile(String filename) throws URISyntaxException {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filename).toURI());
    }

    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            // We are accessing the attributes of the method here
            // Will be called for all methods in the CompilationUnit, including inner class methods
            System.out.println(n.getName());
            super.visit(n, arg);
        }
    }
}