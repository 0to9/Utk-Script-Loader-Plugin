package me.utk.spigot_scripting.event;

import me.utk.spigot_scripting.util.reflection.ReflectiveInitializer;

import java.lang.reflect.Method;

public class ScriptTerminationWrapper extends EventWrapper<EventWrapper.NoEvent> {
    private static final ScriptTerminationWrapper INSTANCE = new ScriptTerminationWrapper(null);

    private ScriptTerminationWrapper(NoEvent event) {
        super(event);
    }

    @Override
    protected EventWrapper<NoEvent> getInstance(NoEvent event) {
        return new ScriptTerminationWrapper(event);
    }

    public static void handleEvent() {
        INSTANCE.handleEvent0(NoEvent.EVENT_INSTANCE);
    }

    public static void addHandler(ReflectiveInitializer<Method> handlerInit) {
        INSTANCE.INITIALIZERS.add(handlerInit);
    }

    public static void initialize() {
        INSTANCE.initialize0();
    }
    public static void terminate() {
        INSTANCE.terminate0();
    }
}