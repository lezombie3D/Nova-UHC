package net.novaproject.novauhc.ability.craft;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public interface CraftableAbility {

    String getName();

    default String getCraftId() {
        return getName();
    }

    default String getCraftPermissionAbilityName() {
        return getName();
    }

    Material[] getCraftPattern();

    ItemStack getCraftResult();

    String getCraftName();

    String getCraftDescription();

    default void registerCraft() {
        CraftAbilityManager.register(this);
    }

    default void onCrafted(Player player, CraftItemEvent event) {

    }
}

