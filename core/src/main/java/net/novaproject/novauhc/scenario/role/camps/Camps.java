package net.novaproject.novauhc.scenario.role.camps;

import org.bukkit.inventory.ItemStack;

public interface Camps {
    String getName();

    String getColor();

    ItemStack getItem();

    boolean isMainCamp();

    Camps getParent();
}

