package io.github.utk003.spigot_scripting.event.spigot.player;

import io.github.utk003.spigot_scripting.event.EventWrapper;
import io.github.utk003.spigot_scripting.util.reflection.ReflectiveInitializer;
import org.bukkit.event.player.PlayerInteractEvent;

import java.lang.reflect.Method;

public class PlayerInteractWrapper extends EventWrapper<PlayerInteractEvent> {
    private static final PlayerInteractWrapper INSTANCE = new PlayerInteractWrapper(null);

    private PlayerInteractWrapper(PlayerInteractEvent event) {
        super(event);
    }

    @Override
    protected EventWrapper<PlayerInteractEvent> getInstance(PlayerInteractEvent event) {
        return new PlayerInteractWrapper(event);
    }

    public static void handleEvent(PlayerInteractEvent event) {
        INSTANCE.handleEvent0(event);
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