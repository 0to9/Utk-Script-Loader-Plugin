package io.github.utk003.spigot_scripting.util;

import io.github.utk003.spigot_scripting.plugin.PluginMain;
import io.github.utk003.util.data.Pair;
import io.github.utk003.util.data.Triplet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Logger {
    public static final String PATH = FileUtil.PROJECT_CLASS_PATH + "util.Logger";

    public static boolean printVerboseToConsole = true;

    private static String outputDumpFileName;
    static {
        clearTimeStamp();
    }

    public static void updateTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        String date = pad(calendar.get(Calendar.YEAR)) + pad(calendar.get(Calendar.MONTH)) + pad(calendar.get(Calendar.DATE));
        String time = pad(calendar.get(Calendar.AM_PM) * 12 + calendar.get(Calendar.HOUR)) + pad(calendar.get(Calendar.MINUTE)) +
                pad(calendar.get(Calendar.SECOND)) + pad2(calendar.get(Calendar.MILLISECOND));
        String readable = calendar.getTime().toString().replaceAll(":", ".");

        outputDumpFileName = date + time + " - " + readable;
    }
    public static void clearTimeStamp() {
        outputDumpFileName = "something went wrong (0000)";
    }

    private static String pad(int i) {
        return (i < 10 ? "0" : "") + i;
    }
    private static String pad2(int i) {
        return (i < 10 ? "00" : i < 100 ? "0" : "") + i;
    }

    public static void createDumpFolder(String pluginDataFolder) {
        File folder = new File(pluginDataFolder + "/logs");
        if (!folder.exists())
            //noinspection StatementWithEmptyBody
            while (!folder.mkdirs()) /* do nothing */;
    }

    private static PrintWriter getPrintWriter() throws IOException {
        String path = PluginMain.INSTANCE == null ? "" : PluginMain.INSTANCE.getDataFolder() + "/";
        return new PrintWriter(new FileWriter(path + "logs/" + outputDumpFileName + ".txt", true));
    }

    // first = message, second = timestamp, third = exception
    private static final Queue<Triplet<String, String, Exception>> MESSAGE_Q = new ConcurrentLinkedQueue<>();
    public static void dumpMessages() {
        try {
            PrintWriter pw = getPrintWriter();
            while (!MESSAGE_Q.isEmpty()) {
                Triplet<String, String, Exception> message = MESSAGE_Q.poll();
                pw.println(message.second + (message.third == null ? "" : " (error log)")); // small searchable flag
                pw.println(message.first);
                if (message.third != null)
                    message.third.printStackTrace(pw);
                pw.println();
            }
            pw.close();
        } catch (Exception e) {
            System.err.println("Something went wrong (0001) - Unexpected failure while saving error/message dumps");
            if (printVerboseToConsole)
                e.printStackTrace(System.err);
        }
    }

    public static void log(String message) {
        if (printVerboseToConsole)
            System.out.println(message);
        MESSAGE_Q.add(new Triplet<>(message, new Date().toString(), null));
    }
    public static void logError(Exception e) {
        logError("~~ no custom error message provided ~~", e);
    }
    public static void logError(String message, Exception e) {
        System.err.println(message);

        if (printVerboseToConsole)
            e.printStackTrace(System.err);

        MESSAGE_Q.add(new Triplet<>(message, new Date().toString(), e));
    }
}
