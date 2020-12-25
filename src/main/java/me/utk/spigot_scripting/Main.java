package me.utk.spigot_scripting;

import javassist.CtClass;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.event.EventsUtil;
import me.utk.spigot_scripting.event.ScriptInitializationWrapper;
import me.utk.spigot_scripting.loader_linker.ScriptLinker;
import me.utk.spigot_scripting.loader_linker.ScriptLoader;

public class Main {
    public static void main(String[] args) {
        CtClass.debugDump = "dump";

        // Start script processing
        ScriptLoader.loadScript("src/main/resources/scripts/main.txt"); // temporary file path
        ScriptLinker.linkScripts();
        EventsUtil.loadAndInitializeEventClassChanges();
        CommandUtil.loadAndInitializeSubCommandHandler();
        ScriptInitializationWrapper.handleEvent();
        // End script processing
    }
}
