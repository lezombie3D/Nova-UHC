package net.novaproject.novauhc.scenario.role.loupgarouuhc;

import lombok.experimental.Delegate;
import net.novaproject.novauhc.scenario.role.camps.AbstractCamp;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.Material;

public enum LGCamps implements Camps {

    VILLAGE("Village", "§a§l", Material.EMERALD, null, true),
    LOUP_GAROU("Loups-Garous", "§c§l", Material.REDSTONE, null, true),
    SOLITAIRE("Solitaire", "§e§l", Material.DIAMOND, null, true),

    COUPLE("Couple", "§d§l", Material.RED_ROSE, SOLITAIRE, false),
    ANGE("Ange", "§b§l", Material.FEATHER, SOLITAIRE, false),
    ASSASIN("Assasin", "§b§l", Material.DIAMOND, SOLITAIRE, false),
    ;


    @Delegate
    private final Camps delegate;

    LGCamps(String name, String color, Material material, Camps parent, boolean mainCamp) {
        this.delegate = new AbstractCamp(name, color, material, parent, mainCamp) {
        };
    }


}
