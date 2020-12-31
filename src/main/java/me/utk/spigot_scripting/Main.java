package me.utk.spigot_scripting;

import javassist.CtClass;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.event.EventsUtil;
import me.utk.spigot_scripting.loader_linker.CustomClassPool;
import me.utk.spigot_scripting.loader_linker.ScriptLinker;
import me.utk.spigot_scripting.loader_linker.ScriptLoader;
import me.utk.spigot_scripting.script.ScriptParser;

import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String dump = null;
        List<String> scripts = new LinkedList<>();
        for (String arg : args) {
            int splitInd = arg.indexOf("=") + 1;
            String type = arg.substring(0, splitInd), filePath = arg.substring(splitInd);
            switch (type) {
                case "-dump=":
                    if (dump != null)
                        throw new IllegalArgumentException("Cannot have more than 1 dump file");
                    dump = filePath;
                    break;

                case "-script=":
                    scripts.add(filePath);
                    break;

                default:
                    throw new IllegalArgumentException("\"" + type + "\" is not a valid argument descriptor");
            }
        }

        CtClass.debugDump = dump;

        testScripts(scripts.toArray(new String[0]));
    }

    private static void testScripts(String... filepath) {
        processScripts(filepath, false);
    }

    public static void processScripts(String... filePaths) {
        processScripts(filePaths, true);
    }
    private static void processScripts(String[] filePaths, boolean initializeClasses) {
        // Reload class pool to allow reloading classes
        CustomClassPool.reloadDefault();

        // Load and link scripts
        for (String filePath : filePaths)
            ScriptLoader.loadScript(filePath);
        ScriptLinker.linkScripts();

        if (initializeClasses) {
            // Initialize classes
            EventsUtil.initializeEventWrappers();
            CommandUtil.initializeSubCommandHandler();
        }

        // Clear leftovers from script processing
        ScriptParser.resetScripts();
        ScriptLinker.CLASS_CLASS_MAP.clear();
    }

    public static void clearScriptResidues() {
        // Terminate classes
        EventsUtil.terminateEventWrappers();
        CommandUtil.terminateSubCommandHandler();
    }
}
