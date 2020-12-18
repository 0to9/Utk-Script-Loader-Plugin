package me.utk.spigot_scripting.script.elements;

import java.io.PrintStream;

public class Variable extends CodeHolder {
    public Variable(String type, String code) {
        super(type, code);
    }

    public void print(PrintStream out, String id) {
        out.println("(var) " + TYPE + " " + id + " = " + JAVA_CODE + ";");
    }
}
