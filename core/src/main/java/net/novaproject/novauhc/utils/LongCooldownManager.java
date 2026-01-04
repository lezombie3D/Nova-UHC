package net.novaproject.novauhc.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A class to manage cooldowns for your plugins
 *
 * @author PhantomUnicorns
 */
public class LongCooldownManager {

    private static final Map<UUID, Map<String, Long>> _COOLDOWNS;

    static {
        _COOLDOWNS = new HashMap<>();
    }

    /**
     * Puts a duration in milliseconds
     *
     * @param player   The player to add a cooldown to
     * @param key      The key you would like to place on the cooldown
     * @param duration The time it takes for the cooldown to end (Milliseconds)
     * @return If it successfully added the cooldown
     */
    public static boolean put(Player player, String key, long duration) {
        return put(player.getUniqueId(), key, duration);
    }

    /**
     * Puts a duration in milliseconds
     *
     * @param uuid     The player's uuid to add a cooldown to
     * @param key      The key you would like to place on the cooldown
     * @param duration The time it takes for the cooldown to end (Milliseconds)
     * @return If it successfully added the cooldown
     */
    public static boolean put(UUID uuid, String key, long duration) {
        if (_COOLDOWNS.containsKey(uuid)) {
            Map<String, Long> playerCooldowns = _COOLDOWNS.get(uuid);
            if (!playerCooldowns.containsKey(key)) {
                playerCooldowns.put(key, System.currentTimeMillis() + duration);
            } else {
                return false;
            }
        } else {
            Map<String, Long> playerCooldowns = new HashMap<>();
            playerCooldowns.put(key, System.currentTimeMillis() + duration);
            _COOLDOWNS.put(uuid, playerCooldowns);
        }
        return true;
    }

    /**
     * Gets the duration left in milliseconds
     *
     * @param player The player to get the cooldown of
     * @param key    The key the cooldown is associated with
     * @return The duration left (Milliseconds)
     */
    public static long get(Player player, String key) {
        return get(player.getUniqueId(), key);
    }

    /**
     * Gets the duration left in milliseconds
     *
     * @param player The player to get the cooldown of
     * @param key    The key the cooldown is associated with
     * @return The duration left (Milliseconds)
     */
    public static long get(UUID uuid, String key) {
        if (_COOLDOWNS.containsKey(uuid)) {
            Map<String, Long> playerCooldowns = _COOLDOWNS.get(uuid);
            if (playerCooldowns.containsKey(key)) {
                long durationLeft = playerCooldowns.get(key);
                if (durationLeft > System.currentTimeMillis()) {
                    return durationLeft - System.currentTimeMillis();
                } else {
                    playerCooldowns.remove(key);
                    if (playerCooldowns.isEmpty()) {
                        _COOLDOWNS.remove(uuid);
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
     * Gets and removes the cooldown from the player
     *
     * @param player The player to get-remove the cooldown of
     * @param key    The key the cooldown is associated with
     * @return The duration left (Milliseconds)
     */
    public static long remove(Player player, String key) {
        return remove(player.getUniqueId(), key);
    }

    /**
     * Gets and removes the cooldown from the player
     *
     * @param player The player to get-remove the cooldown of
     * @param key    The key the cooldown is associated with
     * @return The duration left (Milliseconds)
     */
    public static long remove(UUID uuid, String key) {
        if (_COOLDOWNS.containsKey(uuid)) {
            Map<String, Long> playerCooldowns = _COOLDOWNS.get(uuid);
            if (playerCooldowns.containsKey(key)) {
                long durationLeft = playerCooldowns.remove(key);
                if (durationLeft > System.currentTimeMillis()) {
                    return durationLeft - System.currentTimeMillis();
                } else {
                    playerCooldowns.remove(key);
                    if (playerCooldowns.isEmpty()) {
                        _COOLDOWNS.remove(uuid);
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
