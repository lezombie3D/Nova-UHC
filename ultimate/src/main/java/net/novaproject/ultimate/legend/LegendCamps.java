package net.novaproject.ultimate.legend;

import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public enum LegendCamps implements Camps {

    LEGEND("Légende", "§6");

    private final String name;
    private final String color;

    LegendCamps(String name, String color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public ItemStack getItem() {
        return new ItemCreator(Material.NETHER_STAR).setName(color+name).getItemstack();
    }

    @Override
    public boolean isMainCamp() {
        return true;
    }

    @Override
    public Camps getParent() {
        return null;
    }
}