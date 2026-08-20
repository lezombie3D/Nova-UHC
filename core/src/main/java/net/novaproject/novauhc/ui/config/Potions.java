package net.novaproject.novauhc.ui.config;

import org.bukkit.Material;

import static org.bukkit.Material.*;

public enum Potions {
    SPEED("Speed", POTION, 8258, SUGAR),
    STRENGHT("Strenght", POTION, 8233, BLAZE_POWDER),
    JUMP_BOOST("Jump Boost", POTION, 8267, RABBIT_FOOT),
    POISON("Poison", POTION, 8260, SPIDER_EYE),
    FIRE_RESISTANCE("Fire Resistance", POTION, 8259, MAGMA_CREAM),
    HEAL("Instant Health", POTION, 8229, SPECKLED_MELON),
    REGENERATION("Regeneration", POTION, 8257, GHAST_TEAR),
    INVISIBILITY("Invisibility", POTION, 8270, GOLDEN_CARROT),
    OTHER("Autres", POTION, 8236, FERMENTED_SPIDER_EYE),
    SPLASH("Splash", SULPHUR, 0, SULPHUR),
    LEVEL_II("Niveau II", GLOWSTONE_DUST, 0, GLOWSTONE_DUST),
    LONG_DURATION("Longue durée", REDSTONE, 0, REDSTONE);

    private final String name;
    private final Material itemMaterial;
    private final int idItem;
    private final Material material;
    private boolean enabled = true;

    Potions(String name, Material itemMaterial, int idItem, Material material) {
        this.name = name;
        this.itemMaterial = itemMaterial;
        this.idItem = idItem;
        this.material = material;
    }

    public Material getMaterial() {
        return this.material;
    }

    public Material getItemMaterial() {
        return this.itemMaterial;
    }

    public int getIdItem() {
        return this.idItem;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }
}

