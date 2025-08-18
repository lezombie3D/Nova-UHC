package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class FIreEnchantLess extends Scenario {
    @Override
    public String getName() {
        return "FireEnchantLess";
    }

    @Override
    public String getDescription() {
        return "Désactive les enchantements liés au feu (Fire Aspect, Flame).";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ENCHANTED_BOOK);
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (!isActive()) return;
        ItemStack item = event.getItem();
        if (item != null && (item.containsEnchantment(Enchantment.ARROW_FIRE) || item.containsEnchantment(Enchantment.FIRE_ASPECT))) {
            event.setCancelled(true);
            player.sendMessage("§cLes enchantements de feu sont désactivés dans ce scénario !");
        }
    }

    @Override
    public void onAnvilUse(ItemStack result, InventoryClickEvent event) {
        if (!isActive()) return;
        if (result.containsEnchantment(Enchantment.FIRE_ASPECT)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
        }
        if (result.containsEnchantment(Enchantment.ARROW_FIRE)) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
        }
    }

    @Override
    public void onEnchant(Player player, Enchantment enchant, ItemStack item, EnchantItemEvent event) {

        if (enchant.equals(Enchantment.ARROW_FIRE)) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
        }
        if (enchant.equals(Enchantment.FIRE_ASPECT)) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
        }

    }
}

