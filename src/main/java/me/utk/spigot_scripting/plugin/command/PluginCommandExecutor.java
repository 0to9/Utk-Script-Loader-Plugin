package me.utk.spigot_scripting.plugin.command;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.command.SubCommandHandler;
import me.utk.spigot_scripting.plugin.PluginMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

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
                // give general info on how stuff works
                // use 'subCommands' (below) to print list of all loaded commands
                // specify that *most* sub-commands will provide their own help menus
                String subCommands = SubCommandHandler.getSubCommandIDs();
                subCommands += "version, changelog, and help";
                break;

            case "version":
                if (args.length > 1)
                    CommandUtil.sendWarningMessage(sender, label + " version' takes no additional arguments");

                CommandUtil.sendMessage(sender, PluginMain.PLUGIN_NAME + ": v" + PluginMain.VERSION);
                break;

            case "changelog":
                if (args.length > 2)
                    CommandUtil.sendWarningMessage(sender, label + " changelog' takes up to 1 additional argument");

                String version = args.length == 1 ? "v" + PluginMain.VERSION : args[1];

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

            default:
                SubCommandHandler.executeCommand(sender, label, args);
                break;
        }
        return true;
    }
}
