package me.utk.spigot_scripting.event;

import javassist.CtClass;
import me.utk.spigot_scripting.loader_linker.ScriptLinker;
import me.utk.spigot_scripting.util.ErrorLogger;
import me.utk.spigot_scripting.util.FileUtil;
import me.utk.spigot_scripting.util.reflection.ReflectiveInitializer;
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
        addClassToMap("Exit", "", "ScriptTerminationWrapper");

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

    public static void addEventHandler(String eventID, CtClass handlerClass, String methodID) throws Exception {
        String fullClassName = getFullyQualifiedPackagePath(eventID);
        Class<?> clazz = Class.forName(fullClassName);
        Method adder = clazz.getMethod("addHandler", ReflectiveInitializer.class);
        adder.invoke(null, (ReflectiveInitializer<Method>) () -> {
            Class<?> finalClass = ScriptLinker.CLASS_CLASS_MAP.get(handlerClass);
            return finalClass.getMethod(methodID, EventWrapper.class);
        });
    }

    public static void initializeEventWrappers() {
        runMethodOnEventWrappers("initialize");
    }
    public static void terminateEventWrappers() {
        runMethodOnEventWrappers("terminate");
    }
    private static void runMethodOnEventWrappers(String methodID) {
        for (String eventID : EVENT_ID_TO_PACKAGES_MAP.keySet())
            try {
                Class<?> eventClass = Class.forName(getFullyQualifiedPackagePath(eventID));
                Method initMethod = eventClass.getMethod(methodID);
                initMethod.invoke(null);
            } catch (Exception e) {
                ErrorLogger.logError(() -> "Unable to " + methodID + " " + getEventClassName(eventID) + " handler class", e);
            }
    }
}
