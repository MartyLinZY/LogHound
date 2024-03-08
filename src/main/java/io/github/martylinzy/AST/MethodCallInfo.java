package io.github.martylinzy.AST;

public class MethodCallInfo {
    String className;
    String methodName;
    String packageName;

    MethodCallInfo(String className, String methodName, String packageName) {
        this.className = className;
        this.methodName = methodName;
        this.packageName = packageName;
    }
}
