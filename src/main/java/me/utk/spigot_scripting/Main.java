package me.utk.spigot_scripting;

import javassist.CtClass;

import me.utk.spigot_scripting.command.CommandUtil;
import me.utk.spigot_scripting.event.EventsUtil;
import me.utk.spigot_scripting.event.ScriptInitializationWrapper;
import me.utk.spigot_scripting.loader_linker.ScriptLinker;
import me.utk.spigot_scripting.loader_linker.ScriptLoader;

public class Main {
    public static void main(String[] args) throws Exception {
        CtClass.debugDump = "dump";

//        File file = new File("test.txt");
//        Scanner scanner = new Scanner(new FileInputStream(file));
//        System.out.println(scanner.current());
//        while (scanner.hasMore())
//            System.out.println(scanner.advance());

//        ElementParser.parseScript("test.txt").print(System.out);

//        ClassPool defaultPool = ClassPool.getDefault();
//        defaultPool.importPackage("java.util");
//        CtClass clazz = defaultPool.get("me.utk.spigot_scripting.Helper");
//        CtMethod method = clazz.getDeclaredMethod("run", new CtClass[0]);
//
//
//        CtClass c1 = defaultPool.makeClass("me.utk.Test1");
//        CtMethod newMeth = CtMethod.make("public static int fact(int i) {\n" +
//                "        ArrayList list = new ArrayList();\n" +
//                "        list.add(new Integer(1));\n" +
//                "        list.add(new Integer(2));\n" +
//                "        list.add(new Integer(3));\n" +
//                "        return ((Integer) list.remove(i)).intValue();\n" +
//                "    }", c1);
//        c1.addMethod(newMeth);
//        c1.toClass();
//
//        method.insertBefore("System.out.println(me.utk.Test1.fact(1));");
//        clazz.toClass();
//
//        Helper.run();

        // Start script processing
//        ScriptLoader.loadScript("scripts/main.txt");
        ScriptLoader.loadScript("test.txt");
        ScriptLinker.linkScripts();
        EventsUtil.loadAndInitializeEventClassChanges();
        CommandUtil.loadAndInitializeSubCommandHandler();
        ScriptInitializationWrapper.handleEvent();
        // End script processing
    }
}
