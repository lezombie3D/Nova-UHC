package net.novaproject.novauhc.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public class ShortCooldownManager {

    private static final Map<Player, Map<String, Long>> _COOLDOWNS;
    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<UUID, UUID> lastDamager = new HashMap<>();
    private static final long COMBAT_COOLDOWN_MS = ConfigUtils.getGeneralConfig().getInt("timer.combat") * 1000L;

    static {
        _COOLDOWNS = new IdentityHashMap<>();
    }

    public static void startCombat(UUID playerId, UUID damagerId) {
        cooldowns.put(playerId, System.currentTimeMillis());
        lastDamager.put(playerId, damagerId);
    }

    public static UUID getLastDamager(UUID playerId) {
        return lastDamager.get(playerId);
    }

    /**
     * Checks if the player is currently in combat (cooldown active).
     */
    public static boolean isInCombat(UUID playerId) {
        Long lastHit = cooldowns.get(playerId);
        if (lastHit == null) return false;
        return (System.currentTimeMillis() - lastHit) < COMBAT_COOLDOWN_MS;
    }

    /**
     * Removes expired cooldowns.
     * Should be called periodically to clean up.
     */
    public static void cleanup() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> (now - entry.getValue()) >= COMBAT_COOLDOWN_MS);
    }
    /**
     * Puts a duration in milliseconds
     *
     * @param player
     *            The player to add a cool-down to
     * @param key
     *            The key you would like to place on the cool-down
     * @param duration
     *            The time it takes for the cool-down to end (Milliseconds)
     * @return If it successfully added the cool-down
     */
    public static boolean put(Player player, String key, long duration) {
        if (_COOLDOWNS.containsKey(player)) {
            Map<String, Long> playerCooldowns = _COOLDOWNS.get(player);
            if (!playerCooldowns.containsKey(key)) {
                playerCooldowns.put(key, System.currentTimeMillis() + duration);
            } else {
                return false;
            }
        } else {
            Map<String, Long> playerCooldowns = new HashMap<>();
            playerCooldowns.put(key, System.currentTimeMillis() + duration);
            _COOLDOWNS.put(player, playerCooldowns);
        }
        return true;
    }

    /**
     * Gets the duration left in milliseconds
     *
     * @param player
     *            The player to get the cool-down of
     * @param key
     *            The key the cool-down is associated with
     * @return The duration left (Milliseconds)
     */
    public static long get(Player player, String key) {
        if (_COOLDOWNS.containsKey(player)) {
            Map<String, Long> playerCooldowns = _COOLDOWNS.get(player);
            if (playerCooldowns.containsKey(key)) {
                long durationLeft = playerCooldowns.get(key);
                if (durationLeft > System.currentTimeMillis()) {
                    return durationLeft - System.currentTimeMillis();
                } else {
                    playerCooldowns.remove(key);
                    if (playerCooldowns.isEmpty()) {
                        _COOLDOWNS.remove(player);
                    }
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    /**
     * Gets and removes the cool-down from the player
     *
     * @param player
     *            The player to get-remove the cool-down of
     * @param key
     *            The key the cool-down is associated with
     * @return The duration left (Milliseconds)
     */
    public static long remove(Player player, String key) {
        if (_COOLDOWNS.containsKey(player)) {
            Map<String, Long> playerCooldowns = _COOLDOWNS.get(player);
            if (playerCooldowns.containsKey(key)) {
                long durationLeft = playerCooldowns.remove(key);
                if (durationLeft > System.currentTimeMillis()) {
                    return durationLeft - System.currentTimeMillis();
                } else {
                    playerCooldowns.remove(key);
                    if (playerCooldowns.isEmpty()) {
                        _COOLDOWNS.remove(player);
                    }
                    return -1;
                }
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }
}
