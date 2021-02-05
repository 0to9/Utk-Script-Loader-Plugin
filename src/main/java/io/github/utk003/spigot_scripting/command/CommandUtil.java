package io.github.utk003.spigot_scripting.command;

import io.github.utk003.spigot_scripting.script.elements.Command;
import io.github.utk003.spigot_scripting.util.Logger;
import javassist.CtClass;
import io.github.utk003.spigot_scripting.loader_linker.ScriptLinker;
import io.github.utk003.util.data.Triplet;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class CommandUtil {
    private CommandUtil() {
    }

    public static void addSubCommandHandler(List<String> subCommandID, CtClass clazz, String methodID, Command.HandlerType type) {
        Logger.log("Added sub-command handler: " + methodID);
        for (String id : subCommandID)
            SubCommandHandler.INITIALIZERS.add(() -> {
                Class<?> finalClass = ScriptLinker.CLASS_CLASS_MAP.get(clazz);
                Method method = finalClass.getMethod(methodID, CommandSender.class, String[].class);
                return new Triplet<>(id, method, type);
            });
    }

    public static void initializeSubCommandHandler() {
        SubCommandHandler.initialize();
        Logger.log("SubCommandHandler initialized");
    }
    public static void terminateSubCommandHandler() {
        SubCommandHandler.terminate();
        Logger.log("SubCommandHandler terminated");
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
    public static void sendWarningMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GOLD + message);
    }
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }

    public static List<String> purgeCompletions(String currentInput, List<String> completions) {
        if (completions != null) {
            String currentInputLowerCase = currentInput.toLowerCase();
            Set<String> seen = new HashSet<>(4 * completions.size() / 3);
            completions.removeIf(s -> !s.toLowerCase().contains(currentInputLowerCase) || !seen.add(s));
            if (completions.isEmpty())
                completions = null;
        }
        return completions;
    }

    public static String[] trimArguments(String... args) {
        return trimArguments(1, args);
    }
    public static String[] trimArguments(int numToTrim, String... args) {
        String[] newArgs = new String[args.length - numToTrim]; // intentionally throw exception if args.length < numToTrim
        System.arraycopy(args, numToTrim, newArgs, 0, newArgs.length);
        return newArgs;
    }
}
