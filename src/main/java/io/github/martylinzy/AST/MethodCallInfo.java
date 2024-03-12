package io.github.martylinzy.AST;
import java.util.Objects;
public class MethodCallInfo {
    public String className;
    public String methodName;
    public String packageName;

    public MethodCallInfo(String className, String methodName, String packageName) {
        this.className = className;
        this.methodName = methodName;
        this.packageName = packageName;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodCallInfo that = (MethodCallInfo) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, packageName);
    }
}
