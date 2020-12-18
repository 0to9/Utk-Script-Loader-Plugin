package me.utk.spigot_scripting.script.elements;

public abstract class CodeHolder {
    public final String TYPE; // variable type or method return type
    public final String JAVA_CODE;
    public CodeHolder(String type, String code) {
        TYPE = type;
        JAVA_CODE = code;
    }
}
