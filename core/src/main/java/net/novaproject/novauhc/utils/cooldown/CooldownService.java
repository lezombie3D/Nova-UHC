package net.novaproject.novauhc.utils.cooldown;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import net.novaproject.novauhc.UHCManager;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CooldownService {

    private static final Map<UUID, ExpiryTracker<String>> COOLDOWNS = new HashMap<>();

    private static final CooldownSpi.ICooldownService INSTANCE = new CooldownSpi.ICooldownService() {
        @Override public boolean put(Player player, String key, long durationMs) { return CooldownService.put(player, key, durationMs); }
        @Override public boolean put(UUID uuid, String key, long durationMs) { return CooldownService.put(uuid, key, durationMs); }
        @Override public long get(Player player, String key) { return CooldownService.get(player, key); }
        @Override public long get(UUID uuid, String key) { return CooldownService.get(uuid, key); }
        @Override public long remove(Player player, String key) { return CooldownService.remove(player, key); }
        @Override public long remove(UUID uuid, String key) { return CooldownService.remove(uuid, key); }
        @Override public int clearAll(Player player) { return CooldownService.clearAll(player); }
        @Override public int clearAll(UUID uuid) { return CooldownService.clearAll(uuid); }
        @Override public Set<String> getActiveKeys(Player player) { return CooldownService.getActiveKeys(player); }
        @Override public Set<String> getActiveKeys(UUID uuid) { return CooldownService.getActiveKeys(uuid); }
        @Override public int reduceAll(Player player, long reductionMs) { return CooldownService.reduceAll(player, reductionMs); }
        @Override public int reduceAll(UUID uuid, long reductionMs) { return CooldownService.reduceAll(uuid, reductionMs); }
    };

    public static CooldownSpi.ICooldownService instance() {
        return INSTANCE;
    }

    public static boolean put(Player player, String key, long durationMs) {
        return put(player.getUniqueId(), key, durationMs);
    }

    public static boolean put(UUID uuid, String key, long durationMs) {
        return COOLDOWNS.computeIfAbsent(uuid, k -> new ExpiryTracker<>()).putIfExpired(key, durationMs);
    }

    public static long get(Player player, String key) {
        return get(player.getUniqueId(), key);
    }

    public static long get(UUID uuid, String key) {
        ExpiryTracker<String> tracker = COOLDOWNS.get(uuid);
        if (tracker == null) return -1;
        long remaining = tracker.remaining(key);
        if (tracker.isEmpty()) COOLDOWNS.remove(uuid);
        return remaining;
    }

    public static long remove(Player player, String key) {
        return remove(player.getUniqueId(), key);
    }

    public static long remove(UUID uuid, String key) {
        ExpiryTracker<String> tracker = COOLDOWNS.get(uuid);
        if (tracker == null) return -1;
        long remaining = tracker.remove(key);
        if (tracker.isEmpty()) COOLDOWNS.remove(uuid);
        return remaining;
    }

    public static int clearAll(Player player) {
        return clearAll(player.getUniqueId());
    }

    public static int clearAll(UUID uuid) {
        ExpiryTracker<String> tracker = COOLDOWNS.remove(uuid);
        return tracker == null ? 0 : tracker.size();
    }

    public static Set<String> getActiveKeys(Player player) {
        return getActiveKeys(player.getUniqueId());
    }

    public static Set<String> getActiveKeys(UUID uuid) {
        ExpiryTracker<String> tracker = COOLDOWNS.get(uuid);
        return tracker == null ? Collections.emptySet() : tracker.activeKeys();
    }

    public static int reduceAll(Player player, long reductionMs) {
        if (player == null) return 0;
        return reduceAll(player.getUniqueId(), reductionMs);
    }

    public static int reduceAll(UUID uuid, long reductionMs) {
        if (reductionMs <= 0) return 0;
        ExpiryTracker<String> tracker = COOLDOWNS.get(uuid);
        if (tracker == null) return 0;
        int affected = tracker.reduceAll(reductionMs);
        if (tracker.isEmpty()) COOLDOWNS.remove(uuid);
        return affected;
    }

    public static final class ExpiryTracker<K> {

        private final Map<K, Long> expiry = new ConcurrentHashMap<>();

        public boolean putIfExpired(K key, long durationMs) {
            long now = System.currentTimeMillis();
            Long existing = expiry.get(key);
            if (existing != null && existing > now) return false;
            expiry.put(key, now + durationMs);
            return true;
        }

        public void extend(K key, long durationMs) {
            expiry.merge(key, System.currentTimeMillis() + durationMs, Math::max);
        }

        public void set(K key, long durationMs) {
            expiry.put(key, System.currentTimeMillis() + durationMs);
        }

        public void setSeconds(K key, double durationSec) {
            set(key, (long) (durationSec * 1000L));
        }

        public long remaining(K key) {
            Long expiresAt = expiry.get(key);
            if (expiresAt == null) return -1;
            long remaining = expiresAt - System.currentTimeMillis();
            if (remaining > 0) return remaining;
            expiry.remove(key, expiresAt);
            return -1;
        }

        public boolean isActive(K key) {
            return remaining(key) > 0;
        }

        public long remove(K key) {
            Long expiresAt = expiry.remove(key);
            if (expiresAt == null) return -1;
            long remaining = expiresAt - System.currentTimeMillis();
            return remaining > 0 ? remaining : -1;
        }

        public Set<K> activeKeys() {
            long now = System.currentTimeMillis();
            Set<K> keys = new HashSet<>();
            for (Map.Entry<K, Long> entry : expiry.entrySet()) {
                if (entry.getValue() > now) keys.add(entry.getKey());
            }
            return keys;
        }

        public int reduceAll(long reductionMs) {
            if (reductionMs <= 0 || expiry.isEmpty()) return 0;
            long now = System.currentTimeMillis();
            int affected = 0;
            Iterator<Map.Entry<K, Long>> it = expiry.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, Long> entry = it.next();
                long remaining = entry.getValue() - now;
                if (remaining <= 0) {
                    it.remove();
                    continue;
                }
                if (remaining <= reductionMs) it.remove();
                else entry.setValue(entry.getValue() - reductionMs);
                affected++;
            }
            return affected;
        }

        public boolean isEmpty() {
            return expiry.isEmpty();
        }

        public int size() {
            return expiry.size();
        }

        public void clear() {
            expiry.clear();
        }
    }

    public static final class CombatTracker {

        private static final Map<UUID, Long> LAST_HIT = new HashMap<>();
        private static final Map<UUID, UUID> LAST_DAMAGER = new HashMap<>();

        private static final CooldownSpi.ICombatTracker INSTANCE = new CooldownSpi.ICombatTracker() {
            @Override public void startCombat(UUID playerId, UUID damagerId) { CombatTracker.startCombat(playerId, damagerId); }
            @Override public boolean isInCombat(UUID playerId) { return CombatTracker.isInCombat(playerId); }
            @Override public UUID getLastDamager(UUID playerId) { return CombatTracker.getLastDamager(playerId); }
            @Override public void clearAll() { CombatTracker.clearAll(); }
        };

        public static CooldownSpi.ICombatTracker instance() { return INSTANCE; }

        public static void startCombat(UUID playerId, UUID damagerId) {
            LAST_HIT.put(playerId, System.currentTimeMillis());
            LAST_DAMAGER.put(playerId, damagerId);
        }

        public static boolean isInCombat(UUID playerId) {
            Long lastHit = LAST_HIT.get(playerId);
            if (lastHit == null) return false;
            return (System.currentTimeMillis() - lastHit) < UHCManager.get().getCombatTagSeconds() * 1000L;
        }

        public static UUID getLastDamager(UUID playerId) {
            return LAST_DAMAGER.get(playerId);
        }

        public static void clearAll() {
            LAST_HIT.clear();
            LAST_DAMAGER.clear();
        }
    }
}
