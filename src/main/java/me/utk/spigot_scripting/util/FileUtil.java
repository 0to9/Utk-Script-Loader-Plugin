package me.utk.spigot_scripting.util;

import java.io.File;

public class FileUtil {
    public static final String PROJECT_PACKAGE = "me.utk.spigot_scripting";
    public static final String PROJECT_CLASS_PATH = PROJECT_PACKAGE + ".";

    public static final String PATH = PROJECT_CLASS_PATH + "util.FileUtil";

    public static String cleanFilePath(String path) {
        String[] arr = path.split("[/\\\\]");

        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (int i = 0; i < arr.length; i++)
            if (!arr[i].equals("..")) {
                if (!arr[i].isEmpty() && !arr[i].equals("."))
                    arr[index++] = arr[i];
            } else if (index <= 0)
                builder.append(File.separatorChar).append("..");
            else
                index--;
        for (int i = 0; i < index; i++)
            builder.append(File.separatorChar).append(arr[i]);
        return builder.substring(1);
    }

    public static String getParentDirectory(String path) {
        return path.substring(0, path.lastIndexOf(File.separatorChar) + 1);
    }
    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf(File.separatorChar) + 1).replaceAll("\\.txt", "");
    }
}
