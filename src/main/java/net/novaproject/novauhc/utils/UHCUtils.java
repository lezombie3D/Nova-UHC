package net.novaproject.novauhc.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.novaproject.novauhc.Common;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.util.*;

public class UHCUtils {

    public static ItemStack createCustomHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64Texture));

        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        head.setItemMeta(meta);
        return head;
    }

    public static Map<String, ItemStack[]> savePlayerInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        Map<String, ItemStack[]> savedInventory = new HashMap<>();
        savedInventory.put("armor", inv.getArmorContents());
        savedInventory.put("inventory", inv.getContents());

        return savedInventory;
    }

    public static void applyInfiniteEffects(PotionEffect[] effects, Player player) {
        for (PotionEffect activeEffect : effects) {
            player.getPlayer().removePotionEffect(activeEffect.getType());
        }
        for (PotionEffect effect : effects) {
            effect.apply(player);
        }
    }

    public static String getInventoryContentsAsString(Map<String, ItemStack[]> savedInventory) {
        if (savedInventory == null) return "Aucun inventaire sauvegardé.";

        StringBuilder content = new StringBuilder("Inventaire sauvegardé :\n");

        content.append(" - Armure : ");
        if (savedInventory.containsKey("armor")) {
            for (ItemStack item : savedInventory.get("armor")) {
                if (item != null && item.getType() != Material.AIR) {
                    content.append(item.getType().name()).append(", ");
                }
            }
        }
        content.append("\n");
        content.append(" - Contenu : ");
        if (savedInventory.containsKey("inventory")) {
            for (ItemStack item : savedInventory.get("inventory")) {
                if (item != null && item.getType() != Material.AIR) {
                    content.append(item.getAmount()).append("x ").append(item.getType().toString()).append(", ");
                }
            }
        }

        return content.toString();
    }

    public static void giveLobbyItems(Player player) {
        player.getInventory().setItem(0, Common.get().getTeamItem().getItemstack());
        player.getInventory().setItem(2, Common.get().getActiveRole().getItemstack());
        player.getInventory().setItem(6, Common.get().getActiveScenario().getItemstack());
        if (player.hasPermission("novauhc.host") || player.hasPermission("novauhc.cohost")) {
            player.getInventory().setItem(4, Common.get().getConfigItem().getItemstack());
            player.getInventory().setItem(8, Common.get().getReglesItem().getItemstack());
        }


    }

    public static void restorePlayerInventory(Player player, Map<String, ItemStack[]> savedInventory) {
        if (savedInventory == null) return;

        PlayerInventory inv = player.getInventory();

        if (savedInventory.containsKey("armor")) {
            inv.setArmorContents(savedInventory.get("armor"));
        }

        if (savedInventory.containsKey("inventory")) {
            inv.setContents(savedInventory.get("inventory"));
        }
    }

    public static void clearPlayerInventory(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

    }

    public static ItemCreator createCustomButon(String texture, String name, List<String> lore) {
        ItemCreator item = new ItemCreator(createCustomHead(texture));
        item.setName(name);
        if (lore != null) item.setLores(lore);
        return item;
    }

    public static String getFormattedTime(int seconds) {
        return seconds >= 3600
                ? String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60)
                : String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    public static ItemCreator redMinus(List<String> lore, String name) {

        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=;";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator greenPlus(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkwZjYyZWM1ZmEyZTkzZTY3Y2YxZTAwZGI4YWY0YjQ3YWM3YWM3NjlhYTA5YTIwM2ExZjU3NWExMjcxMGIxMCJ9fX0=";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator arrowDroite(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzkwZjYyZWM1ZmEyZTkzZTY3Y2YxZTAwZGI4YWY0YjQ3YWM3YWM3NjlhYTA5YTIwM2ExZjU3NWExMjcxMGIxMCJ9fX0=";
        return createCustomButon(text, name, lore);
    }

    public static ItemCreator arrowGauche(List<String> lore, String name) {
        String text = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19";
        return createCustomButon(text, name, lore);
    }

    public static String calculPourcentage(double valeur, double total) {
        if (total <= 0) return "0.0%";
        double pourcentage = (valeur / total) * 100;
        return String.format("%.1f%%", pourcentage);
    }

    public static ItemCreator getReset() {
        return new ItemCreator(Material.REDSTONE).setName(ChatColor.RED + "Réinitialisée les valeur.");
    }

    public static List<String> getLoreForCLique(String left, String rigth) {
        return Arrays.asList("", ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + left, ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + rigth, "");

    }
}
