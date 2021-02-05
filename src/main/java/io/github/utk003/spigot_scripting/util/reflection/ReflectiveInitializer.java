package io.github.utk003.spigot_scripting.util.reflection;

@FunctionalInterface
public interface ReflectiveInitializer<R> {
    R get() throws ReflectiveOperationException;
}
