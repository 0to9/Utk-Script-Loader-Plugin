package me.utk.spigot_scripting.command;

import me.utk.spigot_scripting.plugin.PluginMain;
import org.bukkit.command.CommandSender;

import java.util.*;

@SuppressWarnings({"rawtypes","MismatchedQueryAndUpdateOfCollection"})
public class SubCommandHandler {
    private static final List EXECUTOR_FAILURE_COUNTS = new LinkedList();
    private static final List TAB_COMPLETER_FAILURE_COUNTS = new LinkedList();
    private static boolean isInitialized = false;

    private static final Set<String> ALL_SUB_COMMAND_IDS = new HashSet<>();

    public static String getSubCommandIDs() {
        StringBuilder builder = new StringBuilder();
        for (String id : ALL_SUB_COMMAND_IDS)
            builder.append(id).append(", ");
        return builder.toString();
    }

    /**
     *
     */
    public static void executeCommand(CommandSender sender, String subCommand, String[] args) {
        if (isInitialized)
            executionImplementation(sender, subCommand, args, EXECUTOR_FAILURE_COUNTS.iterator());
        else
            CommandUtil.sendMessage(sender, PluginMain.PLUGIN_NAME + " command handler was not initialized");
    }
    private static void executionImplementation(CommandSender sender, String subCommand, String[] args, Iterator it) {
    }

    /**
     *
     */
    public static List<String> tabComplete(CommandSender sender, String subCommand, String[] args) {
        if (isInitialized) {
            List<String> completions = new LinkedList<>();
            tabCompletionImplementation(sender, subCommand, args, completions, TAB_COMPLETER_FAILURE_COUNTS.iterator());
            return CommandUtil.purgeCompletions(args[args.length - 1], completions);
        }
        return null;
    }
    private static void tabCompletionImplementation(CommandSender sender, String subCommand, String[] args,
                                                    List currentCompletions, Iterator it) {
    }

    public static void initialize() {
        if (isInitialized)
            return;
        isInitialized = true;
    }
}
