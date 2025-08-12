package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.Common;
import org.bukkit.enchantments.Enchantment;

public enum Enchants {
    PROTECTION_ENVIRONMENTAL(Enchantment.PROTECTION_ENVIRONMENTAL, "§8┃ " + Common.get().getMainColor() + "Protection", 0, 50, 4),
    FIRE_PROTECTION(Enchantment.PROTECTION_FIRE, "§8┃ " + Common.get().getMainColor() + "Fire Protection", 0, 50, 4),
    FEATHER_FALLING(Enchantment.PROTECTION_FALL, "§8┃ " + Common.get().getMainColor() + "Feather Falling", 0, 50, 4),
    BLAST_PROTECTION(Enchantment.PROTECTION_EXPLOSIONS, "§8┃ " + Common.get().getMainColor() + "Blast Protection", 0, 50, 4),
    PROJECTILE_PROTECTION(Enchantment.PROTECTION_PROJECTILE, "§8┃ " + Common.get().getMainColor() + "Projectile Protection", 0, 50, 4),
    RESPIRATION(Enchantment.OXYGEN, "§8┃ " + Common.get().getMainColor() + "Respiration", 0, 50, 3),
    AQUA_AFFINITY(Enchantment.WATER_WORKER, "§8┃ " + Common.get().getMainColor() + "Aqua Affinity", 0, 50, 1),
    THORNS(Enchantment.THORNS, "§8┃ " + Common.get().getMainColor() + "Thorns", 0, 50, 3),
    DEPTH_STRIDERS(Enchantment.DEPTH_STRIDER, "§8┃ " + Common.get().getMainColor() + "Depth Strider", 0, 50, 3),
    SHARPNESS(Enchantment.DAMAGE_ALL, "§8┃ " + Common.get().getMainColor() + "Sharpness", 0, 50, 5),
    SMITE(Enchantment.DAMAGE_UNDEAD, "§8┃ " + Common.get().getMainColor() + "Smite", 0, 50, 5),
    BANE_OF_ARTHROPODS(Enchantment.DAMAGE_ARTHROPODS, "§8┃ " + Common.get().getMainColor() + "Bane of Arthropods", 0, 50, 5),
    KNOCKBACK(Enchantment.KNOCKBACK, "§8┃ " + Common.get().getMainColor() + "Knockback", 0, 50, 2),
    FIRE_ASPECT(Enchantment.FIRE_ASPECT, "§8┃ " + Common.get().getMainColor() + "Fire Aspect", 0, 50, 2),
    LOOTING(Enchantment.LOOT_BONUS_MOBS, "§8┃ " + Common.get().getMainColor() + "Looting", 0, 50, 3),
    EFFICIENCY(Enchantment.DIG_SPEED, "§8┃ " + Common.get().getMainColor() + "Efficiency", 0, 50, 5),
    SILK_TOUCH(Enchantment.SILK_TOUCH, "§8┃ " + Common.get().getMainColor() + "Silk Touch", 0, 1, 1),
    UNBREAKING(Enchantment.DURABILITY, "§8┃ " + Common.get().getMainColor() + "Unbreaking", 0, 50, 3),
    FORTUNE(Enchantment.LOOT_BONUS_BLOCKS, "§8┃ " + Common.get().getMainColor() + "Fortune", 0, 50, 3),
    POWER(Enchantment.ARROW_DAMAGE, "§8┃ " + Common.get().getMainColor() + "Power", 0, 50, 5),
    PUNCH(Enchantment.ARROW_KNOCKBACK, "§8┃ " + Common.get().getMainColor() + "Punch", 0, 50, 2),
    FLAME(Enchantment.ARROW_FIRE, "§8┃ " + Common.get().getMainColor() + "Flame", 0, 1, 1),
    INFINITY(Enchantment.ARROW_INFINITE, "§8┃ " + Common.get().getMainColor() + "Infinity", 0, 1, 1),
    LUCK_OF_THE_SEA(Enchantment.LUCK, "§8┃ " + Common.get().getMainColor() + "Luck of the Sea", 0, 100, 3),
    LURE(Enchantment.LURE, "§8┃ " + Common.get().getMainColor() + "Lure", 0, 100, 3);

    private final Enchantment enchantment;

    private final String name;

    private final int min;

    private final int max;

    private int configValue;

    Enchants(Enchantment enchantment, String name, int min, int max, int configValue) {
        this.enchantment = enchantment;
        this.name = name;
        this.min = min;
        this.max = max;
        this.configValue = configValue;
    }

    public static Enchants getEnchant(int pos) {
        Enchants[] enchants = values();
        if (pos < 0 || pos >= enchants.length) {
            return null;
        }
        return enchants[pos];
    }

    public static Enchants getEnchant(Enchantment enchant) {
        for (Enchants item : values()) {
            if (enchant.equals(item.getEnchantment()))
                return item;
        }
        return null;
    }

    public int getPosition() {
        return this.ordinal();
    }

    public Enchantment getEnchantment() {
        return this.enchantment;
    }

    public String getName() {
        return this.name;
    }

    public int getMin() {
        return this.min;
    }

    public int getMax() {
        return this.max;
    }

    public void addConfigValue() {
        if (this.configValue < this.enchantment.getMaxLevel())
            this.configValue++;
    }

    public void removeConfigValue() {
        if (this.configValue > 0)
            this.configValue--;
    }

    public int getConfigValue() {
        return this.configValue;
    }

    public void setConfigValue(int configValue) {
        this.configValue = configValue;
    }
}