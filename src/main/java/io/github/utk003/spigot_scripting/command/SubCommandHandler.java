package io.github.utk003.spigot_scripting.command;

import io.github.utk003.spigot_scripting.plugin.PluginMain;
import io.github.utk003.spigot_scripting.script.elements.Command;
import io.github.utk003.spigot_scripting.util.reflection.ReflectiveInitializer;
import io.github.utk003.spigot_scripting.util.ColoredText;
import io.github.utk003.spigot_scripting.util.Logger;
import io.github.utk003.util.data.Ref;
import io.github.utk003.util.data.Pair;
import io.github.utk003.util.data.Triplet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.*;

public class SubCommandHandler {
    private static boolean isInitialized = false;

    private static final MultiValuedMap<String, Pair<Method, Ref<Integer>>> ALL_EXECUTORS = new HashSetValuedHashMap<>(),
            ALL_COMPLETERS = new HashSetValuedHashMap<>(), ALL_HELP_MENUS = new HashSetValuedHashMap<>();

    public static String getSubCommandExecutorIDs() {
        StringBuilder builder = new StringBuilder();
        for (String id : ALL_EXECUTORS.keySet())
            builder.append(id).append(", ");
        return builder.toString();
    }
    public static List<String> getSubCommandExecutorIDsList() {
        return new LinkedList<>(ALL_EXECUTORS.keySet());
    }
    public static List<String> getSubCommandHelpMenuIDsList() {
        return new LinkedList<>(ALL_HELP_MENUS.keySet());
    }

    public static void executeCommand(CommandSender sender, String subCommand, String[] args) {
        if (isInitialized) {
            Collection<Pair<Method, Ref<Integer>>> execs = ALL_EXECUTORS.get(subCommand);
            if (execs != null)
                // try to prevent ConcurrentModificationException if reload happens
                try {
                    Iterator<Pair<Method, Ref<Integer>>> it = execs.iterator();
                    while (it.hasNext()) {
                        Pair<Method, Ref<Integer>> pair = it.next();
                        try {
                            pair.first.invoke(null, sender, args);
                        } catch (Exception e) {
                            if (++pair.second.value >= 10)
                                it.remove();
                            Logger.logError(subCommand + " executor failed: failure #" + pair.second.value, e);
                        }
                    }
                } catch (ConcurrentModificationException ignored) {
                }
            else
                CommandUtil.sendWarningMessage(sender, "Unable to execute sub-command '" + subCommand + "'");
        } else
            CommandUtil.sendMessage(sender, PluginMain.PLUGIN_NAME + " sub-command handler was not initialized");
    }

    public static List<String> tabComplete(CommandSender sender, String subCommand, String[] args) {
        List<String> completions = new LinkedList<>();
        if (isInitialized) {
            Collection<Pair<Method, Ref<Integer>>> comps = ALL_COMPLETERS.get(subCommand);
            if (comps != null)
                // try to prevent ConcurrentModificationException if reload happens
                try {
                    Iterator<Pair<Method, Ref<Integer>>> it = comps.iterator();
                    while (it.hasNext()) {
                        Pair<Method, Ref<Integer>> pair = it.next();
                        try {
                            //noinspection unchecked
                            completions.addAll((List<String>) pair.first.invoke(null, sender, args));
                        } catch (Exception e) {
                            if (++pair.second.value >= 10)
                                it.remove();
                            Logger.logError(subCommand + " tab completer failed: failure #" + pair.second.value, e);
                        }
                    }
                } catch (ConcurrentModificationException ignored) {
                }
        }
        return completions;
    }

    public static void printHelp(CommandSender sender, String subCommand, String[] args) {
        if (isInitialized) {
            Collection<Pair<Method, Ref<Integer>>> comps = ALL_HELP_MENUS.get(subCommand);
            if (comps != null)
                // try to prevent ConcurrentModificationException if reload happens
                try {
                    int countMenusPrinted = 0;
                    Iterator<Pair<Method, Ref<Integer>>> it = comps.iterator();
                    while (it.hasNext()) {
                        Pair<Method, Ref<Integer>> pair = it.next();
                        try {
                            String[] menu = (String[]) pair.first.invoke(null, sender, args);
                            if (menu.length > 0) {
                                CommandUtil.sendMessage(sender, "Help Menu (#" + (++countMenusPrinted) + ") for Sub-Command '" + subCommand + "'");
                                for (String msg : menu)
                                    CommandUtil.sendMessage(sender, ColoredText.parseString(msg));
                            }
                        } catch (Exception e) {
                            if (++pair.second.value >= 10)
                                it.remove();
                            Logger.logError(subCommand + " help menu failed: failure #" + pair.second.value, e);
                        }
                    }
                    if (countMenusPrinted <= 0)
                        CommandUtil.sendWarningMessage(sender, "Unable to create valid help menu for sub-command '" + args[1] + "'");
                } catch (ConcurrentModificationException ignored) {
                }
            else
                CommandUtil.sendWarningMessage(sender, "Unable to print help menu for sub-command '" + subCommand + "'");
        } else
            CommandUtil.sendMessage(sender, PluginMain.PLUGIN_NAME + " sub-command handler was not initialized");
    }

    static final Collection<ReflectiveInitializer<Triplet<String, Method, Command.HandlerType>>> INITIALIZERS = new LinkedList<>();

    public static void initialize() {
        if (isInitialized)
            return;
        for (ReflectiveInitializer<Triplet<String, Method, Command.HandlerType>> init : INITIALIZERS)
            try {
                Triplet<String, Method, Command.HandlerType> trip = init.get();
                switch (trip.third) {
                    case EXECUTOR:
                        ALL_EXECUTORS.put(trip.first, new Pair<>(trip.second, new Ref<>(0)));
                        break;

                    case TAB_COMPLETER:
                        ALL_COMPLETERS.put(trip.first, new Pair<>(trip.second, new Ref<>(0)));
                        break;

                    case HELP_MENU:
                        ALL_HELP_MENUS.put(trip.first, new Pair<>(trip.second, new Ref<>(0)));
                        break;
                }
            } catch (ReflectiveOperationException e) {
                Logger.logError("Unexpected error while initializing sub-command handler", e);
            }
        INITIALIZERS.clear();
        isInitialized = true;
    }

    public static void terminate() {
        ALL_EXECUTORS.clear();
        ALL_COMPLETERS.clear();
        isInitialized = false;
    }
}
