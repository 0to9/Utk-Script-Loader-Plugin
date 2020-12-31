package me.utk.spigot_scripting.plugin;

import me.utk.spigot_scripting.Main;
import me.utk.spigot_scripting.event.ScriptInitializationWrapper;
import me.utk.spigot_scripting.event.ScriptTerminationWrapper;
import me.utk.spigot_scripting.plugin.command.PluginCommandExecutor;
import me.utk.spigot_scripting.plugin.command.PluginTabCompleter;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.util.FileUtil;
import me.utk.util.function.lambda.Lambda2;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;

import static me.utk.spigot_scripting.plugin.command.PluginTabCompleter.ChangelogHandler;
import static me.utk.spigot_scripting.plugin.command.PluginTabCompleter.ChangelogHandler.ChangelogSection;

public class PluginMain extends JavaPlugin {
    public static final String PLUGIN_NAME = "Utk Script Loader Plugin";
    public static final String VERSION = "pre3.0.0-1.16.4-rev4";

    public static final String PATH = FileUtil.PROJECT_CLASS_PATH + "plugin.PluginMain";

    public static PluginMain INSTANCE;
    public static final Set<OfflinePlayer> ALL_PLAYERS = new HashSet<>(30);

    @Override
    public void onEnable() {
        // Set instance variable
        INSTANCE = this;

        // Call extracted helper method
        doOnEnableAlways(false);

        // Load changelog
        loadChangelog();

        // Enable plugin command handlers
        {
            PluginCommand pluginCommand = Objects.requireNonNull(getCommand("usl"));
            pluginCommand.setExecutor(new PluginCommandExecutor());
            pluginCommand.setTabCompleter(new PluginTabCompleter());
        }
    }

    private void doOnEnableAlways(boolean forceScriptReload) {
        // Save config if missing
        saveDefaultConfig();

        // Reload config to newest values
        reloadConfig();

        // Get error-logging config
        ErrorLogger.printStackTrace = getConfig().getBoolean("Print Debug Output", false);

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
            String reloadConfigPath = "Reload Scripts";
            if (forceScriptReload || getConfig().getBoolean(reloadConfigPath, true)) {
                reloadScripts();
                getConfig().set(reloadConfigPath, false);

                // Save config to file
                try {
                    getConfig().save(new File(getDataFolder(), "config.yml"));
                } catch (IOException e) {
                    ErrorLogger.logError(() -> "Error while saving config file", e);
                }
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

        // Clear instance variable
        INSTANCE = null;
    }

    private void doOnDisableAlways() {
        // Send script termination event
        ScriptTerminationWrapper.handleEvent();

        // Destroy scripts
        Main.clearScriptResidues();

        // Clear all_players set
        ALL_PLAYERS.clear();
    }

    public void reloadPlugin() {
        // Disable plugin
        doOnDisableAlways();

        // Enable plugin
        doOnEnableAlways(true); // true = force reload scripts
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
                                section.setHeader(formatColors(line, ChangelogSection::formatHeader));
                                setHeader = true;
                            } else
                                section.appendLine(formatColors(line, ChangelogSection::formatBody));
                        } else if (setHeader) {
                            ChangelogHandler.appendVersionLog(version, section);
                            section = new ChangelogSection();
                            setHeader = false;
                        }
                    }
                    if (setHeader)
                        ChangelogHandler.appendVersionLog(version, section);
                } catch (Exception e) {
                    ErrorLogger.logError(() -> "Error while loading " + version + " changelog", e);
                }

        } catch (Exception e) {
            ErrorLogger.logError(() -> "Error while reading changelog files", e);
        }
    }

    private String formatColors(String line, Lambda2<String, String, ChatColor[]> formatter) {
        StringBuilder builder = new StringBuilder();
        String[] split = line.split("<\\w*>");
        char[] arr = line.toCharArray();
        int charInd = 0;
        List<ChatColor> colors = new LinkedList<>();
        ChatColor[] template = {};
        boolean first = true;
        for (String s : split) {
            if (!first) {
                StringBuilder helper = new StringBuilder("" + arr[charInd]);
                while (arr[++charInd] != '>')
                    helper.append(arr[charInd]);
                helper.append(arr[charInd++]);
                ChatColor col = ChangelogSection.toChatColor(helper.toString());
                if (col == null)
                    colors.clear();
                else colors.add(col);
            }
            if (!s.isEmpty()) {
                builder.append(colors.isEmpty() ? s : formatter.get(s, colors.toArray(template)));
                charInd += s.length();
            }
            first = false;
        }
        return builder.toString().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
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
                ErrorLogger.logError(() -> "Error while reloading default scripts", e);
            }

        StringBuilder logBuilder = new StringBuilder("Default Script Loading Manifest (for console/logs):");
        for (String orderedFile : new TreeSet<>(done))
            logBuilder.append("\n").append(" - ").append(orderedFile);
        System.out.println(logBuilder);
    }
}
