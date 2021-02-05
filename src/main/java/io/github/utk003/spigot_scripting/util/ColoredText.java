package io.github.utk003.spigot_scripting.util;

import org.bukkit.ChatColor;

import static org.bukkit.ChatColor.*;

public abstract class ColoredText {
    private ColoredText() {
    }

    public static String parseString(String str) {
        return parseString(str, RESET);
    }
    public static String parseString(String str, ChatColor... lineDefaultFormatting) {
        StringBuilder builder = new StringBuilder();
        int lastNonReset = lineDefaultFormatting.length;
        while (lastNonReset > 0 && lineDefaultFormatting[lastNonReset - 1] != RESET)
            lastNonReset--;
        for (int i = lastNonReset; i < lineDefaultFormatting.length; i++)
            builder.append(lineDefaultFormatting[i]);
        return parseString(str, builder.toString());
    }
    public static String parseString(String line, String lineDefaultFormatting) {
        try {
            StringBuilder builder = new StringBuilder(lineDefaultFormatting);
            char[] arr = line.toCharArray();
            for (int i = 0; i < arr.length; i++)
                switch (arr[i]) {
                    case '<':
                        StringBuilder helper = new StringBuilder("<");
                        while (arr[++i] != '>')
                            helper.append(arr[i]);
                        helper.append(">");
                        ChatColor col = toChatColor(helper.toString());
                        if (col != null) builder.append(col);
                        else builder.append(RESET).append(lineDefaultFormatting);
                        break;

                    case '>':
                        throw new IllegalStateException("Unmatched '>' in colored string");

                    default:
                        builder.append(arr[i]);
                        break;
                }
            return builder.toString().replaceAll("&lt;", "<").replaceAll("&gt;", ">");
        } catch (Exception e) {
            return line;
        }
    }

    private static ChatColor toChatColor(String code) {
        switch (code) {
            case "<BLACK>":
                return BLACK;
            case "<DARK_BLUE>":
                return DARK_BLUE;
            case "<DARK_GREEN>":
                return DARK_GREEN;
            case "<DARK_AQUA>":
                return DARK_AQUA;
            case "<DARK_RED>":
                return DARK_RED;
            case "<DARK_PURPLE>":
                return DARK_PURPLE;
            case "<GOLD>":
                return GOLD;
            case "<GRAY>":
                return GRAY;
            case "<DARK_GRAY>":
                return DARK_GRAY;
            case "<BLUE>":
                return BLUE;
            case "<GREEN>":
                return GREEN;
            case "<AQUA>":
                return AQUA;
            case "<RED>":
                return RED;
            case "<LIGHT_PURPLE>":
                return LIGHT_PURPLE;
            case "<YELLOW>":
                return YELLOW;
            case "<WHITE>":
                return WHITE;
            case "<MAGIC>":
                return MAGIC;
            case "<BOLD>":
                return BOLD;
            case "<STRIKETHROUGH>":
                return STRIKETHROUGH;
            case "<UNDERLINE>":
                return UNDERLINE;
            case "<ITALIC>":
                return ITALIC;
            case "<RESET>":
                return RESET;

            case "<>":
                return null;

            default:
                throw new IllegalArgumentException(code + " is not a valid color code");
        }
    }
}
