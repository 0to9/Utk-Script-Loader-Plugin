package me.utk.spigot_scripting.script.elements;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public abstract class DataHolder {
    public final Map<String, Function> DEFINED_FUNCTIONS = new HashMap<>();
    public final Map<String, Variable> DEFINED_VARIABLES = new HashMap<>();

    public void print(PrintStream out) {
        for (Map.Entry<String, Variable> e : DEFINED_VARIABLES.entrySet()) {
            out.println();
            e.getValue().print(out, e.getKey());
        }
        out.println();

        for (Map.Entry<String, Function> e : DEFINED_FUNCTIONS.entrySet()) {
            out.println();
            e.getValue().print(out, e.getKey());
        }
    }
}
