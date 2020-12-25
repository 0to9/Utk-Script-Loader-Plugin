package me.utk.spigot_scripting.command;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.util.FileUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.List;

public abstract class CommandUtil {
    private CommandUtil() {
    }

    private static final String SUB_COMMAND_HANDLER_CLASS_PATH = FileUtil.PROJECT_CLASS_PATH + "command.SubCommandHandler";

    public static void addSubCommandExecutor(String subCommandID, String executorMethodCode) throws Exception {
        ClassPool defaultPool = ClassPool.getDefault();
        CtClass subCommandHandlerClass = defaultPool.get(SUB_COMMAND_HANDLER_CLASS_PATH);

        CtMethod initializeMethod = subCommandHandlerClass.getDeclaredMethod("initialize", new CtClass[0]);
        initializeMethod.insertAfter("{\n" +
                "            EXECUTOR_FAILURE_COUNTS.add(new me.utk.util.data.ClassWrapper(new Integer(0)));\n" +
                "            ALL_SUB_COMMAND_IDS.add(\"" + subCommandID + "\");\n" +
                "        }");

        CtMethod executeMethod = subCommandHandlerClass.getDeclaredMethod("executionImplementation");
        String code = "{\n" +
                "          me.utk.util.data.ClassWrapper numFailures = (me.utk.util.data.ClassWrapper) $4.next();\n" +
                "          int failures = ((Integer) numFailures.value).intValue();\n" +
                "          if (failures < 10 && $2.equals(\"" + subCommandID + "\"))\n" +
                "              try {\n" +
                "                  " + executorMethodCode + "($1, $3);\n" +
                "              } catch (Exception e) {\n" +
                "                  numFailures.value = new Integer(failures + 1);\n" +
                "              }\n" +
                "      }";
        executeMethod.insertAfter(code);
    }
    public static void addSubCommandTabCompleter(String subCommandID, String completerMethodCode) throws Exception {
        ClassPool defaultPool = ClassPool.getDefault();
        CtClass subCommandHandlerClass = defaultPool.get(SUB_COMMAND_HANDLER_CLASS_PATH);

        CtMethod initializeMethod = subCommandHandlerClass.getDeclaredMethod("initialize", new CtClass[0]);
        initializeMethod.insertAfter("TAB_COMPLETER_FAILURE_COUNTS.add(new me.utk.util.data.ClassWrapper(new Integer(0)));");

        CtMethod executeMethod = subCommandHandlerClass.getDeclaredMethod("tabCompletionImplementation");
        String code = "{\n" +
                "          me.utk.util.data.ClassWrapper numFailures = (me.utk.util.data.ClassWrapper) $5.next();\n" +
                "          int failures = ((Integer) numFailures.value).intValue();\n" +
                "          if (failures < 10 && $2.equals(\"" + subCommandID + "\"))\n" +
                "              try {\n" +
                "                  $4.addAll(" + completerMethodCode + "($1, $3));\n" +
                "              } catch (Exception e) {\n" +
                "                  numFailures.value = new Integer(failures + 1);\n" +
                "              }\n" +
                "      }";
        executeMethod.insertAfter(code);
    }

    public static void loadAndInitializeSubCommandHandler() {
        ClassPool defaultPool = ClassPool.getDefault();
        try {
            CtClass subCommandHandlerClass = defaultPool.get(SUB_COMMAND_HANDLER_CLASS_PATH);
            Class<?> eventClass = subCommandHandlerClass.toClass();

            Method initMethod = eventClass.getMethod("initialize");
            initMethod.invoke(null);
        } catch (Exception e) {
            ErrorLogger.logError(() -> "Unable to initialize SubCommandHandler class", e);
        }
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(message);
    }
    public static void sendWarningMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.GOLD + message); // TODO choose gold (= orange) or yellow
    }
    public static void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }

    public static List<String> purgeCompletions(String currentInput, List<String> completions) {
        if (completions != null) {
            completions.removeIf(s -> !s.contains(currentInput));
            if (completions.isEmpty())
                completions = null;
        }
        return completions;
    }

    public static String[] trimArguments(String... args) {
        String[] newArgs = new String[args.length - 1]; // intentionally throw exception if args.length == 0
        System.arraycopy(args, 1, newArgs, 0, newArgs.length);
        return newArgs;
    }
}
