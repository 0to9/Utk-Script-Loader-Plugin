package me.utk.spigot_scripting.loader_linker;

import javassist.ClassPool;
import javassist.LoaderClassPath;
import me.utk.spigot_scripting.plugin.PluginMain;
import me.utk.util.function.lambda.Lambda0;
import me.utk.util.function.void_lambda.VoidLambda0;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class CustomClassPool extends ClassPool {
    private static final URL[] URLs = {
            PluginMain.class.getProtectionDomain().getCodeSource().getLocation(),
            VoidLambda0.class.getProtectionDomain().getCodeSource().getLocation(),
    };
    private final ClassLoader LOADER;
    public CustomClassPool() {
        LOADER = new URLClassLoader(URLs, ClassPool.getDefault().getClassLoader());
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
