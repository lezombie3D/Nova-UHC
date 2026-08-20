package net.novaproject.novauhc.player.utils;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public final class InventorySnapshots {

    private static final String ARMOR = "armor";
    private static final String CONTENTS = "inventory";

    public static Map<String, ItemStack[]> savePlayerInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        Map<String, ItemStack[]> savedInventory = new HashMap<>();
        savedInventory.put(ARMOR, inv.getArmorContents());
        savedInventory.put(CONTENTS, inv.getContents());

        return savedInventory;
    }

    public static void restorePlayerInventory(Player player, Map<String, ItemStack[]> savedInventory) {
        if (savedInventory == null) return;

        PlayerInventory inv = player.getInventory();

        if (savedInventory.containsKey(ARMOR)) {
            inv.setArmorContents(savedInventory.get(ARMOR));
        }

        if (savedInventory.containsKey(CONTENTS)) {
            inv.setContents(savedInventory.get(CONTENTS));
        }
    }

    public static void clearPlayerInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
    }

    public static String getInventoryContentsAsString(Map<String, ItemStack[]> savedInventory, Player viewer) {
        if (savedInventory == null) {
            return LangManager.get().get(CoreLang.COMMON_INVENTORY_NONE, viewer);
        }
        return LangManager.get().get(CoreLang.COMMON_INVENTORY_ARMOR, viewer,
                        Map.of("%items%", describe(savedInventory.get(ARMOR), false, viewer)))
                + "\n"
                + LangManager.get().get(CoreLang.COMMON_INVENTORY_CONTENTS, viewer,
                        Map.of("%items%", describe(savedInventory.get(CONTENTS), true, viewer)));
    }

    private static String describe(ItemStack[] items, boolean withAmount, Player viewer) {
        if (items == null) return LangManager.get().get(CoreLang.COMMON_INVENTORY_EMPTY, viewer);
        StringJoiner joiner = new StringJoiner(", ");
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) continue;
            joiner.add(withAmount ? item.getAmount() + "x " + item.getType().name() : item.getType().name());
        }
        return joiner.length() == 0
                ? LangManager.get().get(CoreLang.COMMON_INVENTORY_EMPTY, viewer)
                : joiner.toString();
    }

    public static void dropAll(Map<String, ItemStack[]> savedInventory, Location location) {
        if (savedInventory == null || location == null || location.getWorld() == null) return;
        for (ItemStack[] items : savedInventory.values()) {
            if (items == null) continue;
            for (ItemStack item : items) {
                if (item == null || item.getType() == Material.AIR) continue;
                location.getWorld().dropItemNaturally(location, item);
            }
        }
    }

    public static void setRealHealth(int maxHealth, int currentHealth, Player player, int abso) {
        if (maxHealth <= 0) return;

        double heal = (20.0 * currentHealth) / maxHealth;
        heal = Math.max(1.0, Math.min(20.0, heal));

        player.setHealth(heal);

        double ratio = 250.0;
        int absorptionHP = (int) (abso / ratio * 2);

        EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.setAbsorptionHearts(absorptionHP);
    }
}

