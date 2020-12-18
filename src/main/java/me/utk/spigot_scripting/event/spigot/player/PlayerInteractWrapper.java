package me.utk.spigot_scripting.event.spigot.player;

import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class PlayerInteractWrapper {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final List FAILURE_COUNTS = new LinkedList();
    private static boolean isInitialized = false;

    public static void handleEvent(PlayerInteractEvent event) {
        PlayerInteractWrapper wrapper = new PlayerInteractWrapper(event);
        if (isInitialized)
            notifyHandlers(wrapper, FAILURE_COUNTS.iterator());
    }

    private static void notifyHandlers(PlayerInteractWrapper wrapper, Iterator it) {
    }

    public static void initialize() {
        if (isInitialized)
            return;
        isInitialized = true;
    }

    private final PlayerInteractEvent WRAPPED_EVENT;
    public PlayerInteractWrapper(PlayerInteractEvent event) {
        WRAPPED_EVENT = event;
    }

    public PlayerInteractEvent getEvent() {
        return WRAPPED_EVENT;
    }
}