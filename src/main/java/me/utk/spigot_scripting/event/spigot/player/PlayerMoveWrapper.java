package me.utk.spigot_scripting.event.spigot.player;

import me.utk.spigot_scripting.event.EventWrapper;
import me.utk.spigot_scripting.util.reflection.ReflectiveInitializer;
import org.bukkit.event.player.PlayerMoveEvent;

import java.lang.reflect.Method;

public class PlayerMoveWrapper extends EventWrapper<PlayerMoveEvent> {
    private static final PlayerMoveWrapper INSTANCE = new PlayerMoveWrapper(null);

    private PlayerMoveWrapper(PlayerMoveEvent event) {
        super(event);
    }

    @Override
    protected EventWrapper<PlayerMoveEvent> getInstance(PlayerMoveEvent event) {
        return new PlayerMoveWrapper(event);
    }

    public static void handleEvent(PlayerMoveEvent event) {
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
