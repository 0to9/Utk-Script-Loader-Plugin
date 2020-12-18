package me.utk.spigot_scripting.script.elements;

import java.io.PrintStream;

/**
 * Any interactions with the script loader or
 * the Spigot API (via hooks)
 * <p>
 * Usage syntax (for events):
 * <pre>
 * &#64;EventID : {
 *     // code to do
 * }
 * </pre>
 *
 * @author Utkarsh Priyam
 */
public class Hook extends CodeHolder {
    public final String HOOK_ID;
    public Hook(String id, String code) {
        super(null, code);
        HOOK_ID = id;
    }

    public void print(PrintStream out) {
        out.println("(hook) @" + HOOK_ID + " :" + JAVA_CODE);
    }
}
