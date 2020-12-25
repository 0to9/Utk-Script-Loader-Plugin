package me.utk.spigot_scripting.plugin;

import me.utk.spigot_scripting.plugin.command.PluginCommandExecutor;
import me.utk.spigot_scripting.plugin.command.PluginTabCompleter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PluginMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "Utk Script Loader Plugin";
    public static final String VERSION = "3.0.0-1.16.4";

    @Override
    public void onEnable() {
        // Enable plugin command handlers
        PluginCommand pluginCommand = Objects.requireNonNull(getCommand("usl"));
        pluginCommand.setExecutor(new PluginCommandExecutor());
        pluginCommand.setTabCompleter(new PluginTabCompleter());
    }

    @Override
    public void onDisable() {
    }
}
