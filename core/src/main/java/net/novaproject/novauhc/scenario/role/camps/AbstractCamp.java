package net.novaproject.novauhc.scenario.role.camps;

import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractCamp implements Camps {

    private final String name;
    private final DyeColor dyeColor;
    private final Camps parent;
    private final boolean mainCamp;

    public AbstractCamp(String name, DyeColor dyeColor) {
        this(name, dyeColor, null, true);
    }

    public AbstractCamp(String name, DyeColor dyeColor, Camps parent, boolean mainCamp) {
        this.name = name;
        this.dyeColor = dyeColor;
        this.parent = parent;
        this.mainCamp = mainCamp;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DyeColor getDyeColor() {
        return dyeColor;
    }

    @Override
    public String getColor() {
        return CampUtils.CampColors.chatCode(dyeColor) + "§l";
    }

    @Override
    public ItemStack getItem() {
        return new ItemCreator(CampUtils.CampColors.dyeItem(dyeColor))
                .setName(getColor() + name)
                .getItemstack();
    }

    @Override
    public ItemStack getSkull() {
        return CampUtils.CampColors.skull(dyeColor);
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
