package io.github.utk003.spigot_scripting.plugin;

import io.github.utk003.spigot_scripting.Main;
import io.github.utk003.spigot_scripting.event.ScriptInitializationWrapper;
import io.github.utk003.spigot_scripting.event.ScriptTerminationWrapper;
import io.github.utk003.spigot_scripting.plugin.command.PluginCommandExecutor;
import io.github.utk003.spigot_scripting.plugin.command.PluginTabCompleter;
import io.github.utk003.spigot_scripting.util.ColoredText;
import io.github.utk003.spigot_scripting.util.Logger;
import io.github.utk003.spigot_scripting.util.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

import static io.github.utk003.spigot_scripting.plugin.command.PluginTabCompleter.ChangelogHandler;
import static io.github.utk003.spigot_scripting.plugin.command.PluginTabCompleter.ChangelogHandler.ChangelogSection;

public class PluginMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "Utk Script Loader Plugin";
    // change version here, in plugin.yml, and create version changelog
    public static final String VERSION = "pre3.0.0-1.16.4-rev6";

    public static final String PATH = FileUtil.PROJECT_CLASS_PATH + "plugin.PluginMain";

    public static Plugin PLUGIN;
    public static PluginMain INSTANCE;
    public static final Set<OfflinePlayer> ALL_PLAYERS = new HashSet<>(30);

    public static String VERBOSE_OUTPUT_CONFIG_PATH = "Print Verbose Output";
    public static String RELOAD_DEFAULT_SCRIPTS_CONFIG_PATH = "Install Default Scripts";

    public static String GITHUB_URL = "https://github.com/0to9/Utk-Script-Loader-Plugin";

    @Override
    public void onEnable() {
        // Set instance variables
        PLUGIN = INSTANCE = this;

        // Create logger dump folder
        Logger.createDumpFolder(getDataFolder().toString());

        // Call extracted helper method
        doOnEnableAlways();

        // Load changelog
        loadChangelog();

        // Enable plugin command handlers
        {
            PluginCommand pluginCommand = Objects.requireNonNull(getCommand("usl"));
            pluginCommand.setExecutor(new PluginCommandExecutor());
            pluginCommand.setTabCompleter(new PluginTabCompleter());
        }
    }

    private void doOnEnableAlways() {
        // Set logger time stamp
        Logger.updateTimeStamp();

        // Save config if missing
        saveDefaultConfig();

        // Reload config to newest values
        reloadConfig();

        // Get error-logging config
        Logger.printVerboseToConsole = getConfig().getBoolean(VERBOSE_OUTPUT_CONFIG_PATH, false);

        // Generate all_players set
        {
            // TODO add players to ALL_PLAYERS when they join the game
            // ^^ deals with new players joining the server
            final long NOW = new Date().getTime(), MAX_TIME_OFF = 14 * 24L * 3600L * 1000L; // MAX_TIME_OFF = 14 days
            for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
                if (player.isBanned()) continue;                           // Skip banned players
                if (NOW - player.getLastPlayed() > MAX_TIME_OFF) continue; // Skip if offline for more than MAX_TIME_OFF
                ALL_PLAYERS.add(player);
            }
        }

        // Copy scripts if not present
        {
            if (getConfig().getBoolean(RELOAD_DEFAULT_SCRIPTS_CONFIG_PATH, true)) {
                reloadScripts();
                setConfig(RELOAD_DEFAULT_SCRIPTS_CONFIG_PATH, false);
            }
        }

        // Process scripts
        Main.processScripts(getDataFolder() + "/scripts/main.txt");

        // Send script initialization event
        ScriptInitializationWrapper.handleEvent();
    }

    @Override
    public void onDisable() {
        // Call extracted helper method
        doOnDisableAlways();

        // Clear instance variables
        PLUGIN = INSTANCE = null;
    }

    private void doOnDisableAlways() {
        // Send script termination event
        ScriptTerminationWrapper.handleEvent();

        // Destroy scripts
        Main.clearScriptResidues();

        // Clear all_players set
        ALL_PLAYERS.clear();

        // Dump logged messages and clear logger time stamp
        Logger.dumpMessages();
        Logger.clearTimeStamp();
    }

    public void reloadPlugin() {
        // Disable plugin
        doOnDisableAlways();

        // Enable plugin
        doOnEnableAlways();
    }

    public void setConfig(String path, boolean newValue) {
        // Set config value
        getConfig().set(path, newValue);

        // Save config to file
        try {
            getConfig().save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            Logger.logError("Error while saving config file", e);
        }
    }

    private void loadChangelog() {
        try {
            List<String> versions = new LinkedList<>();
            BufferedReader br = new BufferedReader(Objects.requireNonNull(getTextResource("changelog/list.txt")));
            String line;
            while ((line = br.readLine()) != null)
                if (!line.isEmpty())
                    versions.add(line);

            for (String version : versions)
                try {
                    br = new BufferedReader(Objects.requireNonNull(getTextResource("changelog/" + version + ".txt")));

                    ChangelogSection section = new ChangelogSection();
                    boolean setHeader = false;
                    while ((line = br.readLine()) != null) {
                        if (!line.isEmpty()) {
                            if (!setHeader) {
                                section.setHeader(ColoredText.parseString(line, ChangelogSection.HEADER_FORMAT));
                                setHeader = true;
                            } else
                                section.appendLine(ColoredText.parseString(line, ChangelogSection.BODY_FORMAT));
                        } else if (setHeader) {
                            ChangelogHandler.appendVersionLog(version, section);
                            section = new ChangelogSection();
                            setHeader = false;
                        }
                    }
                    if (setHeader)
                        ChangelogHandler.appendVersionLog(version, section);
                } catch (Exception e) {
                    Logger.logError("Error while loading " + version + " changelog", e);
                }

        } catch (Exception e) {
            Logger.logError("Error while reading changelog list file", e);
        }
    }

    public void reloadScripts() {
        Queue<String> filesToAdd = new LinkedList<>();
        Set<String> done = new HashSet<>();
        filesToAdd.add("scripts/main.txt");

        while (!filesToAdd.isEmpty())
            try {
                String file = filesToAdd.poll(), parent = FileUtil.getParentDirectory(file);
                done.add(file);

                File outputFile = new File(getDataFolder(), file), outputParent = outputFile.getParentFile();
                if (!outputParent.exists())
                    //noinspection StatementWithEmptyBody
                    while (!outputParent.mkdirs()) ;

                BufferedReader br = new BufferedReader(Objects.requireNonNull(getTextResource(file)));
                PrintWriter pw = new PrintWriter(new FileWriter(outputFile));

                String line;
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                    if (line.startsWith("#include")) {
                        String[] split = line.replaceAll("\\s+", " ").trim().split(" ");
                        String toAdd = FileUtil.cleanFilePath(parent + split[2].substring(1, split[2].length() - 1));
                        if (!done.contains(toAdd))
                            filesToAdd.add(toAdd);
                    }
                }
                pw.close();
            } catch (Exception e) {
                Logger.logError("Error while reloading default scripts", e);
            }

        StringBuilder logBuilder = new StringBuilder("Default Script Loading Manifest (for console/logs):");
        for (String orderedFile : new TreeSet<>(done))
            logBuilder.append("\n").append(" - ").append(orderedFile);
        Logger.log(logBuilder.toString());
    }
}
