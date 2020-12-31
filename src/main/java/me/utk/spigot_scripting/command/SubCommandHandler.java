package me.utk.spigot_scripting.command;

import me.utk.spigot_scripting.plugin.PluginMain;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.util.reflection.ReflectiveInitializer;
import me.utk.util.data.ClassWrapper;
import me.utk.util.data.Pair;
import me.utk.util.data.Triplet;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.*;

public class SubCommandHandler {
    private static boolean isInitialized = false;

    private static final MultiValuedMap<String, Pair<Method, ClassWrapper<Integer>>> ALL_EXECUTORS = new HashSetValuedHashMap<>();
    private static final MultiValuedMap<String, Pair<Method, ClassWrapper<Integer>>> ALL_COMPLETERS = new HashSetValuedHashMap<>();

    public static String getSubCommandIDs() {
        StringBuilder builder = new StringBuilder();
        for (String id : ALL_EXECUTORS.keySet())
            builder.append(id).append(", ");
        return builder.toString();
    }
    public static List<String> getSubCommandIDsList() {
        return new LinkedList<>(ALL_EXECUTORS.keySet());
    }

    /**
     *
     */
    public static void executeCommand(CommandSender sender, String subCommand, String[] args) {
        if (isInitialized) {
            Collection<Pair<Method, ClassWrapper<Integer>>> execs = ALL_EXECUTORS.get(subCommand);
            if (execs != null)
                // try to prevent ConcurrentModificationException if reload happens
                try {
                    Iterator<Pair<Method, ClassWrapper<Integer>>> it = execs.iterator();
                    while (it.hasNext()) {
                        Pair<Method, ClassWrapper<Integer>> pair = it.next();
                        try {
                            pair.first.invoke(null, sender, args);
                        } catch (Exception e) {
                            if (++pair.second.value >= 10)
                                it.remove();
                            ErrorLogger.logError(() -> subCommand + " executor failed: #" + pair.second.value, e);
                        }
                    }
                } catch (ConcurrentModificationException ignored) {
                }
            else
                CommandUtil.sendWarningMessage(sender, "Unable to execute sub-command '" + subCommand + "'");
        } else
            CommandUtil.sendMessage(sender, PluginMain.PLUGIN_NAME + " command handler was not initialized");
    }

    /**
     *
     */
    public static List<String> tabComplete(CommandSender sender, String subCommand, String[] args) {
        if (isInitialized) {
            List<String> completions = new LinkedList<>();
            Collection<Pair<Method, ClassWrapper<Integer>>> comps = ALL_COMPLETERS.get(subCommand);
            if (comps != null)
                // try to prevent ConcurrentModificationException if reload happens
                try {
                    Iterator<Pair<Method, ClassWrapper<Integer>>> it = comps.iterator();
                    while (it.hasNext()) {
                        Pair<Method, ClassWrapper<Integer>> pair = it.next();
                        try {
                            //noinspection unchecked
                            completions.addAll((List<String>) pair.first.invoke(null, sender, args));
                        } catch (Exception e) {
                            if (++pair.second.value >= 10)
                                it.remove();
                            ErrorLogger.logError(() -> subCommand + " tab completer failed: #" + pair.second.value, e);
                        }
                    }
                } catch (ConcurrentModificationException ignored) {
                }
            return completions;
        }
        return null;
    }

    static final Collection<ReflectiveInitializer<Triplet<String, Method, Boolean>>> INITIALIZERS = new LinkedList<>();

    public static void initialize() {
        if (isInitialized)
            return;
        for (ReflectiveInitializer<Triplet<String, Method, Boolean>> init : INITIALIZERS)
            try {
                Triplet<String, Method, Boolean> trip = init.get();
                (trip.third ? ALL_EXECUTORS : ALL_COMPLETERS).put(trip.first, new Pair<>(trip.second, new ClassWrapper<>(0)));
            } catch (ReflectiveOperationException ignored) {
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
