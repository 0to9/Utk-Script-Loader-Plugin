package io.github.utk003.spigot_scripting.plugin.command;

import io.github.utk003.spigot_scripting.plugin.PluginMain;
import io.github.utk003.spigot_scripting.command.CommandUtil;
import io.github.utk003.spigot_scripting.command.SubCommandHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.function.Function;

import static io.github.utk003.spigot_scripting.command.SubCommandHandler.tabComplete;
import static org.bukkit.ChatColor.*;

public class PluginTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = args.length == 0 ? null : completeArgs(sender, args);
        return completions == null ? Collections.singletonList("") : completions;
    }

    private List<String> completeArgs(CommandSender sender, String[] args) {
        List<String> completions = new LinkedList<>();

        switch (args.length) {
            case 1:
                completions.add("help");
                completions.add("version");
                completions.add("changelog");
                completions.add("list");

                completions.addAll(SubCommandHandler.getSubCommandExecutorIDsList());

                if (PluginCommandExecutor.isAdmin(sender)) {
                    completions.add("config");
                    completions.add("reload");
                }
                break;

            case 2:
                switch (args[0]) {
                    case "help":
                        completions.add("help");
                        completions.add("version");
                        completions.add("changelog");
                        completions.add("list");

                        completions.addAll(SubCommandHandler.getSubCommandHelpMenuIDsList());

                        completions.add("config");
                        completions.add("reload");
                        break;

                    case "changelog":
                        completions.addAll(ChangelogHandler.VERSION_ORDER);
                        break;

                    case "reload":
                        if (PluginCommandExecutor.isAdmin(sender))
                            completions.add("confirm");
                        break;

                    case "config":
                        if (PluginCommandExecutor.isAdmin(sender)) {
                            completions.add("debug_print");
                            completions.add("install_default_scripts");
                        }

                    default:
                        break;
                }
                break;

            case 3:
                if (args[0].equals("config") && PluginCommandExecutor.isAdmin(sender))
                    switch (args[1]) {
                        case "debug_print":
                        case "reload_default_scripts":
                            completions.add("true");
                            completions.add("false");
                            break;

                        default:
                            break;
                    }
                break;

            default:
                break;
        }
        switch (args[0]) {
            case "version":
            case "changelog":
            case "list":
            case "help":
            case "config":
            case "reload":
                break;

            default:
                // add sub-command handler's tab completions iff sub-command is not a plugin default
                completions.addAll(tabComplete(sender, args[0], CommandUtil.trimArguments(args)));
                break;
        }

        return CommandUtil.purgeCompletions(args[args.length - 1], completions);
    }

    public static class ChangelogHandler {
        private static final Map<String, List<ChangelogSection>> ALL_STORED_VERSION_LOGS = new HashMap<>();
        private static final List<String> VERSION_ORDER = new LinkedList<>();

        private static final String INVALID_LOG_MESSAGE =
                "" + RESET + AQUA + ITALIC + "No changelog is available for this version";

        private static String stripVersion(String version) {
            if (!version.isEmpty() && version.charAt(0) == 'v')
                version = version.substring(1);
            return version;
        }

        public static ChangelogSection[] getVersionLog(String version) {
            List<ChangelogSection> changelog = ALL_STORED_VERSION_LOGS.get(version);
            ChangelogSection[] messages;
            if (changelog == null)
                messages = new ChangelogSection[]{
                        new ChangelogSection(INVALID_LOG_MESSAGE)
                };
            else messages = changelog.toArray(new ChangelogSection[0]);
            return messages;
        }

        private static final Function<String, List<ChangelogSection>> NEW_LIST_MAKER = k -> {
            VERSION_ORDER.add(k);
            return new LinkedList<>();
        };
        public static void appendVersionLog(String version, ChangelogSection... log) {
            List<ChangelogSection> logs = ALL_STORED_VERSION_LOGS.computeIfAbsent(version, NEW_LIST_MAKER);
            logs.addAll(Arrays.asList(log));
        }

        public static String getLogHeader(String version) {
            return "" + YELLOW + BOLD + PluginMain.PLUGIN_NAME + " Version " + stripVersion(version) + " Changelog";
        }

        public static class ChangelogSection {
            public static final String HEADER_FORMAT = "" + AQUA + ITALIC;
            public static final String BODY_FORMAT = "" + WHITE;

            private String header;
            private final List<String> body;

            public ChangelogSection(String... header) {
                body = new LinkedList<>();
                setHeader(header);
            }

            public void setHeader(String... header) {
                StringBuilder builder = new StringBuilder();
                for (String s : header)
                    builder.append(s);
                this.header = builder.toString();
            }
            public void appendLine(String... line) {
                StringBuilder builder = new StringBuilder();
                for (String s : line)
                    builder.append(s);
                body.add(builder.toString());
            }

            public String[] toMessageFormat() {
                String[] message = new String[body.size() + 1];
                message[0] = "- " + HEADER_FORMAT + header;

                String bodyIndent = "  - " + BODY_FORMAT;
                int ind = 1;
                for (Iterator<String> it = body.iterator(); it.hasNext(); ind++)
                    message[ind] = bodyIndent + it.next();
                return message;
            }
        }
    }
}
