package me.utk.spigot_scripting.event;

import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.util.reflection.ReflectiveInitializer;

import java.lang.reflect.Method;
import java.util.*;

public abstract class EventWrapper<E> {
    public static final class NullEvent {
        @SuppressWarnings("InstantiationOfUtilityClass")
        public static final NullEvent EVENT_INSTANCE = new NullEvent();
    }

    private final E EVENT;
    protected EventWrapper(E event) {
        EVENT = event;
    }
    public E getEvent() {
        return EVENT;
    }

    protected abstract EventWrapper<E> getInstance(E event);

    private final Map<Method, Integer> HANDLERS = new HashMap<>();
    protected void handleEvent0(E event) {
        EventWrapper<E> wrapped = getInstance(event);
        if (isInitialized)
            // try to prevent ConcurrentModificationException if reload happens
            try {
                Iterator<Map.Entry<Method, Integer>> it = HANDLERS.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Method, Integer> entry = it.next();
                    try {
                        entry.getKey().invoke(null, wrapped);
                    } catch (Exception e) {
                        int fails = entry.getValue() + 1;
                        entry.setValue(fails);
                        if (fails >= 10)
                            it.remove();
                        ErrorLogger.logError(() -> getClass() + " handler failed: #" + fails, e);
                    }
                }
            } catch (ConcurrentModificationException ignored) {
            }
    }

    protected final Collection<ReflectiveInitializer<Method>> INITIALIZERS = new LinkedList<>();
    private boolean isInitialized = false;

    protected void initialize0() {
        if (isInitialized)
            return;
        for (ReflectiveInitializer<Method> init : INITIALIZERS)
            try {
                Method method = init.get();
                HANDLERS.put(method, 0);
            } catch (ReflectiveOperationException ignored) {
            }
        INITIALIZERS.clear();
        isInitialized = true;
    }

    protected void terminate0() {
        HANDLERS.clear();
        isInitialized = false;
    }
}
