package me.utk.spigot_scripting.event.spigot.player;

import me.utk.spigot_scripting.event.EventWrapper;
import me.utk.spigot_scripting.util.reflection.ReflectiveInitializer;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.Method;

public class PlayerJoinWrapper extends EventWrapper<PlayerJoinEvent> {
    private static final PlayerJoinWrapper INSTANCE = new PlayerJoinWrapper(null);

    private PlayerJoinWrapper(PlayerJoinEvent event) {
        super(event);
    }

    @Override
    protected EventWrapper<PlayerJoinEvent> getInstance(PlayerJoinEvent event) {
        return new PlayerJoinWrapper(event);
    }

    public static void handleEvent(PlayerJoinEvent event) {
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