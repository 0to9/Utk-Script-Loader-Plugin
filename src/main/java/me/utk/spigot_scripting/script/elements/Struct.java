package me.utk.spigot_scripting.script.elements;

import java.io.PrintStream;

public class Struct extends DataHolder {

    public void print(PrintStream out, String id) {
        out.println("(struct) " + id + " {");
        super.print(out);
        out.println("}");
    }
}
