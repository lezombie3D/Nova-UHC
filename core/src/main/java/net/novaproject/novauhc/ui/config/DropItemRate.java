package net.novaproject.novauhc.ui.config;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public enum DropItemRate {
    APPLE("Pommes", Material.APPLE, 0, 15),
    LEATHER("Cuirs", Material.LEATHER, 0, 0),
    FLINT("Silex", Material.FLINT, 0, 15),
    ARROW("Flèches", Material.ARROW, 0, 0),
    ENDERPEARL("Ender Pearls", Material.ENDER_PEARL, 0, 0),
    FEATHER("Plumes", Material.FEATHER, 0, 0),
    STRING("Fil", Material.STRING, 0, 0),
    GUNPOWDER("Poudre à Canon", Material.SULPHUR, 0, 0),
    BLAZE_ROD("Blaze Rod", Material.BLAZE_ROD, 0, 0),
    MAGMA_CREAM("Magma Cream", Material.MAGMA_CREAM, 0, 0);

    private final String name;

    private final Material material;

    private final int data;

    private int amount;

    DropItemRate(String name, Material material, int data, int amount) {
        this.name = name;
        this.material = material;
        this.data = data;
        this.amount = amount;
    }

    public void toggleAmount(ClickType clickType) {
        if (clickType == ClickType.LEFT) {
            if (this.amount >= 100) {
                this.amount = 0;
            } else {
                this.amount += 5;
            }
        } else if (clickType == ClickType.RIGHT) {
            if (this.amount <= 0) {
                this.amount = 100;
            } else {
                this.amount -= 5;
            }
        }
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, Math.min(100, amount));
    }

    public boolean roll() {
        return roll(0);
    }

    public boolean roll(int fallbackPercent) {
        int chance = this.amount > 0 ? this.amount : fallbackPercent;
        return chance > 0 && ThreadLocalRandom.current().nextInt(100) < chance;
    }

    public void dropAt(Location location) {
        location.getWorld().dropItemNaturally(location, new ItemStack(this.material, 1));
    }

    public boolean tryDropAt(Location location) {
        if (!roll()) return false;
        dropAt(location);
        return true;
    }
}

