package me.utk.spigot_scripting.command;

import javassist.CtClass;
import me.utk.spigot_scripting.loader_linker.ScriptLinker;
import me.utk.util.data.ClassWrapper;
import me.utk.util.data.Pair;
import me.utk.util.data.Triplet;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.List;

public abstract class CommandUtil {
    private CommandUtil() {
    }

    public static void addSubCommandExecutor(String subCommandID, CtClass clazz, String methodID) {
        subCommandHelper(subCommandID, clazz, methodID, true);
    }
    public static void addSubCommandTabCompleter(String subCommandID, CtClass clazz, String methodID) {
        subCommandHelper(subCommandID, clazz, methodID, false);
    }
    private static void subCommandHelper(String subCommandID, CtClass clazz, String methodID, boolean isExec) {
        SubCommandHandler.INITIALIZERS.add(() -> {
            Class<?> finalClass = ScriptLinker.CLASS_CLASS_MAP.get(clazz);
            Method method = finalClass.getMethod(methodID, CommandSender.class, String[].class);
            return new Triplet<>(subCommandID, method, isExec);
        });
    }

    public static void initializeSubCommandHandler() {
        SubCommandHandler.initialize();
    }
    public static void terminateSubCommandHandler() {
        SubCommandHandler.terminate();
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
    public static void sendWarningMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GOLD + message); // TODO choose gold (= orange) or yellow
    }
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }

    public static List<String> purgeCompletions(String currentInput, List<String> completions) {
        if (completions != null) {
            String currentInputLowerCase = currentInput.toLowerCase();
            completions.removeIf(s -> !s.toLowerCase().contains(currentInputLowerCase));
            if (completions.isEmpty())
                completions = null;
        }
        return completions;
    }

    public static String[] trimArguments(String... args) {
        String[] newArgs = new String[args.length - 1]; // intentionally throw exception if args.length == 0
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }
}
