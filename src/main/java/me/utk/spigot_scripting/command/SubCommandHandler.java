package me.utk.spigot_scripting.command;

import org.bukkit.command.CommandSender;

import java.util.*;

@SuppressWarnings({"rawtypes","MismatchedQueryAndUpdateOfCollection"})
public class SubCommandHandler {
    private static final List EXECUTOR_FAILURE_COUNTS = new LinkedList();
    private static final List TAB_COMPLETER_FAILURE_COUNTS = new LinkedList();
    private static boolean isInitialized = false;

    private static final Set<String> ALL_SUB_COMMAND_IDS = new HashSet<>();

    /**
     * @param sender  The command caller
     * @param command The main command id (/utk or /v++ or /vpp)
     * @param args    The command call's arguments
     */
    public static void executeCommand(CommandSender sender, String command, String[] args) {
        if (isInitialized)
            if (args.length != 0)
                executionImplementation(sender, args[0], CommandUtil.trimArguments(args), EXECUTOR_FAILURE_COUNTS.iterator());
            else
                CommandUtil.sendErrorMessage(sender, command + " should be called with arguments");
        else
            CommandUtil.sendMessage(sender, command + " command handler was not initialized");
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
