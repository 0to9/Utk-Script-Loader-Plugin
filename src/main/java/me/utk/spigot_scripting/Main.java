package me.utk.spigot_scripting;

import javassist.CtClass;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.event.EventsUtil;
import me.utk.spigot_scripting.event.ScriptInitializationWrapper;
import me.utk.spigot_scripting.event.ScriptTerminationWrapper;
import me.utk.spigot_scripting.loader_linker.CustomClassPool;
import me.utk.spigot_scripting.loader_linker.ScriptLinker;
import me.utk.spigot_scripting.loader_linker.ScriptLoader;
import me.utk.spigot_scripting.script.ScriptParser;
import me.utk.util.function.void_lambda.VoidLambda0;

public class Main {
    public static void main(String[] args) {
        CtClass.debugDump = "dump";

        String testPath = "test.txt";
        String scriptPath = "src/main/resources/scripts/main.txt"; // temporary file path
        String temp = "src/main/resources/scripts/scoreboard/distance.txt";

        testScript(scriptPath, () -> {
            ScriptInitializationWrapper.handleEvent();
            ScriptTerminationWrapper.handleEvent();
        });
    }

    private static void testScript(String filepath, VoidLambda0 todo) {
        createScripts(filepath);
        todo.run();
        destroyScripts();
    }

    public static void createScripts(String... filePaths) {
        // Reload class pool to allow reloading classes
        CustomClassPool.reloadDefault();

        // Load and link scripts
        for (String filePath : filePaths)
            ScriptLoader.loadScript(filePath);
        ScriptLinker.linkScripts();

        // Initialize classes
        EventsUtil.initializeEventWrappers();
        CommandUtil.initializeSubCommandHandler();

        // Clear leftovers from script processing
        ScriptParser.resetScripts();
        ScriptLinker.CLASS_CLASS_MAP.clear();
    }

    public static void destroyScripts() {
        // Terminate classes
        EventsUtil.terminateEventWrappers();
        CommandUtil.terminateSubCommandHandler();
    }
}
