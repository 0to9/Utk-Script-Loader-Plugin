package me.utk.spigot_scripting.event;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class ScriptTerminationWrapper {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final List FAILURE_COUNTS = new LinkedList();
    private static boolean isInitialized = false;

    public static void handleEvent() {
        @SuppressWarnings("InstantiationOfUtilityClass")
        ScriptTerminationWrapper wrapper = new ScriptTerminationWrapper();
        if (isInitialized)
            notifyHandlers(wrapper, FAILURE_COUNTS.iterator());
    }

    private static void notifyHandlers(ScriptTerminationWrapper wrapper, Iterator it) {
    }

    public static void initialize() {
        if (isInitialized)
            return;
        isInitialized = true;
    }
}