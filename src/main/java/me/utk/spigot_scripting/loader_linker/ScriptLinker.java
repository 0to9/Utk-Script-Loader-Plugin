package me.utk.spigot_scripting.loader_linker;

import javassist.*;
import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.plugin.PluginMain;
import me.utk.spigot_scripting.script.ScriptParser;
import me.utk.spigot_scripting.script.elements.*;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.event.EventsUtil;
import me.utk.spigot_scripting.util.FileUtil;
import me.utk.spigot_scripting.util.exception.CompilingException;
import me.utk.spigot_scripting.util.exception.LinkingException;
import me.utk.util.data.Pair;

import java.util.*;

public class ScriptLinker {
    public static final String GENERATED_CLASSES_PACKAGE = FileUtil.PROJECT_CLASS_PATH + "loaded";

    public static final Map<CtClass, Class<?>> CLASS_CLASS_MAP = new HashMap<>();

    public static void linkScripts() {
        try {
            Map<DataHolder, Pair<String, CtClass>> classMap = new HashMap<>();
            Map<CodeHolder, CtMember> memberMap = new HashMap<>();

            Collection<Script> scriptsParsed = ScriptParser.scriptsParsed();

            ClassPool classPool = CustomClassPool.getDefault();
            for (String pack : ScriptParser.IMPORTED_PACKAGES)
                classPool.importPackage(pack);
            for (String pack : EventsUtil.EVENT_PACKAGES)
                classPool.importPackage(pack);
            classPool.importPackage(ScriptLinker.GENERATED_CLASSES_PACKAGE);

            Set<CtClass> lambdaClassSet = new HashSet<>(30);
            lambdaClassSet.add(classPool.get("java.lang.Runnable"));
            for (int i = 0; i < 10; i++) {
                lambdaClassSet.add(classPool.get("me.utk.util.function.void_lambda.VoidLambda" + i));
                lambdaClassSet.add(classPool.get("me.utk.util.function.lambda.Lambda" + i));
            }

            for (Script script : scriptsParsed) {
                // Define script
                String classPath = GENERATED_CLASSES_PACKAGE + "." + script.UNIQUE_SCRIPT_ID;
                classMap.put(script, new Pair<>(script.UNIQUE_SCRIPT_ID, classPool.makeClass(classPath)));

                // Define nested structs
                for (Map.Entry<String, Struct> e : script.DEFINED_STRUCTURES.entrySet()) {
                    String structID = e.getKey();
                    Struct struct = e.getValue();
                    String p1 = classPath + "$" + structID, p2 = script.UNIQUE_SCRIPT_ID + "$" + structID;
                    CtClass clazz = classPool.makeClass(p1);
                    if (struct.RUNNABLE)
                        for (CtClass lambdaClass : lambdaClassSet)
                            clazz.addInterface(lambdaClass);
                    classMap.put(struct, new Pair<>(p2, clazz));
                }
            }
            for (Script script : scriptsParsed) {
                // Declare script (static) methods
                for (Map.Entry<String, Function> e : script.DEFINED_FUNCTIONS.entries())
                    memberMap.put(e.getValue(), getMethod(e.getValue(), e.getKey(), script, script, classMap));

                // Declare nested struct (instance) methods
                for (Struct struct : script.DEFINED_STRUCTURES.values())
                    for (Map.Entry<String, Function> e : struct.DEFINED_FUNCTIONS.entries())
                        memberMap.put(e.getValue(), getMethod(e.getValue(), e.getKey(), script, struct, classMap));

                // Declare script (static) hooks
                for (Hook hook : script.LINKED_CALLBACKS)
                    memberMap.put(hook, getHookMethod(hook, script, classMap));

                // Declare script (static) command handlers
                for (Command command : script.COMMAND_HANDLERS)
                    memberMap.put(command, getCommandMethod(command, script, classMap));
            }
            for (Script script : scriptsParsed) {
                // Declare and define script (static) methods
                for (Map.Entry<String, Variable> e : script.DEFINED_VARIABLES.entries())
                    memberMap.put(e.getValue(), getField(e.getValue(), e.getKey(), script, script, classMap));

                // Declare and define nested struct (instance) variables
                for (Struct struct : script.DEFINED_STRUCTURES.values())
                    for (Map.Entry<String, Variable> e : struct.DEFINED_VARIABLES.entries())
                        memberMap.put(e.getValue(), getField(e.getValue(), e.getKey(), script, struct, classMap));
            }
            for (Script script : scriptsParsed) {
                // Define script (static) methods
                for (Function func : script.DEFINED_FUNCTIONS.values())
                    defineMethod(func, (CtBehavior) memberMap.get(func), script, classMap);

                // Define nested struct (instance) methods
                for (Struct struct : script.DEFINED_STRUCTURES.values())
                    for (Function func : struct.DEFINED_FUNCTIONS.values())
                        defineMethod(func, (CtBehavior) memberMap.get(func), script, classMap);

                // Define script (static) hooks
                for (Hook hook : script.LINKED_CALLBACKS)
                    defineHookMethod(hook, (CtMethod) memberMap.get(hook), script, classMap);

                // Define script (static) command handlers
                for (Command command : script.COMMAND_HANDLERS)
                    defineCommandMethod(command, (CtMethod) memberMap.get(command), script, classMap);
            }

            // Load all created classes
            for (Pair<String, CtClass> pair : classMap.values()) {
                CLASS_CLASS_MAP.put(pair.second, pair.second.toClass());
                pair.second.detach();
            }
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
                "(EventWrapper wrapper) { }", classMap.get(script).second);

        Pair<String, CtClass> holder = classMap.get(script);
        holder.second.addMethod(method);
        EventsUtil.addEventHandler(hook.HOOK_ID, holder.second, hookMethodName);

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
        if (command.IS_EXECUTOR)
            CommandUtil.addSubCommandExecutor(command.COMMAND_ID, holder.second, commandMethodName);
        else
            CommandUtil.addSubCommandTabCompleter(command.COMMAND_ID, holder.second, commandMethodName);

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
                    String tok = st.nextToken();
                    String classPathMacro = script.CLASS_REFERENCES.get(tok);
                    if (classPathMacro != null) {
                        builder.append(classPathMacro);
                        break;
                    }
                    switch (tok) {
                        case "PATH":
                            builder.append("\"").append(script.FILE_PATH).append("\"");
                            break;

                        case "FILE":
                            builder.append("\"").append(FileUtil.getFileName(script.FILE_PATH)).append("\"");
                            break;

                        case "getRelative":
                            builder.append(FileUtil.PATH).append(".cleanFilePath");

                            throwCompilationExceptionIfNoMatch(st, " ", script, "getRelative");
                            throwCompilationExceptionIfNoMatch(st, "(", script, "getRelative");
                            throwCompilationExceptionIfNoMatch(st, " ", script, "getRelative");

                            builder.append("(\"");
                            builder.append(FileUtil.getParentDirectory(script.FILE_PATH));
                            builder.append("\" + ");
                            // rest of switch case can handle strings
                            break;

                        case "PLUGIN":
                        case "JAVA_PLUGIN":
                            builder.append(PluginMain.PATH).append(".INSTANCE");
                            break;

                        case "ALL_PLAYERS":
                            builder.append(PluginMain.PATH).append(".ALL_PLAYERS");
                            break;

                        case "ifIncluded":
                        case "ifNotIncluded":
                            throwCompilationExceptionIfNoMatch(st, " ", script, tok);
                            throwCompilationExceptionIfNoMatch(st, "(", script, tok);
                            throwCompilationExceptionIfNoMatch(st, " ", script, tok);
                            boolean scriptIsIncluded = script.INCLUDED_SCRIPTS.containsKey(st.nextToken());
                            throwCompilationExceptionIfNoMatch(st, " ", script, tok);
                            throwCompilationExceptionIfNoMatch(st, ")", script, tok);

                            // hack to reuse code inclusion code from $else
                            currParsedInclusionState = scriptIsIncluded == tok.equals("ifIncluded");
                            prevParsedInclusionState = currParsedInclusionState;

                        case "else":
                            throwCompilationExceptionIfNoMatch(st, " ", script, tok);
                            throwCompilationExceptionIfNoMatch(st, "{", script, tok);

                            if (prevParsedInclusionState == null)
                                throw new CompilingException("Script in \"" + script.FILE_PATH + "\" has a " +
                                        "dangling $else directive");
                            builder.append(parseAndResolveCodeBlock(st, prevParsedInclusionState, script, classMap, paramMap));
                            throwCompilationExceptionIfNoMatch(st, " ", script, tok);
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
                    
                case "for":
                    builder.append(token);
                    builder.append(maybeParseSpace(builder, st)); // (
                    String forBody = parseAndResolveCodeBlock(st, true, script, classMap, paramMap);
                    if (forBody.contains(";")) {
                        builder.append(forBody);
                        builder.append(")");
                    } else {
                        int colonInd = forBody.indexOf(":");
                        String[] obj = forBody.substring(0, colonInd).trim().split(" "); // [type, id, ...]
                        forBody = forBody.substring(colonInd + 1);
                        builder.append("java.util.Iterator it = (").append(forBody).append(").iterator();it.hasNext();)");
                        String statement = resolveCode(parseStatement(st), script, classMap, paramMap);
                        builder.append("{").append(obj[0]).append(" ").append(obj[1])
                                .append(" = (").append(obj[0]).append(") it.next(); ");
                        builder.append(statement).append("}");
                    }
                    break;

                case ".":
                    builder.append(token);
                    builder.append(maybeParseSpace(builder, st));
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

    private static void throwCompilationExceptionIfNoMatch(Tokenizer tok, String target, Script script, String directiveID) {
        if (!target.equals(tok.nextToken()))
            throw new CompilingException("Script in \"" + script.FILE_PATH + "\" has a " +
                    "incorrectly formatted '$" + directiveID + "' directive");
    }

    private static String maybeParseSpace(StringBuilder builder, Tokenizer st) {
        String tok = st.nextToken();
        if (tok.equals(" ")) {
            builder.append(tok);
            tok = st.nextToken();
        }
        return tok;
    }

    private static String parseStatement(Tokenizer st) {
        StringBuilder builder = new StringBuilder();
        int braceCount = 0;
        boolean hasSeenFreeBrace = false;
        String token = "";
        while (!(!hasSeenFreeBrace && token.equals(";") || hasSeenFreeBrace && braceCount == 0)) {
            token = st.nextToken();
            switch (token) {
                case "{":
                    hasSeenFreeBrace = hasSeenFreeBrace || braceCount == 0;
                case "[":
                case "(":
                    braceCount++;
                    break;

                case "}":
                case "]":
                case ")":
                    braceCount--;
                    break;

                default:
                    break;
            }
            builder.append(token);
        }
        return builder.toString();
    }

    private static String parseAndResolveCodeBlock(Tokenizer st, boolean resolve, Script script,
                                                   Map<DataHolder, Pair<String, CtClass>> classMap,
                                                   Map<String, Integer> paramMap) {
        StringBuilder builder = new StringBuilder();
        int braceCount = 1;
        String tok;
        while (braceCount > 0) {
            switch (tok = st.nextToken()) {
                case "(":
                case "[":
                case "{":
                    braceCount++;
                    break;

                case ")":
                case "]":
                case "}":
                    braceCount--;
                    break;

                default:
                    break;
            }
            if (resolve)
                builder.append(tok);
        }
        return resolve ? resolveCode(builder.substring(0, builder.length() - 1), script, classMap, paramMap) : "";
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
