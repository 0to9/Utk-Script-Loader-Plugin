package io.github.utk003.spigot_scripting.script.elements;

import java.io.PrintStream;
import java.util.List;

/**
 * Defining command executors and tab completers for individual modules
 * <p>
 * Usage syntax (for executors):
 * <pre>
 * $exec commandID : {
 *
 *     // code to do
 * }
 * </pre>
 * Usage syntax (for tab completers):
 * <pre>
 * $comp commandID : {
 *     // code to do
 * }
 * </pre>
 *
 * @author Utkarsh Priyam
 */
public class Command extends CodeHolder {
    public final List<String> COMMAND_IDS;
    public final HandlerType TYPE;
    public Command(List<String> ids, String code, HandlerType type) {
        super(null, code);
        COMMAND_IDS = ids;
        TYPE = type;
    }

    public void print(PrintStream out) {
        out.println("(command) $" + TYPE + " " + COMMAND_IDS + " :" + JAVA_CODE);
    }

    public enum HandlerType {
        EXECUTOR, TAB_COMPLETER, HELP_MENU;

        @Override
        public String toString() {
            switch (this) {
                case EXECUTOR:
                    return "exec";

                case TAB_COMPLETER:
                    return "comp";

                case HELP_MENU:
                    return "help";

                default:
                    return null;
            }
        }

        public static HandlerType fromID(String id) {
            switch (id) {
                case "exec":
                    return EXECUTOR;

                case "comp":
                    return TAB_COMPLETER;

                case "help":
                    return HELP_MENU;

                default:
                    throw new IllegalArgumentException("\"" + id + "\" is not a valid command handler id");
            }
        }

        public String toMethodReturnType() {
            switch (this) {
                case EXECUTOR:
                    return "void";

                case TAB_COMPLETER:
                    return "List";

                case HELP_MENU:
                    return "String[]";

                default:
                    return "";
            }
        }
    }
}
