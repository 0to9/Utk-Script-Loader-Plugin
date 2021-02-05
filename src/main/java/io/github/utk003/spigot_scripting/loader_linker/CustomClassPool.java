package io.github.utk003.spigot_scripting.loader_linker;

import javassist.ClassPool;
import javassist.LoaderClassPath;

public class CustomClassPool extends ClassPool {
    private static final ClassLoader LOADER = CustomClassPool.class.getClassLoader();

    public CustomClassPool() {
        appendSystemPath();
        appendClassPath(new LoaderClassPath(LOADER));
    }

    private static ClassPool defaultPool;
    public static ClassPool getDefault() {
        return getDefault(false);
    }
    public static synchronized ClassPool getDefault(boolean reload) {
        if (defaultPool == null || reload)
            defaultPool = new CustomClassPool();
        return defaultPool;
    }
    public static void reloadDefault() {
        getDefault(true);
    }

    @Override
    public ClassLoader getClassLoader() {
        return LOADER;
    }
}
