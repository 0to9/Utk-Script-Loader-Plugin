package io.github.utk003.spigot_scripting.script.elements;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Script extends DataHolder {
    public final String FILE_PATH;
    public final String UNIQUE_SCRIPT_ID;
    public Script(String filePath) {
        FILE_PATH = filePath;
        UNIQUE_SCRIPT_ID = filePath.replaceAll("\\.txt", "")    // remove .txt from end
                .replaceAll("[.]", "dOt")                       // replace all .s with 'dOt'
                .replaceAll("/", "_sLaSh_")                     // replace all /s with '_sLaSh_'
                .replaceAll("[^\\w]", "_");                     // remove all non-word characters
    }

    public final Map<String, Script> INCLUDED_SCRIPTS = new HashMap<>();
    public final List<Hook> LINKED_CALLBACKS = new LinkedList<>();
    public final List<Command> COMMAND_HANDLERS = new LinkedList<>();
    public final Map<String, Struct> DEFINED_STRUCTURES = new HashMap<>();

    public final Map<String, String> CLASS_REFERENCES = new HashMap<>();

    @Override
    public String toString() {
        return "(script) " + FILE_PATH;
    }

    @Override
    public void print(PrintStream out) {
        out.println("(script) {");

        for (Map.Entry<String, Script> e : INCLUDED_SCRIPTS.entrySet())
            out.println("#include " + e.getKey() + " \"" + e.getValue().FILE_PATH + "\"");
        out.println();

        for (Hook h : LINKED_CALLBACKS)
            h.print(out);

        super.print(out);
        out.println();

        for (Map.Entry<String, Struct> e : DEFINED_STRUCTURES.entrySet()) {
            out.println();
            e.getValue().print(out, e.getKey());
        }

        out.println("}");
    }
}
