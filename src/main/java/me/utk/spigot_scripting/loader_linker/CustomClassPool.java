package me.utk.spigot_scripting.loader_linker;

import javassist.ClassPool;
import me.utk.spigot_scripting.plugin.PluginMain;

import java.net.URL;
import java.net.URLClassLoader;

public class CustomClassPool extends ClassPool {
    private static final URL URL = PluginMain.class.getProtectionDomain().getCodeSource().getLocation();
    private final ClassLoader LOADER;
    public CustomClassPool() {
        LOADER = new URLClassLoader(new URL[]{URL}, ClassPool.getDefault().getClassLoader());
    }

    private static ClassPool defaultPool;
    public static ClassPool getDefault() {
        return getDefault(false);
    }
    public static synchronized ClassPool getDefault(boolean reload) {
        if (defaultPool == null || reload) {
            defaultPool = new CustomClassPool();
            defaultPool.appendSystemPath();
        }
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
