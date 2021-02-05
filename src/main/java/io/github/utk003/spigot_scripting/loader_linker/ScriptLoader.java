package io.github.utk003.spigot_scripting.loader_linker;

import io.github.utk003.spigot_scripting.script.ScriptParser;
import io.github.utk003.spigot_scripting.util.Logger;
import io.github.utk003.spigot_scripting.util.FileUtil;

public class ScriptLoader {
    public static void loadScript(String filepath) {
        try {
            filepath = FileUtil.cleanFilePath(filepath);
            ScriptParser.parseScript(filepath);
        } catch (Exception e) {
            Logger.logError("Unable to load script in \"" + filepath + "\"", e);
        }
    }
}
