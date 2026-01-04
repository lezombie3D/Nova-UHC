package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public enum DropItemRate {
    APPLE("Pommes", Material.APPLE, 0, 15),
    LEATHER("Cuirs", Material.LEATHER, 0, 0),
    FLINT("Silex", Material.FLINT, 0, 15),
    ARROW("Flèches", Material.ARROW, 0, 0),
    ENDERPEARL("Ender Pearls", Material.ENDER_PEARL, 0, 0);

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

    public ItemStack getItem() {
        return (new ItemCreator(this.material)).setDurability((short) this.data).setName(Common.get().getMainColor() + this.name + " §8(§a+" + this.amount + "%§8)").getItemstack();
    }

    public String getName() {
        return this.name;
    }

    public Material getMaterial() {
        return this.material;
    }

    public int getData() {
        return this.data;
    }

    public int getAmount() {
        return this.amount;
    }
}