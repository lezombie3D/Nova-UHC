package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.config.Potions;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

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
                event.getWhoClicked().sendMessage(Common.get().getInfoTag() + ChatColor.RED + "Golden Head Desactivée !");
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getEnchanter();

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        for (Enchantment enchant : event.getEnchantsToAdd().keySet()) {
            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onEnchant(player, enchant, item, event);
            });
            if (isDiamondArmor(item)) {
                if (enchant.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                    int level = event.getEnchantsToAdd().get(enchant);

                    if (isDiamondArmor(item) && level > uhcPlayer.getProtectionMax()) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "La protection sur l'armure en diamant est limitée au niveau 2!");
                        return;
                    }

                }
            }


            if (enchant.equals(Enchantment.DAMAGE_ALL)) {
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getSharpness()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.DAMAGE_ARTHROPODS)) { // Bane of Arthropods
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getBaneOfArthropods()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.DAMAGE_UNDEAD)) { // Smite
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getSmite()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.KNOCKBACK)) {
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getKnockback()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.FIRE_ASPECT)) {
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getFireAspect()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.LOOT_BONUS_MOBS)) { // Looting
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getLooting()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

// Bow Enchantments
            if (enchant.equals(Enchantment.ARROW_DAMAGE)) { // Power
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getPower()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.ARROW_KNOCKBACK)) { // Punch
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getPunch()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.ARROW_FIRE)) { // Flame
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getFlame()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.ARROW_INFINITE)) { // Infinity
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getInfinity()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

// Armor Enchantments
            if (enchant.equals(Enchantment.PROTECTION_ENVIRONMENTAL)) { // Protection
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getProtection()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.PROTECTION_FIRE)) { // Fire Protection
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getFireProtection()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.PROTECTION_EXPLOSIONS)) { // Blast Protection
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getBlastProtection()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.PROTECTION_PROJECTILE)) { // Projectile Protection
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getProjectileProtection()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.PROTECTION_FALL)) { // Feather Falling
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getFeatherFalling()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.OXYGEN)) { // Respiration
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getRespiration()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.WATER_WORKER)) { // Aqua Affinity
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getAquaAffinity()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.THORNS)) {
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getThorns()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

// Tool Enchantments
            if (enchant.equals(Enchantment.DIG_SPEED)) { // Efficiency
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getEfficiency()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.SILK_TOUCH)) {
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getSilkTouch()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.DURABILITY)) { // Unbreaking
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getUnbreaking()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.LOOT_BONUS_BLOCKS)) { // Fortune
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getFortune()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

// Fishing Rod Enchantments
            if (enchant.equals(Enchantment.LUCK)) { // Luck of the Sea
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getLuckOfTheSea()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }

            if (enchant.equals(Enchantment.LURE)) {
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getLure()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }
            if (enchant.equals(Enchantment.DEPTH_STRIDER)) {
                int level = event.getEnchantsToAdd().get(enchant);
                if (level > uhcPlayer.getDepthStrider()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }

        if (event.getSlotType() != InventoryType.SlotType.RESULT) {
            return;
        }
        ItemStack result = event.getCurrentItem();
        if (result == null) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onAnvilUse(result, event);
        });

        if (isDiamondArmor(result)) {
            if (result.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) {
                int level = result.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);

                if (level > uhcPlayer.getProtectionMax()) {
                    event.setCancelled(true);

                    player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                    return;
                }
            }
        }

        if (result.containsEnchantment(Enchantment.DAMAGE_ALL)) { // Sharpness
            int level = result.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
            if (level > uhcPlayer.getSharpness()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.DAMAGE_ARTHROPODS)) { // Bane of Arthropods
            int level = result.getEnchantmentLevel(Enchantment.DAMAGE_ARTHROPODS);
            if (level > uhcPlayer.getBaneOfArthropods()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.DAMAGE_UNDEAD)) { // Smite
            int level = result.getEnchantmentLevel(Enchantment.DAMAGE_UNDEAD);
            if (level > uhcPlayer.getSmite()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.KNOCKBACK)) {
            int level = result.getEnchantmentLevel(Enchantment.KNOCKBACK);
            if (level > uhcPlayer.getKnockback()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.FIRE_ASPECT)) {
            int level = result.getEnchantmentLevel(Enchantment.FIRE_ASPECT);
            if (level > uhcPlayer.getFireAspect()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) { // Looting
            int level = result.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
            if (level > uhcPlayer.getLooting()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }
        if (result.containsEnchantment(Enchantment.ARROW_DAMAGE)) { // Power
            int level = result.getEnchantmentLevel(Enchantment.ARROW_DAMAGE);
            if (level > uhcPlayer.getPower()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) { // Punch
            int level = result.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK);
            if (level > uhcPlayer.getPunch()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.ARROW_FIRE)) { // Flame
            int level = result.getEnchantmentLevel(Enchantment.ARROW_FIRE);
            if (level > uhcPlayer.getFlame()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.ARROW_INFINITE)) { // Infinity
            int level = result.getEnchantmentLevel(Enchantment.ARROW_INFINITE);
            if (level > uhcPlayer.getInfinity()) {
                event.setCancelled(true);

                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

// Armor Enchantments
        if (result.containsEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)) { // Protection
            int level = result.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
            if (level > uhcPlayer.getProtection()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.PROTECTION_FIRE)) { // Fire Protection
            int level = result.getEnchantmentLevel(Enchantment.PROTECTION_FIRE);
            if (level > uhcPlayer.getFireProtection()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.PROTECTION_EXPLOSIONS)) { // Blast Protection
            int level = result.getEnchantmentLevel(Enchantment.PROTECTION_EXPLOSIONS);
            if (level > uhcPlayer.getBlastProtection()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.PROTECTION_PROJECTILE)) { // Projectile Protection
            int level = result.getEnchantmentLevel(Enchantment.PROTECTION_PROJECTILE);
            if (level > uhcPlayer.getProjectileProtection()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.PROTECTION_FALL)) { // Feather Falling
            int level = result.getEnchantmentLevel(Enchantment.PROTECTION_FALL);
            if (level > uhcPlayer.getFeatherFalling()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.OXYGEN)) { // Respiration
            int level = result.getEnchantmentLevel(Enchantment.OXYGEN);
            if (level > uhcPlayer.getRespiration()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.WATER_WORKER)) { // Aqua Affinity
            int level = result.getEnchantmentLevel(Enchantment.WATER_WORKER);
            if (level > uhcPlayer.getAquaAffinity()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.THORNS)) {
            int level = result.getEnchantmentLevel(Enchantment.THORNS);
            if (level > uhcPlayer.getThorns()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

// Tool Enchantments
        if (result.containsEnchantment(Enchantment.DIG_SPEED)) { // Efficiency
            int level = result.getEnchantmentLevel(Enchantment.DIG_SPEED);
            if (level > uhcPlayer.getEfficiency()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.SILK_TOUCH)) {
            int level = result.getEnchantmentLevel(Enchantment.SILK_TOUCH);
            if (level > uhcPlayer.getSilkTouch()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.DURABILITY)) { // Unbreaking
            int level = result.getEnchantmentLevel(Enchantment.DURABILITY);
            if (level > uhcPlayer.getUnbreaking()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) { // Fortune
            int level = result.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            if (level > uhcPlayer.getFortune()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

// Fishing Rod Enchantments
        if (result.containsEnchantment(Enchantment.LUCK)) { // Luck of the Sea
            int level = result.getEnchantmentLevel(Enchantment.LUCK);
            if (level > uhcPlayer.getLuckOfTheSea()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
                return;
            }
        }

        if (result.containsEnchantment(Enchantment.LURE)) {
            int level = result.getEnchantmentLevel(Enchantment.LURE);
            if (level > uhcPlayer.getLure()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);
            }
        }

        if (result.containsEnchantment(Enchantment.DEPTH_STRIDER)) { // Efficiency
            int level = result.getEnchantmentLevel(Enchantment.DEPTH_STRIDER);
            if (level > uhcPlayer.getDepthStrider()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.CAT_HIT, 10, 10);

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
                    player.sendMessage("§cCette potion est désactivée.");
                    player.closeInventory();
                }
            }
        }
    }

}
