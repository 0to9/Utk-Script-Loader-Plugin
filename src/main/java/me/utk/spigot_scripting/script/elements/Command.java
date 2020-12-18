package me.utk.spigot_scripting.script.elements;

import java.io.PrintStream;

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
    public final String COMMAND_ID;
    public final boolean IS_EXECUTOR;
    public Command(String id, String code, boolean isExecutor) {
        super(null, code);
        COMMAND_ID = id.toLowerCase();
        IS_EXECUTOR = isExecutor;
    }

    public void print(PrintStream out) {
        out.println("(command) $" + COMMAND_ID + " :" + JAVA_CODE);
    }
}
