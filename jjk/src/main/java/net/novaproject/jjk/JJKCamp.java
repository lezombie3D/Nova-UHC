package net.novaproject.jjk;

import lombok.experimental.Delegate;
import net.novaproject.novauhc.scenario.role.camps.AbstractCamp;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.Material;

public enum JJKCamp implements Camps {

    SOLITAIRE("Solitaire", "§6", Material.ACACIA_FENCE, null, true),

    EXORCISTES("Exorcistes", "§a", Material.IRON_SWORD, null, true),

    FLEAUX("Fléaux", "§c", Material.BLAZE_ROD, null, true),

    DUO("Duo", "§d", Material.GOLD_ORE, null, true),

    ZENIN("Zenin", "§e", Material.ACACIA_DOOR, null, true);

    @Delegate
    private final Camps delegate;

    JJKCamp(String name, String color, Material material, Camps parent, boolean mainCamp) {
        this.delegate = new AbstractCamp(name, color, material, parent, mainCamp) {};
    }
}