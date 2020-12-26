package me.utk.spigot_scripting.script;

import me.utk.spigot_scripting.script.elements.*;
import me.utk.spigot_scripting.util.FileUtil;
import me.utk.spigot_scripting.util.exception.LinkingException;
import me.utk.spigot_scripting.util.exception.ParsingException;

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

    private static void declareStruct(Scanner scanner, Script script) {
        eat(scanner, "struct");
        String uniqueID = currentToken(scanner);
        eat(scanner, uniqueID);

        Struct struct = new Struct();
        eat(scanner, "{");
        while (!currentToken(scanner).equals("}"))
            addMethodOrVariable(scanner, struct);
        eat(scanner, "}");
        script.DEFINED_STRUCTURES.put(uniqueID, struct);
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
        boolean isExecutor = currentToken(scanner).equals("$exec");
        eat(scanner, isExecutor ? "$exec" : "$comp");

        String uniqueID = currentToken(scanner);
        eat(scanner, uniqueID);

        eat(scanner, ":");
        script.COMMAND_HANDLERS.add(new Command(uniqueID, parseCodeBlock(scanner), isExecutor));
    }

    private static String parseExpression(Scanner scanner) {
        StringBuilder builder = new StringBuilder();

        int grouperCount = 0;
        boolean isExpressionEnd;
        String currentToken;
        do {
            currentToken = currentToken(scanner);
            isExpressionEnd = false;

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
                    isExpressionEnd = true;
                    break;

                default:
                    break;
            }

            if (!isExpressionEnd) {
                builder.append(" ").append(currentToken);
                eat(scanner, currentToken);
            }
        } while (grouperCount > 0 || !isExpressionEnd);
        return builder.toString();
    }

    private static boolean takesSpace(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z' || c == '_' || c == ',' || c == ')';
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
        String type = currentToken(scanner); // either variable type or method return val
        eat(scanner, type);
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
