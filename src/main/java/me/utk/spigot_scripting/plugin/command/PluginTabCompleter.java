package me.utk.spigot_scripting.plugin.command;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.command.SubCommandHandler;
import me.utk.spigot_scripting.plugin.PluginMain;
import me.utk.util.function.lambda.Lambda0;
import me.utk.util.function.lambda.Lambda1;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;
import java.util.function.Function;

import static me.utk.spigot_scripting.command.SubCommandHandler.tabComplete;
import static org.bukkit.ChatColor.*;

public class PluginTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = args.length == 0 ? null : completeArgs(sender, args);
        return completions == null ? Collections.singletonList("") : completions;
    }

    private List<String> completeArgs(CommandSender sender, String[] args) {
        List<String> completions = new LinkedList<>(), otherComps = tabComplete(sender, args[0], CommandUtil.trimArguments(args));

        switch (args.length) {
            case 1:
                completions.add("help");
                completions.add("version");
                completions.add("changelog");
                completions.add("list");

                completions.addAll(SubCommandHandler.getSubCommandIDsList());

                if (PluginCommandExecutor.canReload(sender))
                    completions.add("reload");
                break;

            case 2:
                switch (args[0]) {
                    case "changelog":
                        completions.addAll(ChangelogHandler.VERSION_ORDER);
                        break;

                    case "reload":
                        if (PluginCommandExecutor.canReload(sender))
                            completions.add("confirm");
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
        if (otherComps != null)
            completions.addAll(otherComps);

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

            public static String formatHeader(String text, ChatColor... formatting) {
                return format(text, formatting) + HEADER_FORMAT;
            }
            public static String formatBody(String text, ChatColor... formatting) {
                return format(text, formatting) + BODY_FORMAT;
            }
            private static String format(String text, ChatColor... formatting) {
                StringBuilder builder = new StringBuilder();
                for (ChatColor format : formatting)
                    builder.append(format);
                builder.append(text);
                builder.append(ChatColor.RESET);
                return builder.toString();
            }

            public static ChatColor toChatColor(String code) {
                switch (code) {
                    case "<BLACK>": return BLACK;
                    case "<DARK_BLUE>": return DARK_BLUE;
                    case "<DARK_GREEN>": return DARK_GREEN;
                    case "<DARK_AQUA>": return DARK_AQUA;
                    case "<DARK_RED>": return DARK_RED;
                    case "<DARK_PURPLE>": return DARK_PURPLE;
                    case "<GOLD>": return GOLD;
                    case "<GRAY>": return GRAY;
                    case "<DARK_GRAY>": return DARK_GRAY;
                    case "<BLUE>": return BLUE;
                    case "<GREEN>": return GREEN;
                    case "<AQUA>": return AQUA;
                    case "<RED>": return RED;
                    case "<LIGHT_PURPLE>": return LIGHT_PURPLE;
                    case "<YELLOW>": return YELLOW;
                    case "<WHITE>": return WHITE;
                    case "<MAGIC>": return MAGIC;
                    case "<BOLD>": return BOLD;
                    case "<STRIKETHROUGH>": return STRIKETHROUGH;
                    case "<UNDERLINE>": return UNDERLINE;
                    case "<ITALIC>": return ITALIC;
                    case "<RESET>": return RESET;

                    default: return null;
                }
            }
        }
    }
}
