package me.utk.spigot_scripting.script.elements;

import java.io.PrintStream;

public class Struct extends DataHolder {
    public final boolean RUNNABLE;
    public Struct(boolean isRunnable) {
        RUNNABLE = isRunnable;
    }

    public void print(PrintStream out, String id) {
        out.println((RUNNABLE ? "(callback) " : "(struct) ") + id + " {");
        super.print(out);
        out.println("}");
    }
}
