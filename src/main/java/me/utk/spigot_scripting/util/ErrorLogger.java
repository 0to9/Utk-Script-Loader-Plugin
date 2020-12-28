package me.utk.spigot_scripting.util;

import me.utk.util.function.lambda.Lambda0;

public class ErrorLogger {
    public static boolean printStackTrace = true;
    public static void logError(Lambda0<String> message, Exception e) {
        System.err.println(message.get());
        if (printStackTrace) {
            System.err.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
