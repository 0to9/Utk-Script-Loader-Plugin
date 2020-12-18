package me.utk.spigot_scripting.event;

import me.utk.spigot_scripting.event.spigot.player.PlayerInteractWrapper;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ScriptInitializationWrapper {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final List FAILURE_COUNTS = new LinkedList();
    private static boolean isInitialized = false;

    public static void handleEvent() {
        @SuppressWarnings("InstantiationOfUtilityClass")
        ScriptInitializationWrapper wrapper = new ScriptInitializationWrapper();
        if (isInitialized)
            notifyHandlers(wrapper, FAILURE_COUNTS.iterator());
    }

    private static void notifyHandlers(ScriptInitializationWrapper wrapper, Iterator it) {
    }

    public static void initialize() {
        if (isInitialized)
            return;
        isInitialized = true;
    }
}
