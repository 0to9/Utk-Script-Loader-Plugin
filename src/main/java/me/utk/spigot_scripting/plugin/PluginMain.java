package me.utk.spigot_scripting.plugin;

import me.utk.spigot_scripting.Main;
import me.utk.spigot_scripting.event.ScriptInitializationWrapper;
import me.utk.spigot_scripting.event.ScriptTerminationWrapper;
import me.utk.spigot_scripting.plugin.command.PluginCommandExecutor;
import me.utk.spigot_scripting.plugin.command.PluginTabCompleter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PluginMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "Utk Script Loader Plugin";
    public static final String VERSION = "3.0.0-1.16.4";

    public static boolean areScriptsProcessed = false;

    @Override
    public void onEnable() {
        // Save config if missing
        saveDefaultConfig();

        // Enable plugin command handlers
        PluginCommand pluginCommand = Objects.requireNonNull(getCommand("usl"));
        pluginCommand.setExecutor(new PluginCommandExecutor());
        pluginCommand.setTabCompleter(new PluginTabCompleter());

        // Process scripts
        Main.createScripts("src/main/resources/scripts/main.txt"); // TODO fix filepath

        // Send script initialization event
        ScriptInitializationWrapper.handleEvent();
    }

    @Override
    public void onDisable() {
        // Send script termination event
        ScriptTerminationWrapper.handleEvent();

        // Destroy scripts
        Main.destroyScripts();

        // Save config if missing
        saveDefaultConfig();
    }
}
