package io.github.utk003.spigot_scripting.script.elements;

import java.io.PrintStream;

/**
 * Any method declared in the script.
 * <p>
 * Declaration syntax:
 * <pre>
 * return_type method_name(args...) {
 *     // code to do
 *     // code to do
 *     return val
 * }
 * </pre>
 * Usage syntax:
 * <pre>
 * method_name()
 * </pre>
 * or
 * <pre>
 * var_name = method_name()
 * </pre>
 *
 * @author Utkarsh Priyam
 */
public class Function extends CodeHolder {
    public final String ARGUMENTS;
    public Function(String type, String args, String code) {
        super(type, code);
        ARGUMENTS = args;
    }

    public void print(PrintStream out, String id) {
        out.println("(func) " + TYPE + " " + id + ARGUMENTS + JAVA_CODE);
    }
}
