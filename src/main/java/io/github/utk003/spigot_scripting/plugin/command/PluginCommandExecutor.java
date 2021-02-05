package io.github.utk003.spigot_scripting.plugin.command;

import io.github.utk003.spigot_scripting.plugin.PluginMain;
import io.github.utk003.spigot_scripting.command.CommandUtil;
import io.github.utk003.spigot_scripting.command.SubCommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PluginCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        label = "`/" + label;
        if (args.length == 0) {
            CommandUtil.sendErrorMessage(sender, label + "' should be called with arguments");
            return true;
        }

        switch (args[0]) {
            case "help":
                if (args.length == 1) {
                    CommandUtil.sendMessage(sender, label + "' is the main command for the " + PluginMain.PLUGIN_NAME);
                    CommandUtil.sendMessage(sender, "The plugin is open-sourced on github (" + PluginMain.GITHUB_URL + ")");
                    CommandUtil.sendMessage(sender, "To get information about a specific sub-command, use the command " + label + " help <sub-command>'");
                } else {
                    switch (args[1]) {
                        case "version":
                            CommandUtil.sendMessage(sender, label + " version' takes no additional arguments");
                            CommandUtil.sendMessage(sender, "It prints the currently running version of the " + PluginMain.PLUGIN_NAME);
                            break;

                        case "changelog":
                            CommandUtil.sendMessage(sender, label + " changelog' takes up to 1 additional argument");
                            CommandUtil.sendMessage(sender, "It prints the full changelog for the specified version, or the current one if no version is specified");
                            break;

                        case "list":
                            CommandUtil.sendMessage(sender, label + " list' takes no additional arguments");
                            CommandUtil.sendMessage(sender, "It lists all currently available sub-commands for " + label + "'");
                            break;

                        case "help":
                            CommandUtil.sendMessage(sender, label + " help' takes up to 1 additional argument");
                            CommandUtil.sendMessage(sender, "It prints a help-menu for the specified sub-command, or a generic plugin help menu if no sub-command is specified");
                            break;

                        case "config":
                            CommandUtil.sendMessage(sender, label + " config' takes exactly 2 additional arguments");
                            CommandUtil.sendMessage(sender, "It allows admin users to modify the plugin's configuration settings in 'config.yml'");
                            break;

                        case "reload":
                            CommandUtil.sendMessage(sender, label + " reload' takes at most a confirmation argument");
                            CommandUtil.sendMessage(sender, "It allows admin users to reload all script implementations");
                            break;

                        default:
                            SubCommandHandler.printHelp(sender, args[1], CommandUtil.trimArguments(2, args));
                            break;
                    }
                    break;
                }
                // if args.length == 1, pass through into "list" case

            case "list":
                if (args.length > 1)
                    CommandUtil.sendWarningMessage(sender, label + " list' takes no additional arguments");

                String subCommands = SubCommandHandler.getSubCommandExecutorIDs();
                subCommands += "version, changelog, list, help, config, and reload.";
                CommandUtil.sendMessage(sender,
                        ChatColor.DARK_AQUA + "The available sub-commands for " + label + "' are " + subCommands);
                break;

            case "version":
                if (args.length > 1)
                    CommandUtil.sendWarningMessage(sender, label + " version' takes no additional arguments");

                CommandUtil.sendMessage(sender, ChatColor.BLUE + PluginMain.PLUGIN_NAME + ": " + PluginMain.VERSION);
                break;

            case "changelog":
                if (args.length > 2)
                    CommandUtil.sendWarningMessage(sender, label + " changelog' takes up to 1 additional argument");

                String version = args.length == 1 ? PluginMain.VERSION : args[1];

                String logHeader = PluginTabCompleter.ChangelogHandler.getLogHeader(version);
                PluginTabCompleter.ChangelogHandler.ChangelogSection[] log = PluginTabCompleter.ChangelogHandler.getVersionLog(version);

                sender.sendMessage(logHeader);
                for (PluginTabCompleter.ChangelogHandler.ChangelogSection section : log) {
                    String[] message = section.toMessageFormat();
                    for (String msg : message)
                        sender.sendMessage(msg);
                    sender.sendMessage("");
                }
                break;

            case "config":
                if (!isAdmin(sender)) {
                    CommandUtil.sendErrorMessage(sender, "Only admin users can use " + label + " config'");
                    break;
                }

                switch (args.length) {
                    case 1:
                    case 2:
                        CommandUtil.sendErrorMessage(sender, label + " config' requires exactly 2 additional arguments");
                        break;

                    default:
                        if (args.length > 3)
                            CommandUtil.sendWarningMessage(sender, label + " config' takes exactly 2 additional arguments");

                        String configPath;
                        switch (args[1]) {
                            case "debug_print":
                                configPath = PluginMain.VERBOSE_OUTPUT_CONFIG_PATH;
                                break;

                            case "install_default_scripts":
                                configPath = PluginMain.RELOAD_DEFAULT_SCRIPTS_CONFIG_PATH;
                                break;

                            default:
                                configPath = null;
                                break;
                        }
                        if (configPath == null) {
                            CommandUtil.sendErrorMessage(sender, label + " config' requires a valid config setting to modify");
                            break;
                        }

                        switch (args[2]) {
                            case "true":
                                PluginMain.INSTANCE.setConfig(configPath, true);
                                break;

                            case "false":
                                PluginMain.INSTANCE.setConfig(configPath, false);
                                break;

                            default:
                                CommandUtil.sendErrorMessage(sender, label + " config' requires a valid boolean value to set to");
                                break;
                        }
                        break;
                }
                break;

            case "reload":
                if (!isAdmin(sender)) {
                    CommandUtil.sendErrorMessage(sender, "Only admin users can use " + label + " reload'");
                    break;
                }
                boolean reload;
                switch (args.length) {
                    case 1:
                        reload = false;
                        break;

                    case 2:
                        if (args[1].equals("confirm")) {
                            reload = true;
                            break;
                        }

                    default:
                        CommandUtil.sendWarningMessage(sender, label + " reload' only takes a confirmation argument");
                        reload = args[1].equals("confirm");
                        break;
                }
                if (reload) {
                    Bukkit.broadcastMessage(ChatColor.AQUA + PluginMain.PLUGIN_NAME + " script reload initiated by " + sender.getName());

                    PluginMain.INSTANCE.reloadPlugin(); // Also reloads scripts
                    CommandUtil.sendMessage(sender, ChatColor.GREEN + "Scripts reloaded successfully");

                    Bukkit.broadcastMessage(ChatColor.AQUA + "Script reload complete");
                } else {
                    CommandUtil.sendWarningMessage(sender, label + " reload' will reload all script implementations in use by the " + PluginMain.PLUGIN_NAME);
                    CommandUtil.sendWarningMessage(sender, "To reinstall the default scripts, use the command " + label + " config'");
                    CommandUtil.sendWarningMessage(sender, "To confirm the reload, use the command " + label + " reload confirm'");
                }
                break;

            default:
                label = args[0];
                args = CommandUtil.trimArguments(args);
                SubCommandHandler.executeCommand(sender, label, args);
                break;
        }
        return true;
    }

    public static boolean isAdmin(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || (sender instanceof Player && sender.isOp());
    }
}
