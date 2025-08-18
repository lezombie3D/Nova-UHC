package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.ui.config.Potions;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.material.MaterialData;

import java.util.Map;
import java.util.stream.Collectors;

public class PlayerCraftEvent implements Listener {

    public UHCManager uhcManager = UHCManager.get();


    @EventHandler
    public void onCraft(CraftItemEvent event){

        ItemStack item = event.getRecipe().getResult();
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onCraft(item, event);
        });
        ShapedRecipe goldenHead = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE).setName(ChatColor.GOLD + "Golden Head").getItemstack());
        goldenHead.shape("GGG", "GHG", "GGG");
        goldenHead.setIngredient('G', Material.GOLD_INGOT);
        goldenHead.setIngredient('H', new MaterialData(Material.SKULL_ITEM, (byte) 3));
        GoldenHead golden = ScenarioManager.get().getScenario(GoldenHead.class);

        if (golden.isActive()) {
            if (item == goldenHead.getResult()) {
                CommonString.BLOCKED_CRAFT_ITEM.send((Player) event.getWhoClicked());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void EnchantItemEvent(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (player == null || event.getItem() == null)
            return;

        if (containsBlockedEnchant(uhcPlayer, event.getEnchantsToAdd())) {

            if (isDiamondArmor(event.getItem()) && event.getEnchantsToAdd().containsKey(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                int level = event.getEnchantsToAdd().get(Enchantment.PROTECTION_ENVIRONMENTAL);
                if (level <= uhcPlayer.getProtectionMax()) {
                    return;
                }
            }

            event.setCancelled(true);
            CommonString.BLOCKED_ENCHANT.send(player);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onAnvil(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;
        Player player = (Player) event.getWhoClicked();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        Inventory inv = event.getClickedInventory();
        if (!(inv instanceof AnvilInventory))
            return;
        AnvilInventory anvil = (AnvilInventory) inv;
        InventoryView view = event.getView();
        int rawSlot = event.getRawSlot();
        if (rawSlot != view.convertSlot(rawSlot) || rawSlot != 2)
            return;
        ItemStack result = anvil.getItem(2);
        if (result == null || result.getEnchantments() == null)
            return;
        if (containsBlockedEnchant(uhcPlayer, result.getEnchantments())) {
            if (isDiamondArmor(result) && result.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                int level = result.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
                if (level > uhcPlayer.getProtectionMax()) {
                    result.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL);
                    CommonString.EXEDED_LIMITE.send(player);
                }
            } else {
                getBlockedEnchant(uhcPlayer, result.getEnchantments())
                        .keySet()
                        .forEach(result::removeEnchantment);
                CommonString.BLOCKED_ENCHANT.send(player);
            }
        }
    }


    private boolean isDiamondArmor(ItemStack item) {
        Material type = item.getType();
        return type == Material.DIAMOND_HELMET ||
                type == Material.DIAMOND_CHESTPLATE ||
                type == Material.DIAMOND_LEGGINGS ||
                type == Material.DIAMOND_BOOTS;
    }


    @EventHandler
    public void onPrepareFurnaceItem(FurnaceSmeltEvent event) {
        ItemStack item = event.getResult();
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onFurnace(item, event);
        });
    }

    @EventHandler
    public void onBrew(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!(event.getInventory() instanceof org.bukkit.inventory.BrewerInventory))
            return;
        int slot = event.getRawSlot();
        if (slot == 3) {
            ItemStack result = event.getInventory().getItem(slot);
            if (result == null)
                return;
            for (Potions potion : Potions.values()) {
                if (result.getType() == potion.getMaterial() && !potion.isEnabled()) {
                    event.getInventory().remove(result);
                    event.setCancelled(true);
                    CommonString.BLOCKED_POTION.send(player);
                    player.closeInventory();
                }
            }
        }
    }

    private boolean containsBlockedEnchant(UHCPlayer player, Map<Enchantment, Integer> enchantments) {
        return enchantments.entrySet().stream().anyMatch(x -> isBlockedEnchant(player, x.getKey(), x.getValue()));
    }

    private Map<Enchantment, Integer> getBlockedEnchant(UHCPlayer player, Map<Enchantment, Integer> enchantments) {
        return enchantments.entrySet().stream().filter(x -> isBlockedEnchant(player, x.getKey(), x.getValue().intValue())).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    private boolean isBlockedEnchant(UHCPlayer player, Enchantment enchant, int value) {
        Enchants ench = Enchants.getEnchant(enchant);
        return (player.getEnchantLimits().get(ench) < value);
    }

}
