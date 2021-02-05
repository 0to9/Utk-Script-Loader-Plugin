package io.github.utk003.spigot_scripting.script.elements;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.io.PrintStream;
import java.util.Map;

public abstract class DataHolder {
    public final MultiValuedMap<String, Function> DEFINED_FUNCTIONS = new HashSetValuedHashMap<>();
    public final MultiValuedMap<String, Variable> DEFINED_VARIABLES = new HashSetValuedHashMap<>();

    public void print(PrintStream out) {
        for (Map.Entry<String, Variable> e : DEFINED_VARIABLES.entries()) {
            out.println();
            e.getValue().print(out, e.getKey());
        }
        out.println();

        for (Map.Entry<String, Function> e : DEFINED_FUNCTIONS.entries()) {
            out.println();
            e.getValue().print(out, e.getKey());
        }
    }
}
