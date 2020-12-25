package me.utk.spigot_scripting.plugin.command;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.plugin.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

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
                break;

            case 2:
                if (args[0].equals("changelog"))
                    completions.addAll(ChangelogHandler.ALL_STORED_VERSION_LOGS.keySet());
                break;

            default:
                break;
        }
        if (otherComps != null)
            completions.addAll(otherComps);

        return CommandUtil.purgeCompletions(args[args.length - 1], completions);
    }

    public static class ChangelogHandler {
        private static final Map<String, ChangelogSection[]> ALL_STORED_VERSION_LOGS = new HashMap<>();

        private static final String INVALID_LOG_MESSAGE =
                "" + RESET + YELLOW + ITALIC + "No changelog is available for version ";

        private static String[] formatVersion(String version) {
            String stripped;
            if (version.isEmpty() || version.charAt(0) != 'v') {
                stripped = version;
                version = "v" + version;
            } else
                stripped = version.substring(1);

            return new String[]{stripped, version};
        }

        public static ChangelogSection[] getVersionLog(String version) {
            String[] vers = formatVersion(version);

            ChangelogSection[] changelog = ALL_STORED_VERSION_LOGS.get(vers[1]);
            if (changelog == null)
                changelog = new ChangelogSection[]{
                        new ChangelogSection(INVALID_LOG_MESSAGE + vers[0])
                };
            return changelog;
        }

        public static void updateVersionLog(String version, ChangelogSection... log) {
            ALL_STORED_VERSION_LOGS.put(formatVersion(version)[1], log);
        }
        public static void appendVersionLog(String version, ChangelogSection... log) {
            version = formatVersion(version)[1];

            ChangelogSection[] oldLog = ALL_STORED_VERSION_LOGS.get(version);
            int oldLen = oldLog == null ? 0 : oldLog.length;

            ChangelogSection[] newLog = new ChangelogSection[oldLen + log.length];
            if (oldLog != null)
                System.arraycopy(oldLog, 0, newLog, 0, oldLen);
            System.arraycopy(log, 0, newLog, oldLen, log.length);

            ALL_STORED_VERSION_LOGS.put(version, newLog);
        }

        public static String getLogHeader(String version) {
            return "" + YELLOW + BOLD + PluginMain.PLUGIN_NAME + " Version " + formatVersion(version)[0] + " Changelog";
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
        }
    }
}
