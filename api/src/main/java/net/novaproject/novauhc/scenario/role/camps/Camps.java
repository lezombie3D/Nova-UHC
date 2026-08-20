package net.novaproject.novauhc.scenario.role.camps;

import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;

public interface Camps {
    String getName();

    String getColor();

    ItemStack getItem();

    boolean isMainCamp();

    Camps getParent();

    default DyeColor getDyeColor() {
        return null;
    }

    default ItemStack getSkull() {
        return getItem();
    }

    default boolean is(Camps other) {
        if (other == null) return false;
        String a = this.getName();
        String b = other.getName();
        return a != null && a.equals(b);
    }

    default boolean isOrHasParent(Camps other) {
        Camps current = this;
        int guard = 0;
        while (current != null && guard++ < 64) {
            if (current.is(other)) return true;
            current = current.getParent();
        }
        return false;
    }
}
