package me.utk.spigot_scripting.loader_linker;

import javassist.*;
import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.script.ScriptParser;
import me.utk.spigot_scripting.script.elements.*;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.event.EventsUtil;
import me.utk.spigot_scripting.util.FileUtil;
import me.utk.spigot_scripting.util.exception.CompilingException;
import me.utk.spigot_scripting.util.exception.LinkingException;
import me.utk.util.data.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ScriptLinker {
    public static final String GENERATED_CLASSES_PACKAGE = FileUtil.PROJECT_CLASS_PATH + "loaded";

    public static void linkScripts() {
        try {
            ScriptLoader.SCRIPT_LOCKER.acquire();

            Map<DataHolder, Pair<String, CtClass>> classMap = new HashMap<>();
            Map<CodeHolder, CtMember> memberMap = new HashMap<>();

            ClassPool classPool = ClassPool.getDefault();
            for (String pack : ScriptParser.IMPORTED_PACKAGES)
                classPool.importPackage(pack);
            for (String pack : EventsUtil.EVENT_PACKAGES)
                classPool.importPackage(pack);
            classPool.importPackage(ScriptLinker.GENERATED_CLASSES_PACKAGE);

            for (Script script : ScriptLoader.LOADED_SCRIPTS) {
                // Define script
                String classPath = GENERATED_CLASSES_PACKAGE + "." + script.UNIQUE_SCRIPT_ID;
                classMap.put(script, new Pair<>(script.UNIQUE_SCRIPT_ID, classPool.makeClass(classPath)));

                // Define nested structs
                for (Map.Entry<String, Struct> e : script.DEFINED_STRUCTURES.entrySet()) {
                    String p1 = classPath + "$" + e.getKey(), p2 = script.UNIQUE_SCRIPT_ID + "$" + e.getKey();
                    classMap.put(e.getValue(), new Pair<>(p2, classPool.makeClass(p1)));
                }
            }
            for (Script script : ScriptLoader.LOADED_SCRIPTS) {
                // Declare script (static) methods
                for (Map.Entry<String, Function> e : script.DEFINED_FUNCTIONS.entrySet())
                    memberMap.put(e.getValue(), getMethod(e.getValue(), e.getKey(), script, script, classMap));

                // Declare nested struct (instance) methods
                for (Struct struct : script.DEFINED_STRUCTURES.values())
                    for (Map.Entry<String, Function> e : struct.DEFINED_FUNCTIONS.entrySet())
                        memberMap.put(e.getValue(), getMethod(e.getValue(), e.getKey(), script, struct, classMap));

                // Declare script (static) hooks
                for (Hook hook : script.LINKED_CALLBACKS)
                    memberMap.put(hook, getHookMethod(hook, script, classMap));

                // Declare script (static) command handlers
                for (Command command : script.COMMAND_HANDLERS)
                    memberMap.put(command, getCommandMethod(command, script, classMap));
            }
            for (Script script : ScriptLoader.LOADED_SCRIPTS) {
                // Declare and define script (static) methods
                for (Map.Entry<String, Variable> e : script.DEFINED_VARIABLES.entrySet())
                    memberMap.put(e.getValue(), getField(e.getValue(), e.getKey(), script, script, classMap));

                // Declare and define nested struct (instance) variables
                for (Struct struct : script.DEFINED_STRUCTURES.values())
                    for (Map.Entry<String, Variable> e : struct.DEFINED_VARIABLES.entrySet())
                        memberMap.put(e.getValue(), getField(e.getValue(), e.getKey(), script, struct, classMap));
            }
            for (Script script : ScriptLoader.LOADED_SCRIPTS) {
                // Define script (static) methods
                for (Map.Entry<String, Function> e : script.DEFINED_FUNCTIONS.entrySet())
                    defineMethod(e.getValue(), (CtBehavior) memberMap.get(e.getValue()), script, classMap);

                // Define nested struct (instance) methods
                for (Struct struct : script.DEFINED_STRUCTURES.values())
                    for (Map.Entry<String, Function> e : struct.DEFINED_FUNCTIONS.entrySet())
                        defineMethod(e.getValue(), (CtBehavior) memberMap.get(e.getValue()), script, classMap);

                // Define script (static) hooks
                for (Hook hook : script.LINKED_CALLBACKS)
                    defineHookMethod(hook, (CtMethod) memberMap.get(hook), script, classMap);

                // Define script (static) command handlers
                for (Command command : script.COMMAND_HANDLERS)
                    defineCommandMethod(command, (CtMethod) memberMap.get(command), script, classMap);
            }
            for (Pair<String, CtClass> pair : classMap.values())
                pair.second.toClass();

            ScriptLoader.SCRIPT_LOCKER.release();
        } catch (Exception e) {
            ErrorLogger.logError(() -> "Encountered an error while linking scripts", e);
        }
    }

    private static CtBehavior getMethod(Function func, String id, Script enclosingScript, DataHolder holder,
                                        Map<DataHolder, Pair<String, CtClass>> classMap) throws Exception {
        if ("init".equals(func.TYPE)) { // is constructor
            String arguments = resolveArguments(func.ARGUMENTS, enclosingScript, classMap);

            CtConstructor constructor = CtNewConstructor.make("public " + id + arguments + "{ }", classMap.get(holder).second);
            classMap.get(holder).second.addConstructor(constructor);
            return constructor;
        } else { // is a normal method
            String retType = resolveDataType(func.TYPE, enclosingScript, classMap);
            String arguments = resolveArguments(func.ARGUMENTS, enclosingScript, classMap);
            String modifiers = "public ";
            if (enclosingScript == holder)
                modifiers += "static ";
            String body = "{ return " + getDefaultReturnValue(retType) + "; }";

            CtMethod method = CtNewMethod.make(modifiers + retType + " " + id + arguments + body, classMap.get(holder).second);
            classMap.get(holder).second.addMethod(method);
            return method;
        }
    }

    private static CtMethod getHookMethod(Hook hook, Script script, Map<DataHolder, Pair<String, CtClass>> classMap) throws Exception {
        String hookMethodName = "hoooooookOn" + hook.HOOK_ID;

        CtMethod method = CtNewMethod.make("public static void " + hookMethodName +
                "(" + EventsUtil.getEventClassName(hook.HOOK_ID) + " event) { }", classMap.get(script).second);

        Pair<String, CtClass> holder = classMap.get(script);
        holder.second.addMethod(method);
        EventsUtil.addHandlerCodeToEventHandler(hook.HOOK_ID, holder.first + "." + hookMethodName);

        return method;
    }

    private static CtMethod getCommandMethod(Command command, Script script,
                                             Map<DataHolder, Pair<String, CtClass>> classMap) throws Exception {
        String commandMethodName = "subCommandHandler_" + command.COMMAND_ID;
        String returnType;
        if (command.IS_EXECUTOR) {
            commandMethodName += "Executor";
            returnType = "void";
        } else {
            commandMethodName += "TabCompleter";
            returnType = "List";
        }

        CtMethod method = CtNewMethod.make("public static " + returnType + " " + commandMethodName +
                "(org.bukkit.command.CommandSender sender, String[] args) { return " +
                getDefaultReturnValue(returnType) + "; }", classMap.get(script).second);

        Pair<String, CtClass> holder = classMap.get(script);
        holder.second.addMethod(method);
        String methodCallCode = holder.first + "." + commandMethodName;
        if (command.IS_EXECUTOR)
            CommandUtil.addSubCommandExecutor(command.COMMAND_ID, methodCallCode);
        else
            CommandUtil.addSubCommandTabCompleter(command.COMMAND_ID, methodCallCode);

        return method;
    }

    private static String getDefaultReturnValue(String returnType) {
        switch (returnType) {
            case "void":
                return "";

            case "byte":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
            case "char":
            case "boolean":
                return "(" + returnType + ") 0";

            default:
                return "null";
        }
    }

    private static CtField getField(Variable var, String id, Script enclosingScript, DataHolder holder,
                                    Map<DataHolder, Pair<String, CtClass>> classMap) throws Exception {
        String code = "public ";
        if (enclosingScript == holder)
            code += "static ";
        code += resolveDataType(var.TYPE, enclosingScript, classMap) + " " + id;
        if (var.JAVA_CODE != null)
            code += " = " + resolveCode(var.JAVA_CODE, enclosingScript, classMap);
        code += ";";

        CtField field = CtField.make(code, classMap.get(holder).second);
        classMap.get(holder).second.addField(field);
        return field;
    }

    private static void defineMethod(Function func, CtBehavior method, Script script,
                                     Map<DataHolder, Pair<String, CtClass>> classMap) throws Exception {
        String[] args = func.ARGUMENTS.split(" ");
        Map<String, Integer> paramMap = new HashMap<>(args.length / 2);
        for (int i = 2; i < args.length; i += 3) // param id is every third element
            paramMap.put(args[i], i / 3 + 1);

        String code = resolveCode(func.JAVA_CODE, script, classMap, paramMap);
        method.setBody(code);
    }
    private static final Map<String, Integer> HOOK_ARGUMENTS_MAP = Collections.singletonMap("wrapper", 1);
    private static void defineHookMethod(Hook hook, CtMethod method, Script script,
                                         Map<DataHolder, Pair<String, CtClass>> classMap) throws Exception {
        String code = resolveCode(hook.JAVA_CODE, script, classMap, HOOK_ARGUMENTS_MAP);
        method.setBody(code);
    }
    private static final Map<String, Integer> COMMAND_ARGUMENTS_MAP = new HashMap<>();
    static {
        COMMAND_ARGUMENTS_MAP.put("sender", 1);
        COMMAND_ARGUMENTS_MAP.put("args", 2);
    }
    private static void defineCommandMethod(Command command, CtMethod method, Script script,
                                            Map<DataHolder, Pair<String, CtClass>> classMap) throws Exception {

        String code = resolveCode(command.JAVA_CODE, script, classMap, COMMAND_ARGUMENTS_MAP);
        method.setBody(code);
    }

    private static String resolveArguments(String args, Script script, Map<DataHolder, Pair<String, CtClass>> classMap) {
        String[] splitArgs = args.trim().split(" ");
        for (int i = 1; i < splitArgs.length; i += 3) // datatype is every third element
            splitArgs[i] = resolveDataType(splitArgs[i], script, classMap);

        StringBuilder builder = new StringBuilder();
        for (String arg : splitArgs)
            builder.append(" ").append(arg);
        return builder.toString();
    }

    private static String resolveDataType(String type, Script script, Map<DataHolder, Pair<String, CtClass>> classMap) {
        String[] split = type.split("::");
        Script current = script;
        int lastInd = split.length - 1;
        for (int j = 0; j < lastInd; j++) {
            current = current.INCLUDED_SCRIPTS.get(split[j]);
            if (current == null)
                return type;
        }
        Struct struct = current.DEFINED_STRUCTURES.get(split[lastInd]);
        return struct != null ? classMap.get(struct).first : type;
    }
    private static String resolveCode(String code, Script script, Map<DataHolder, Pair<String, CtClass>> classMap) {
        return resolveCode(code, script, classMap, Collections.emptyMap());
    }
    private static String resolveCode(String code, Script script, Map<DataHolder, Pair<String, CtClass>> classMap,
                                      Map<String, Integer> paramMap) {
        // Prevents scripts from accessing classes they shouldn't see or use
        if (code.contains(FileUtil.PROJECT_PACKAGE))
            throw new LinkingException("Script in \"" + script.FILE_PATH +
                    "\" is trying to access classes from the \"" + FileUtil.PROJECT_PACKAGE + "\" package");

        // TODO better resolution system that can deal with stuff stuck together (like blah::blah+=blah)
        StringBuilder builder = new StringBuilder();
        Tokenizer st = new Tokenizer(code);
        Boolean prevParsedInclusionState = null;
        while (st.hasMoreTokens()) {
            Boolean currParsedInclusionState = null;
            String token = st.nextToken();
            switch (token) {
                case "$":
                    String tok;
                    switch (tok = st.nextToken()) {
                        case "PATH":
                            builder.append("\"").append(script.FILE_PATH).append("\"");
                            break;

                        case "getRelative":
                            builder.append(FileUtil.PATH + ".cleanFilePath");

                            check(st, " ", script, "getRelative");
                            check(st, "(", script, "getRelative");
                            check(st, " ", script, "getRelative");

                            builder.append("(\"");
                            builder.append(FileUtil.getParentDirectory(script.FILE_PATH));
                            builder.append("\" + ");
                            // rest of switch case can handle strings
                            break;

                        case "ifIncluded":
                        case "ifNotIncluded":
                            check(st, " ", script, tok);
                            check(st, "(", script, tok);
                            check(st, " ", script, tok);
                            boolean scriptIsIncluded = script.INCLUDED_SCRIPTS.containsKey(st.nextToken());
                            check(st, " ", script, tok);
                            check(st, ")", script, tok);

                            // hack to reuse code inclusion code from $else
                            currParsedInclusionState = scriptIsIncluded == tok.equals("ifIncluded");
                            prevParsedInclusionState = currParsedInclusionState;

                        case "else":
                            check(st, " ", script, tok);
                            check(st, "{", script, tok);

                            if (prevParsedInclusionState == null)
                                throw new CompilingException("Script in \"" + script.FILE_PATH + "\" has a " +
                                        "dangling $else directive");
                            if (prevParsedInclusionState) {
                                StringBuilder helper = new StringBuilder();
                                int braceCount = 1;
                                while (braceCount > 0) {
                                    switch (tok = st.nextToken()) {
                                        case "{":
                                            braceCount++;
                                            break;

                                        case "}":
                                            braceCount--;
                                            break;

                                        default:
                                            break;
                                    }
                                    helper.append(tok);
                                }
                                builder.append(resolveCode(helper.substring(0, helper.length() - 1), script, classMap, paramMap));
                            } else {
                                int braceCount = 1;
                                while (braceCount > 0)
                                    switch (st.nextToken()) {
                                        case "{":
                                            braceCount++;
                                            break;

                                        case "}":
                                            braceCount--;
                                            break;

                                        default:
                                            break;
                                    }
                            }
                            check(st, " ", script, tok);
                            break;

                        default:
                            throw new CompilingException("Script in \"" + script.FILE_PATH + "\" has a " +
                                    "unrecognized directive: $" + tok);
                    }
                    break;

                case "'":
                case "\"":
                    builder.append(token);
                    String nextToken = st.nextToken();
                    while (!nextToken.equals(token)) {
                        builder.append(nextToken);
                        if (nextToken.equals("\\"))
                            builder.append(st.nextToken());
                        nextToken = st.nextToken();
                    }
                    builder.append(nextToken);
                    break;

                default:
                    String t2 = resolveReference(token, script, classMap);
                    Integer mapping = paramMap.get(token);
                    builder.append(token.equals(t2) && mapping != null ? "$" + mapping : t2);
                    break;
            }
            // boolean for $else support/handling
            prevParsedInclusionState = currParsedInclusionState == null ? null : !currParsedInclusionState;
        }
        return builder.toString();
    }

    private static void check(Tokenizer tok, String target, Script script, String directiveID) {
        if (!target.equals(tok.nextToken()))
            throw new CompilingException("Script in \"" + script.FILE_PATH + "\" has a " +
                    "incorrectly formatted '$" + directiveID + "' directive");
    }

    private static String resolveReference(String ref, Script script, Map<DataHolder, Pair<String, CtClass>> classMap) {
        String[] split = ref.split("::");
        Script current = script;
        int lastInd = split.length - 1;
        for (int j = 0; j < lastInd; j++) {
            current = current.INCLUDED_SCRIPTS.get(split[j]);
            if (current == null)
                return ref;
        }

        Struct struct = current.DEFINED_STRUCTURES.get(split[lastInd]);
        if (struct != null)
            return classMap.get(struct).first;

        // static function or variable
        if (current.DEFINED_VARIABLES.containsKey(split[lastInd]) || current.DEFINED_FUNCTIONS.containsKey(split[lastInd]))
            return classMap.get(current).first + "." + split[lastInd];

        return ref;
    }

    private static class Tokenizer {
        private final char[] arr;
        private int index;

        public Tokenizer(String str) {
            arr = str.toCharArray();
            index = 0;
        }

        public boolean hasMoreTokens() {
            return index < arr.length;
        }
        public String nextToken() {
            if (!isIDChar(arr[index]))
                return "" + arr[index++];

            StringBuilder builder = new StringBuilder();
            while (isIDChar(arr[index]))
                if (arr[index] == ':' && arr[index - 1] != ':' && arr[index + 1] != ':') {
                    if (builder.length() == 0)
                        builder.append(arr[index++]);
                    break;
                } else
                    builder.append(arr[index++]);
            return builder.toString();
        }
        private boolean isIDChar(char c) {
            return '0' <= c && c <= '9' || 'a' <= c && c <= 'z' ||
                    'A' <= c && c <= 'Z' || c == ':' || c == '_';
        }
    }
}
