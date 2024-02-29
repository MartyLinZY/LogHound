package io.github.martylinzy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectExplorer {

    private final List<String> javaFiles = new ArrayList<>();

    /**
     * 遍历指定的目录以寻找所有Java文件。
     *
     * @param directoryPath 要遍历的目录路径
     */
    public void explore(String directoryPath) {
        File root = new File(directoryPath);
        exploreDirectory(root);
    }

    /**
     * 递归遍历目录以寻找Java文件。
     *
     * @param directory 当前遍历的目录
     */
    private void exploreDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    exploreDirectory(file);
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 获取项目中所有Java文件的路径。
     *
     * @return Java文件的路径列表
     */
    public List<String> getJavaFiles() {
        return javaFiles;
    }

    public static void main(String[] args) {
        String projectPath = "src/main/resources/hadoop-2.6.0-src/hadoop-common-project/hadoop-common/src/main/java/";
        ProjectExplorer explorer = new ProjectExplorer();
        explorer.explore(projectPath);

        // 打印找到的所有Java文件路径
        explorer.getJavaFiles().forEach(System.out::println);
        System.out.println("File count: "+explorer.javaFiles.size());
    }
}
