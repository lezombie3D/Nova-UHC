package net.novaproject.novauhc.debug;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class TimedTriggers {

    public record Trigger(String name, String description, Runnable action) {}

    private static final Map<String, Trigger> TRIGGERS = new LinkedHashMap<>();

    public static void register(String name, String description, Runnable action) {
        if (name == null || action == null) return;
        TRIGGERS.put(name.toLowerCase(), new Trigger(name.toLowerCase(), description, action));
    }

    public static void unregister(String name) {
        if (name == null) return;
        TRIGGERS.remove(name.toLowerCase());
    }

    public static Set<String> names() {
        return Collections.unmodifiableSet(TRIGGERS.keySet());
    }

    public static Trigger get(String name) {
        return name == null ? null : TRIGGERS.get(name.toLowerCase());
    }

    public static boolean fire(String name) {
        Trigger trigger = get(name);
        if (trigger == null) return false;
        trigger.action().run();
        return true;
    }

    public static void clear() {
        TRIGGERS.clear();
    }
}
