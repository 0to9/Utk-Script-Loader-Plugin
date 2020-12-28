package me.utk.spigot_scripting.plugin;

import me.utk.spigot_scripting.Main;
import me.utk.spigot_scripting.event.ScriptInitializationWrapper;
import me.utk.spigot_scripting.event.ScriptTerminationWrapper;
import me.utk.spigot_scripting.plugin.command.PluginCommandExecutor;
import me.utk.spigot_scripting.plugin.command.PluginTabCompleter;
import me.utk.spigot_scripting.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PluginMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "Utk Script Loader Plugin";
    public static final String VERSION = "3.0.0-1.16.4";

    public static final String PATH = FileUtil.PROJECT_CLASS_PATH + "plugin.PluginMain";

    public static JavaPlugin INSTANCE;
    public static final Set<OfflinePlayer> ALL_PLAYERS = new HashSet<>(30);

    @Override
    public void onEnable() {
        // Save config if missing
        saveDefaultConfig();

        // Set instance variable
        INSTANCE = this;

        // Generate all_players set
        {
            final long NOW = new Date().getTime(), MAX_TIME_OFF = 14 * 24L * 3600L * 1000L; // MAX_TIME_OFF = 14 days
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.isBanned()) continue;                           // Skip banned players
                if (NOW - player.getLastPlayed() > MAX_TIME_OFF) continue; // Skip if offline for more than MAX_TIME_OFF
                ALL_PLAYERS.add(player);
            }
        }

        // Enable plugin command handlers
        {
            PluginCommand pluginCommand = Objects.requireNonNull(getCommand("usl"));
            pluginCommand.setExecutor(new PluginCommandExecutor());
            pluginCommand.setTabCompleter(new PluginTabCompleter());
        }

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

        // Clear all_players set
        ALL_PLAYERS.clear();

        // Clear instance variable
        INSTANCE = null;

        // Save config if missing
        saveDefaultConfig();
    }
}
