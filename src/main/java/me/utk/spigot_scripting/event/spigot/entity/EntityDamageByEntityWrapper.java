package me.utk.spigot_scripting.event.spigot.entity;

import me.utk.spigot_scripting.event.EventWrapper;
import me.utk.spigot_scripting.util.reflection.ReflectiveInitializer;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.lang.reflect.Method;

public class EntityDamageByEntityWrapper extends EventWrapper<EntityDamageByEntityEvent> {
    private static final EntityDamageByEntityWrapper INSTANCE = new EntityDamageByEntityWrapper(null);

    private EntityDamageByEntityWrapper(EntityDamageByEntityEvent event) {
        super(event);
    }

    @Override
    protected EventWrapper<EntityDamageByEntityEvent> getInstance(EntityDamageByEntityEvent event) {
        return new EntityDamageByEntityWrapper(event);
    }

    public static void handleEvent(EntityDamageByEntityEvent event) {
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