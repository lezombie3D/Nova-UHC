package net.novaproject.novauhc.ability.toolbox;

import net.novaproject.novauhc.ability.Ability;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AbilityStates {

    private static final Map<String, Map<String, Object>> STORE = new ConcurrentHashMap<>();

    private final String scope;

    private AbilityStates(String scope) {
        this.scope = scope;
    }

    public static AbilityStates of(Ability ability, Player owner) {
        return of(ability.getClass(), owner.getUniqueId());
    }

    public static AbilityStates of(Ability ability, UUID owner) {
        return of(ability.getClass(), owner);
    }

    public static AbilityStates of(Class<?> abilityClass, UUID owner) {
        return new AbilityStates(abilityClass.getName() + "#" + owner);
    }

    private Map<String, Object> map() {
        return STORE.computeIfAbsent(scope, k -> new ConcurrentHashMap<>());
    }

    public int getInt(String key) {
        Object o = map().get(key);
        return o instanceof Integer ? (Integer) o : 0;
    }

    public int inc(String key) {
        return inc(key, 1);
    }

    public int inc(String key, int by) {
        int v = getInt(key) + by;
        map().put(key, v);
        return v;
    }

    public boolean getBool(String key) {
        Object o = map().get(key);
        return o instanceof Boolean && (Boolean) o;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) map().get(key);
    }

    public <T> T getOr(String key, T fallback) {
        Object o = map().get(key);
        return o == null ? fallback : (T) o;
    }

    public void set(String key, Object value) {
        if (value == null) map().remove(key);
        else map().put(key, value);
    }

    public boolean has(String key) {
        return map().containsKey(key);
    }

    public void remove(String key) {
        map().remove(key);
    }

    public void clear() {
        STORE.remove(scope);
    }

    public static void clearPlayer(UUID owner) {
        STORE.keySet().removeIf(k -> k.endsWith("#" + owner));
    }

    public static void clearAll() {
        STORE.clear();
    }
}
