package net.novaproject.novauhc.scenario.role;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public final class RoleStates {

    private static final List<Runnable> CLEARERS = new CopyOnWriteArrayList<>();

    public static void onClear(Runnable clearer) {
        if (clearer != null) CLEARERS.add(clearer);
    }

    public static <K, V> Map<K, V> map() {
        Map<K, V> map = new HashMap<>();
        CLEARERS.add(map::clear);
        return map;
    }

    public static <K, V> Map<K, V> concurrentMap() {
        Map<K, V> map = new ConcurrentHashMap<>();
        CLEARERS.add(map::clear);
        return map;
    }

    public static <T> Set<T> set() {
        Set<T> set = new HashSet<>();
        CLEARERS.add(set::clear);
        return set;
    }

    public static boolean hit(Map<UUID, Integer> counter, UUID uuid, int every) {
        if (counter == null || uuid == null || every <= 0) return false;
        int next = counter.compute(uuid, (k, current) -> {
            int value = (current == null ? 0 : current) + 1;
            return value >= every ? 0 : value;
        });
        return next == 0;
    }

    public static void clearAll() {
        for (Runnable clearer : CLEARERS) {
            try {
                clearer.run();
            } catch (Throwable t) {
                Bukkit.getLogger().log(Level.WARNING, "[RoleStates] clearer en échec", t);
            }
        }
    }
}
