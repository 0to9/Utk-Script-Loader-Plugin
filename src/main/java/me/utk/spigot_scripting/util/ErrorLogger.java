package me.utk.spigot_scripting.util;

import me.utk.util.function.lambda.Lambda0;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public class ErrorLogger {
    public static boolean printStackTrace = true;
    public static void logError(Lambda0<String> message, Exception e) {
        try {
            ConsoleCommandSender console = Bukkit.getConsoleSender();

            console.sendMessage(ChatColor.RED + message.get());
            if (printStackTrace) {
                console.sendMessage(ChatColor.DARK_RED + e.getMessage());

                StackTraceElement[] elements = e.getStackTrace();
                console.sendMessage("" + ChatColor.RED + elements[0]);
                for (int i = 1; i < elements.length; i++)
                    console.sendMessage("\t" + ChatColor.DARK_RED + elements[i]);
            }
        } catch (Exception e2) {
            System.err.println(message.get());
            if (printStackTrace) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }
        }
    }
}
