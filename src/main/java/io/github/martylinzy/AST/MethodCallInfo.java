package io.github.martylinzy.AST;

public class MethodCallInfo {
    public String className;
    public String methodName;
    public String packageName;

    public MethodCallInfo(String className, String methodName, String packageName) {
        this.className = className;
        this.methodName = methodName;
        this.packageName = packageName;
    }
}
