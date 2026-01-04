package net.novaproject.novauhc.scenario.role.camps;

import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;


public abstract class AbstractCamp implements Camps {

    private final String name;
    private final String color;
    private final Material material;
    private final Camps parent;
    private final boolean mainCamp;

    public AbstractCamp(String name, String color, Material material) {
        this(name, color, material, null, true);
    }

    public AbstractCamp(String name, String color, Material material, Camps parent, boolean mainCamp) {
        this.name = name;
        this.color = color;
        this.material = material;
        this.parent = parent;
        this.mainCamp = mainCamp;
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
        return new ItemCreator(material)
                .setName(color + name)
                .getItemstack();
    }

    @Override
    public Camps getParent() {
        return parent;
    }

    @Override
    public boolean isMainCamp() {
        return mainCamp;
    }
}
