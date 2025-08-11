package net.novaproject.novauhc.utils;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A (static-based) class to manage cool-downs for your plugins
 *
 * @author PhantomUnicorns
 */
public class ShortCooldownManager {

    private static final Map<Player, Map<String, Long>> _COOLDOWNS;

    static {
        _COOLDOWNS = new IdentityHashMap<>();
    }

    /**
     * Puts a duration in milliseconds
     *
     * @param player   The player to add a cool-down to
     * @param key      The key you would like to place on the cool-down
     * @param duration The time it takes for the cool-down to end (Milliseconds)
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
     * @param player The player to get the cool-down of
     * @param key    The key the cool-down is associated with
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
     * @param player The player to get-remove the cool-down of
     * @param key    The key the cool-down is associated with
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
