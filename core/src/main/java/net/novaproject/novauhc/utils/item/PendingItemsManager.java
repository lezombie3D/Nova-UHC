package net.novaproject.novauhc.utils.item;

import java.util.Arrays;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class PendingItemsManager {

    private static final Map<UUID, List<ItemStack>> PENDING = new HashMap<>();

    public static void give(Player player, ItemStack... items) {
        if (player == null || items == null) return;
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(
                Arrays.stream(items)
                        .filter(i -> i != null && i.getType() != Material.AIR)
                        .toArray(ItemStack[]::new));
        if (leftovers.isEmpty()) return;

        List<ItemStack> stash = PENDING.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        leftovers.values().forEach(item -> stash.add(item.clone()));
        LangManager.get().send(CoreLang.COMMON_FULL_INVENTORY_STORED, player, Map.of("%count%", leftovers.size()));
    }

    public static void drop(UUID uuid, Predicate<ItemStack> filter) {
        List<ItemStack> stash = PENDING.get(uuid);
        if (stash == null) return;
        stash.removeIf(filter);
        if (stash.isEmpty()) PENDING.remove(uuid);
    }

    public static boolean hasPending(UUID uuid) {
        List<ItemStack> stash = PENDING.get(uuid);
        return stash != null && !stash.isEmpty();
    }

    public static int getPendingCount(UUID uuid) {
        List<ItemStack> stash = PENDING.get(uuid);
        return stash == null ? 0 : stash.size();
    }

    public static int claim(Player player) {
        List<ItemStack> stash = PENDING.get(player.getUniqueId());
        if (stash == null || stash.isEmpty()) return 0;

        int claimed = 0;
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack item : stash) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (leftover.isEmpty()) {
                claimed++;
            } else {
                remaining.addAll(leftover.values());
            }
        }

        if (remaining.isEmpty()) {
            PENDING.remove(player.getUniqueId());
        } else {
            PENDING.put(player.getUniqueId(), remaining);
        }
        return claimed;
    }
}