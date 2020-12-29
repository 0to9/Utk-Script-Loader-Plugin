package me.utk.spigot_scripting.plugin.command;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.command.SubCommandHandler;
import me.utk.spigot_scripting.plugin.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import static me.utk.spigot_scripting.plugin.command.PluginTabCompleter.ChangelogHandler.*;

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
                if (args.length > 1)
                    CommandUtil.sendWarningMessage(sender, label + " help' takes no additional arguments");

                // TODO implement help menu
                CommandUtil.sendMessage(sender, PluginMain.PLUGIN_NAME + " Help Menu WIP");

            case "list":
                if (args.length > 1)
                    CommandUtil.sendWarningMessage(sender, label + " list' takes no additional arguments");

                String subCommands = SubCommandHandler.getSubCommandIDs();
                subCommands += "version, changelog, list, and help.";
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

                String logHeader = getLogHeader(version);
                ChangelogSection[] log = getVersionLog(version);

                sender.sendMessage(logHeader);
                for (ChangelogSection section : log) {
                    String[] message = section.toMessageFormat();
                    for (String msg : message)
                        sender.sendMessage(msg);
                    sender.sendMessage("");
                }
                break;

            case "reload":
                if (!canReload(sender)) {
                    CommandUtil.sendErrorMessage(sender, "Only console can use " + label + " reload'");
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
                    // TODO make this reload all script classes as well
                    PluginMain.INSTANCE.reloadScripts();
                    CommandUtil.sendMessage(sender, ChatColor.GREEN + "Default scripts reloaded");
                } else {
                    CommandUtil.sendWarningMessage(sender, label + " reload' will reload all default scripts and may override customizations");
                    CommandUtil.sendWarningMessage(sender, "To confirm reload, use the command " + label + " reload confirm'");
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

    public static boolean canReload(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || (sender instanceof Player && sender.isOp());
    }
}
