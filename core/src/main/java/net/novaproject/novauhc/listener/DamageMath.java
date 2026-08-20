package net.novaproject.novauhc.listener;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class DamageMath {


    public static double getAttackDamageItem(ItemStack stack) {
        if (stack != null && stack.getType() != null && stack.getType() != Material.AIR) {
            double baseDamage;
            switch (stack.getType()) {
                case WOOD_SPADE:
                case WOOD_HOE:
                case GOLD_SPADE:
                case GOLD_HOE:
                    baseDamage = 1.0;
                    break;
                case STONE_HOE:
                case STONE_SPADE:
                case WOOD_PICKAXE:
                case GOLD_PICKAXE:
                    baseDamage = 2.0;
                    break;
                case STONE_PICKAXE:
                case IRON_SPADE:
                case IRON_HOE:
                case WOOD_AXE:
                case GOLD_AXE:
                    baseDamage = 3.0;
                    break;
                case STONE_AXE:
                case IRON_PICKAXE:
                case DIAMOND_SPADE:
                case DIAMOND_HOE:
                case WOOD_SWORD:
                case GOLD_SWORD:
                    baseDamage = 4.0;
                    break;
                case IRON_AXE:
                case DIAMOND_PICKAXE:
                case STONE_SWORD:
                    baseDamage = 5.0;
                    break;
                case DIAMOND_AXE:
                case IRON_SWORD:
                    baseDamage = 6.0;
                    break;
                case DIAMOND_SWORD:
                    baseDamage = 7.0;
                    break;
                default:
                    baseDamage = 0.0;
            }
            return 1.0 + baseDamage;
        } else {
            return 1.0;
        }
    }

    public static double getEnchantDamageItem(ItemStack stack) {
        if (stack != null && stack.getType() != null && stack.getType() != Material.AIR) {
            double enchantDamage = 0.0;
            if (stack.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                enchantDamage = 1.25 * (double) stack.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
            }
            return enchantDamage;
        } else {
            return 0.0;
        }
    }

    public static double getStrengthLevel(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
            return 0.0;
        }
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect != null && effect.getType() != null && effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
                return 1.0 + (double) effect.getAmplifier();
            }
        }
        return 0.0;
    }

    public static boolean isCriticalDamage(Player player) {
        return player.getFallDistance() > 0.0F && !player.isOnGround()
                && !player.hasPotionEffect(PotionEffectType.BLINDNESS) && player.getVehicle() == null;
    }

    public static int getArmorPoints(Player player) {
        int points = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            points += armorValue(piece);
        }
        return points;
    }

    private static int armorValue(ItemStack piece) {
        if (piece == null || piece.getType() == Material.AIR) return 0;
        switch (piece.getType()) {
            case LEATHER_HELMET: case LEATHER_BOOTS: case GOLD_BOOTS: case CHAINMAIL_BOOTS: return 1;
            case LEATHER_LEGGINGS: case GOLD_HELMET: case CHAINMAIL_HELMET: case IRON_BOOTS: return 2;
            case LEATHER_CHESTPLATE: case GOLD_LEGGINGS: case DIAMOND_HELMET: case DIAMOND_BOOTS: return 3;
            case CHAINMAIL_LEGGINGS: case IRON_HELMET: return 4;
            case CHAINMAIL_CHESTPLATE: case GOLD_CHESTPLATE: case IRON_LEGGINGS: return 5;
            case IRON_CHESTPLATE: case DIAMOND_LEGGINGS: return 6;
            case DIAMOND_CHESTPLATE: return 8;
            default: return 0;
        }
    }

    public static int getProtectionEpf(Player player) {
        int epf = 0;
        for (ItemStack piece : player.getInventory().getArmorContents()) {
            if (piece != null) epf += piece.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        }
        return epf;
    }

    public static double armorReducedHearts(Player victim, double hearts) {
        double dmg = hearts * 2.0;
        int armor = Math.min(20, getArmorPoints(victim));
        dmg = dmg * (25 - armor) / 25.0;
        int epf = Math.min(20, getProtectionEpf(victim));
        dmg = dmg * (1.0 - epf * 0.04);
        return dmg / 2.0;
    }
}

