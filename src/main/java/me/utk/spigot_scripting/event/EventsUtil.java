package me.utk.spigot_scripting.event;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.util.FileUtil;
import me.utk.util.data.Pair;

import java.lang.reflect.Method;
import java.util.*;

public abstract class EventsUtil {
    private EventsUtil() {
    }

    private static final String BASE_EVENT_PACKAGE_PATH = FileUtil.PROJECT_CLASS_PATH + "event";
    public static final Set<String> EVENT_PACKAGES = new HashSet<>();
    // eventID -> {me.utk package, me.utk class}
    private static final Map<String, Pair<String, String>> EVENT_ID_TO_PACKAGES_MAP = new HashMap<>();
    static {
        /*
         * TODO add all events to map
         * All events are initially stored relative to root package: BASE_EVENT_PACKAGE_PATH
         */
        // Base script-related events
        addClassToMap("Init", "", "ScriptInitializationWrapper");

        // Spigot events
        addClassToMap("PlayerInteract", "spigot.player", "PlayerInteractWrapper");

        /*
         * Load event packages to packages set
         */
        for (Pair<String, String> eventData : EVENT_ID_TO_PACKAGES_MAP.values())
            EVENT_PACKAGES.add(eventData.first);
    }
    private static void addClassToMap(String eventID, String relativePackage, String className) {
        String packagePath = relativePackage.isEmpty() ? BASE_EVENT_PACKAGE_PATH :
                BASE_EVENT_PACKAGE_PATH + "." + relativePackage;
        EVENT_ID_TO_PACKAGES_MAP.put(eventID, new Pair<>(packagePath, className));
    }

    public static String getFullyQualifiedPackagePath(String eventID) {
        Pair<String, String> eventData = EVENT_ID_TO_PACKAGES_MAP.get(eventID);
        return eventData.first + "." + eventData.second;
    }
    public static String getEventClassName(String eventID) {
        return EVENT_ID_TO_PACKAGES_MAP.get(eventID).second;
    }

    public static void addHandlerCodeToEventHandler(String eventID, String handlerMethodCode) throws Exception {
        ClassPool defaultPool = ClassPool.getDefault();

        String fullClassName = getFullyQualifiedPackagePath(eventID);
        CtClass eventClass = defaultPool.get(fullClassName);

        CtMethod initializeMethod = eventClass.getDeclaredMethod("initialize", new CtClass[0]);
        initializeMethod.insertAfter("FAILURE_COUNTS.add(new me.utk.util.data.ClassWrapper(new Integer(0)));");

        CtClass itClass = defaultPool.get("java.util.Iterator");
        CtMethod handlerMethod = eventClass.getDeclaredMethod("notifyHandlers", new CtClass[]{eventClass, itClass});
        String code = "{\n" +
                "          me.utk.util.data.ClassWrapper numFailures = (me.utk.util.data.ClassWrapper) $2.next();\n" +
                "          int failures = ((Integer) numFailures.value).intValue();\n" +
                "          if (failures < 10)\n" +
                "              try {\n" +
                "                  " + handlerMethodCode + "($1);\n" +
                "              } catch (Exception e) {\n" +
                "                  numFailures.value = new Integer(failures + 1);\n" +
                "              }\n" +
                "      }";
        handlerMethod.insertAfter(code);
    }

    public static void loadAndInitializeEventClassChanges() {
        ClassPool defaultPool = ClassPool.getDefault();
        for (String eventID : EVENT_ID_TO_PACKAGES_MAP.keySet())
            try {
                CtClass eventCtClass = defaultPool.get(getFullyQualifiedPackagePath(eventID));
                Class<?> eventClass = eventCtClass.toClass();

                Method initMethod = eventClass.getMethod("initialize");
                initMethod.invoke(null);
            } catch (Exception e) {
                ErrorLogger.logError(() -> "Unable to initialize " + getEventClassName(eventID) + " handler class", e);
            }
    }
}
