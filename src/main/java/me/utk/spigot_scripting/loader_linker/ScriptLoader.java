package me.utk.spigot_scripting.loader_linker;

import me.utk.spigot_scripting.script.ScriptParser;
import me.utk.spigot_scripting.script.elements.*;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.util.FileUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ScriptLoader {
    public static final Semaphore SCRIPT_LOCKER = new Semaphore(1, true);
    public static final Set<Script> LOADED_SCRIPTS = new HashSet<>();
    public static void loadScript(String filepath) {
        try {
            filepath = FileUtil.cleanFilePath(filepath);
            Script script = ScriptParser.parseScript(filepath);

            SCRIPT_LOCKER.acquire();

            LOADED_SCRIPTS.add(script);
            addIncludedScriptsToSet(script);

            SCRIPT_LOCKER.release();
        } catch (Exception e) {
            String finalFilepath = filepath;
            ErrorLogger.logError(() -> "Unable to load script in \"" + finalFilepath + "\"", e);
        }
    }

    private static void addIncludedScriptsToSet(Script script) {
        for (Map.Entry<String, Script> e : script.INCLUDED_SCRIPTS.entrySet())
            if (LOADED_SCRIPTS.add(e.getValue()))
                addIncludedScriptsToSet(e.getValue());
    }
}
