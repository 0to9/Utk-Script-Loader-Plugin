package io.github.utk003.spigot_scripting.script;

import io.github.utk003.spigot_scripting.script.elements.*;
import io.github.utk003.spigot_scripting.util.exception.LinkingException;
import io.github.utk003.spigot_scripting.util.FileUtil;
import io.github.utk003.spigot_scripting.util.exception.ParsingException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class ScriptParser {
    private static final Map<String, Script> SCRIPTS_PARSED = new HashMap<>();
    public static final Set<String> IMPORTED_PACKAGES = new HashSet<>();

    public static void resetScripts() {
        SCRIPTS_PARSED.clear();
        IMPORTED_PACKAGES.clear();
    }
    public static Collection<Script> scriptsParsed() {
        return SCRIPTS_PARSED.values();
    }

    public static Script parseScript(String filePath) throws FileNotFoundException {
        Script script = SCRIPTS_PARSED.get(filePath);
        if (script == null) {
            SCRIPTS_PARSED.put(filePath, script = new Script(filePath));
            try {
                populateScript(script);
            } catch (ParsingException e) {
                SCRIPTS_PARSED.remove(filePath);
                e.printStackTrace();
                throw new ParsingException(e.getMessage() + " in \"" + filePath + "\"");
            }
        }
        return script;
    }

    private static void populateScript(Script script) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream(script.FILE_PATH));

        String currentToken;
        while (scanner.hasMore()) {
            currentToken = currentToken(scanner);
            if (currentToken.equals("#include"))
                includeScript(scanner, script);
            else if (currentToken.equals("#ref"))
                defineMacro(scanner, script);
            else if (currentToken.equals("import")) {
                eat(scanner, "import");
                String pack = currentToken(scanner);

                // Prevents scripts from accessing classes they shouldn't see or use
                if (pack.startsWith(FileUtil.PROJECT_PACKAGE))
                    throw new LinkingException("Script in \"" + script.FILE_PATH + "\" is illegally trying to " +
                            "access classes from the \"" + FileUtil.PROJECT_PACKAGE + "\" package");

                // Handle multi-class import
                if (pack.endsWith(".*"))
                    pack = pack.substring(0, pack.length() - 2);

                IMPORTED_PACKAGES.add(pack);
                eat(scanner, pack);
                eat(scanner, ";");
            } else if (currentToken.equals("struct"))
                declareStruct(scanner, script);
            else if (currentToken.equals("callback"))
                declareCallback(scanner, script);
            else if (currentToken.startsWith("@"))
                createHook(scanner, script);
            else if (currentToken.startsWith("$"))
                defineCommand(scanner, script);
            else
                addMethodOrVariable(scanner, script);
        }
    }

    private static void includeScript(Scanner scanner, Script script) throws FileNotFoundException {
        eat(scanner, "#include");
        String uniqueID = currentToken(scanner);
        eat(scanner, uniqueID);
        String filePath = currentToken(scanner);
        eat(scanner, filePath);

        filePath = filePath.substring(1, filePath.length() - 1); // trim quotes
        filePath = FileUtil.cleanFilePath(FileUtil.getParentDirectory(script.FILE_PATH) + filePath);

        script.INCLUDED_SCRIPTS.put(uniqueID, parseScript(filePath));
    }
    private static void defineMacro(Scanner scanner, Script script) {
        eat(scanner, "#ref");
        String uniqueID = currentToken(scanner);
        eat(scanner, uniqueID);
        String classPath = currentToken(scanner);
        eat(scanner, classPath);

        // Prevents scripts from accessing classes they shouldn't see or use
        if (classPath.startsWith(FileUtil.PROJECT_PACKAGE))
            throw new LinkingException("Script in \"" + script.FILE_PATH + "\" is illegally trying to " +
                    "refer to classes from the \"" + FileUtil.PROJECT_PACKAGE + "\" package");

        script.CLASS_REFERENCES.put(uniqueID, classPath);
    }

    private static void declareStruct(Scanner scanner, Script script) {
        eat(scanner, "struct");
        parseRestOfStruct(scanner, script, false);
    }
    private static void declareCallback(Scanner scanner, Script script) {
        eat(scanner, "callback");
        parseRestOfStruct(scanner, script, true);
    }
    private static void parseRestOfStruct(Scanner scanner, Script script, boolean isCallback) {
        String uniqueID = currentToken(scanner);
        eat(scanner, uniqueID);

        Struct struct = new Struct(isCallback);
        eat(scanner, "{");
        while (!currentToken(scanner).equals("}"))
            addMethodOrVariable(scanner, struct);
        eat(scanner, "}");
        if (isCallback) {
            boolean[][] alreadyDefined = new boolean[2][10];
            String[] methodIDs = {"run", "get"}, retType = {"void", "Object"};
            for (int i = 0; i < 2; i++)
                for (Function def : struct.DEFINED_FUNCTIONS.get(methodIDs[i])) {
                    int count = countArgs(def);
                    if (count < 10)
                        alreadyDefined[i][count] = true;
                }

            String code = " { throw new UnsupportedOperationException(); }";
            for (int i = 0; i < 2; i++)
                if (!alreadyDefined[i][0])
                    struct.DEFINED_FUNCTIONS.put(methodIDs[i], new Function(retType[i], "()", code));

            StringBuilder builder = new StringBuilder("Object o1");
            for (int j = 1; j < 10; j++) {
                String args = "( " + builder + " )";
                for (int i = 0; i < 2; i++)
                    if (!alreadyDefined[i][j])
                        struct.DEFINED_FUNCTIONS.put(methodIDs[i], new Function(retType[i], args, code));
                builder.append(" , Object o").append(j + 1);
            }
        }
        if (!struct.DEFINED_FUNCTIONS.containsKey(uniqueID))
            struct.DEFINED_FUNCTIONS.put(uniqueID, new Function("init", "()", " { }"));
        script.DEFINED_STRUCTURES.put(uniqueID, struct);
    }

    private static int countArgs(Function function) {
        if (function.ARGUMENTS.length() == 2)
            return 0;
        int countCommas = 0;
        for (char c : function.ARGUMENTS.toCharArray())
            if (c == ',')
                countCommas++;
        return countCommas + 1;
    }

    private static String parseCodeBlock(Scanner scanner) {
        StringBuilder builder = new StringBuilder();

        int braceCount = 0;
        String currentToken;
        do {
            currentToken = currentToken(scanner);

            if (currentToken.equals("{"))
                braceCount++;
            else if (currentToken.equals("}"))
                braceCount--;

            builder.append(" ").append(currentToken);
            eat(scanner, currentToken);
        } while (braceCount > 0);
        return builder.toString();
    }

    private static void createHook(Scanner scanner, Script script) {
        String uniqueID = currentToken(scanner);
        eat(scanner, uniqueID);
        uniqueID = uniqueID.substring(1); // remove the @

        eat(scanner, ":");
        script.LINKED_CALLBACKS.add(new Hook(uniqueID, parseCodeBlock(scanner)));
    }

    private static void defineCommand(Scanner scanner, Script script) {
        String handlerID = currentToken(scanner);
        Command.HandlerType handlerType = Command.HandlerType.fromID(handlerID.substring(1)); // remove the $
        eat(scanner, handlerID);

        List<String> uniqueIDs = new LinkedList<>();
        {
            String uniqueID = currentToken(scanner);
            uniqueIDs.add(uniqueID);
            eat(scanner, uniqueID);

            while (currentToken(scanner).equals(",")) {
                eat(scanner, ",");

                uniqueID = currentToken(scanner);
                uniqueIDs.add(uniqueID);
                eat(scanner, uniqueID);
            }
        }

        eat(scanner, ":");
        script.COMMAND_HANDLERS.add(new Command(uniqueIDs, parseCodeBlock(scanner), handlerType));
    }

    private static String parseExpression(Scanner scanner) {
        StringBuilder builder = new StringBuilder();

        int grouperCount = 0;
        boolean stayInLoop = true;
        String currentToken;
        do {
            currentToken = currentToken(scanner);
            switch (currentToken) {
                case "(":
                case "[":
                case "{":
                    grouperCount++;
                    break;

                case ")":
                case "]":
                case "}":
                    grouperCount--;
                    break;

                case ",":
                case ";":
                    stayInLoop = false;
                    break;

                default:
                    break;
            }

            if (stayInLoop = stayInLoop || grouperCount > 0) {
                builder.append(" ").append(currentToken);
                eat(scanner, currentToken);
            }
        } while (stayInLoop);
        return builder.toString();
    }

    private static String parseArguments(Scanner scanner) {
        StringBuilder builder = new StringBuilder(), helper = new StringBuilder();

        builder.append("( ");
        eat(scanner, "(");

        String currentToken = currentToken(scanner);
        if (currentToken.equals(")")) {
            eat(scanner, ")");
            return "()";
        }
        String prevToken = "";
        do {
            if (currentToken.equals(",")) {
                builder.append(helper).append(" ").append(prevToken);
                builder.append(" , ");

                prevToken = "";
                helper = new StringBuilder();
            } else {
                helper.append(prevToken);
                prevToken = currentToken;
            }
            eat(scanner, currentToken);
        } while (!(currentToken = currentToken(scanner)).equals(")"));

        builder.append(helper).append(" ").append(prevToken).append(" )");
        eat(scanner, ")");

        return builder.toString();
    }

    private static void addMethodOrVariable(Scanner scanner, DataHolder holder) {
        StringBuilder typeBuilder = new StringBuilder(currentToken(scanner)); // either variable type or method return val
        eat(scanner, typeBuilder.toString());
        while (currentToken(scanner).equals("[")) {
            eat(scanner, "[");
            eat(scanner, "]");
            typeBuilder.append("[]");
        }
        String type = typeBuilder.toString();

        String uniqueID = currentToken(scanner); // either variable or method id
        eat(scanner, uniqueID);

        String code = null;
        switch (currentToken(scanner)) {
            case "=":
                eat(scanner, "=");
                code = parseExpression(scanner) + " ;";
            case ",":
                holder.DEFINED_VARIABLES.put(uniqueID, new Variable(type, code));
                while (!currentToken(scanner).equals(";")) {
                    eat(scanner, ",");
                    uniqueID = currentToken(scanner); // variable id
                    eat(scanner, uniqueID);
                    if (currentToken(scanner).equals("=")) {
                        eat(scanner, "=");
                        code = parseExpression(scanner) + " ;";
                    } else
                        code = null;
                    holder.DEFINED_VARIABLES.put(uniqueID, new Variable(type, code));
                }
            case ";":
                if (!holder.DEFINED_VARIABLES.containsKey(uniqueID))
                    holder.DEFINED_VARIABLES.put(uniqueID, new Variable(type, code));
                eat(scanner, ";");
                break;

            case "(":
            default:
                holder.DEFINED_FUNCTIONS.put(uniqueID, new Function(type, parseArguments(scanner), parseCodeBlock(scanner)));
                break;
        }
    }

    private static String currentToken(Scanner scanner) {
        return scanner.current().token;
    }

    private static void eat(Scanner scanner, String token) {
        if (!currentToken(scanner).equals(token)) {
            Scanner.Token tok = scanner.current();
            String msg = "Malformed script @ Row " + tok.row + " Col " + tok.col;
            throw new ParsingException(msg);
        }
        scanner.advance();
    }
}
