package net.novaproject.novauhc.scenario.special.legend.utils;

import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

/**
 * Classe utilitaire pour créer et gérer tous les items spéciaux des légendes.
 * Centralise la création d'items pour éviter la duplication de code.
 *
 * @author NovaProject
 * @version 2.0
 */
public class LegendItems {

    // === ITEMS DE POUVOIR ===

    /**
     * Crée l'item de pouvoir standard (Nether Star)
     *
     * @param legendName Le nom de la légende
     * @return L'item de pouvoir
     */
    public static ItemStack createPowerItem(String legendName) {
        return new ItemCreator(Material.NETHER_STAR)
                .setName("§6Pouvoir de " + legendName)
                .addLore("§7Clic droit pour activer")
                .getItemstack();
    }

    /**
     * Crée l'item de pouvoir générique
     *
     * @return L'item de pouvoir générique
     */
    public static ItemStack createGenericPowerItem() {
        return new ItemCreator(Material.NETHER_STAR)
                .setName("§6Pouvoir")
                .addLore("§7Clic droit pour activer")
                .getItemstack();
    }

    // === ITEMS SPÉCIALISÉS ===

    /**
     * Crée l'arc de l'Archer (choix 1)
     *
     * @return L'arc avec Infinity
     */
    public static ItemStack createArcherBowInfinity() {
        return new ItemCreator(Material.BOW)
                .addEnchantment(Enchantment.ARROW_DAMAGE, 3)
                .addEnchantment(Enchantment.ARROW_INFINITE, 1)
                .setName("§f§lArc de Lumière")
                .setLores(Arrays.asList("§7Flèches infinies", "§7Power III"))
                .setUnbreakable(true)
                .getItemstack();
    }

    /**
     * Crée l'arc de l'Archer (choix 2)
     *
     * @return L'arc avec Power IV
     */
    public static ItemStack createArcherBowPower() {
        return new ItemCreator(Material.BOW)
                .addEnchantment(Enchantment.ARROW_DAMAGE, 4)
                .setName("§f§lArc de Lumière")
                .addLore("§7Power IV")
                .setUnbreakable(true)
                .getItemstack();
    }

    /**
     * Crée la lame secrète de l'Assassin
     *
     * @return La lame secrète
     */
    public static ItemStack createSecretBlade() {
        return new ItemCreator(Material.IRON_SWORD)
                .addEnchantment(Enchantment.DAMAGE_ALL, 1)
                .setName("§8§lLame Secrète")
                .setLores(Arrays.asList("§7Bonus de force quand vous", "§7avez moins de 2 épées"))
                .setUnbreakable(true)
                .getItemstack();
    }

    // === POTIONS DU MAGE ===

    /**
     * Crée une potion de force
     *
     * @return La potion de force
     */
    public static ItemStack createStrengthPotion() {
        return new ItemCreator(Material.POTION)
                .addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 30, 0), true)
                .setName("§cPotion de Force")
                .getItemstack();
    }

    /**
     * Crée une potion de résistance
     *
     * @return La potion de résistance
     */
    public static ItemStack createResistancePotion() {
        return new ItemCreator(Material.POTION)
                .addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 30, 0), true)
                .setName("§7Potion de Résistance")
                .getItemstack();
    }

    /**
     * Crée une potion de vitesse
     *
     * @return La potion de vitesse
     */
    public static ItemStack createSpeedPotion() {
        return new ItemCreator(Material.POTION)
                .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 30, 0), true)
                .setName("§bPotion de Vitesse")
                .getItemstack();
    }

    /**
     * Fait apparaître les potions du Mage
     *
     * @param player Le joueur
     */
    public static void dropMagePotions(Player player) {
        player.getWorld().dropItemNaturally(player.getLocation(), createStrengthPotion()).setPickupDelay(0);
        player.getWorld().dropItemNaturally(player.getLocation(), createResistancePotion()).setPickupDelay(0);
        player.getWorld().dropItemNaturally(player.getLocation(), createSpeedPotion()).setPickupDelay(0);
    }

    // === ÉQUIPEMENT DE BASE ===

    /**
     * Donne l'équipement de base d'une marionnette
     *
     * @param player Le joueur marionnette
     */
    public static void givePuppetEquipment(Player player) {
        PlayerInventory inv = player.getInventory();

        // Armure
        inv.setBoots(new ItemCreator(Material.IRON_BOOTS).setUnbreakable(true).getItemstack());
        inv.setLeggings(new ItemCreator(Material.IRON_LEGGINGS).setUnbreakable(true).getItemstack());
        inv.setChestplate(new ItemCreator(Material.IRON_CHESTPLATE)
                .addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1)
                .setUnbreakable(true).getItemstack());
        inv.setHelmet(new ItemCreator(Material.IRON_HELMET).setUnbreakable(true).getItemstack());

        // Armes et outils
        inv.addItem(new ItemCreator(Material.WOOD_SWORD)
                .addEnchantment(Enchantment.DAMAGE_ALL, 2)
                .setUnbreakable(true).getItemstack());

        inv.addItem(new ItemCreator(Material.BOW).setUnbreakable(true).getItemstack());
        inv.addItem(new ItemCreator(Material.ARROW).setAmount(32).getItemstack());

        // Nourriture et ressources
        inv.addItem(new ItemCreator(Material.COOKED_BEEF).setAmount(64).getItemstack());
        inv.addItem(new ItemCreator(Material.GOLDEN_APPLE).setAmount(5).getItemstack());
        inv.addItem(new ItemCreator(Material.COBBLESTONE).setAmount(128).getItemstack());

        inv.addItem(new ItemCreator(Material.IRON_PICKAXE)
                .addEnchantment(Enchantment.DIG_SPEED, 2)
                .setUnbreakable(true).getItemstack());

        player.updateInventory();
    }

    // === UTILITAIRES ===

    /**
     * Vérifie si un item est une épée
     *
     * @param material Le matériau à vérifier
     * @return true si c'est une épée
     */
    public static boolean isSword(Material material) {
        return material == Material.WOOD_SWORD ||
                material == Material.STONE_SWORD ||
                material == Material.IRON_SWORD ||
                material == Material.GOLD_SWORD ||
                material == Material.DIAMOND_SWORD;
    }

    /**
     * Compte le nombre d'items d'un type dans l'inventaire
     *
     * @param player   Le joueur
     * @param material Le matériau à compter
     * @return Le nombre d'items
     */
    public static int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Compte le nombre d'épées dans l'inventaire
     *
     * @param player Le joueur
     * @return Le nombre d'épées
     */
    public static int countSwords(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isSword(item.getType())) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Vérifie si un joueur a la lame secrète
     *
     * @param player Le joueur
     * @return true si le joueur a la lame secrète
     */
    public static boolean hasSecretBlade(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                if (item.getItemMeta().getDisplayName().equals("§8§lLame Secrète")) {
                    return true;
                }
            }
        }
        return false;
    }
}
