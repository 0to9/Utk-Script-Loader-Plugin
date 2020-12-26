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
    public static void loadScript(String filepath) {
        try {
            filepath = FileUtil.cleanFilePath(filepath);
            ScriptParser.parseScript(filepath);
        } catch (Exception e) {
            String finalFilepath = filepath;
            ErrorLogger.logError(() -> "Unable to load script in \"" + finalFilepath + "\"", e);
        }
    }
}
