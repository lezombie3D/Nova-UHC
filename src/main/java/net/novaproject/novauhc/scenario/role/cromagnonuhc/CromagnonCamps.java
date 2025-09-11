package net.novaproject.novauhc.scenario.role.cromagnonuhc;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import net.novaproject.novauhc.scenario.role.camps.AbstractCamp;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.Material;

@RequiredArgsConstructor
public enum CromagnonCamps implements Camps {

    ZOMS("Village", "§a§l", Material.EMERALD, null, true),
    MAMOUT("Loups-Garous", "§c§l", Material.REDSTONE, null, true),
    DINOS("Solitaire", "§e§l", Material.DIAMOND, null, true),

    COUPLE("Couple", "§d§l", Material.RED_ROSE, DINOS, false),
    ANGE("Ange", "§b§l", Material.FEATHER, DINOS, false);

    @Delegate
    private final Camps delegate;

    CromagnonCamps(String name, String color, Material material, Camps parent, boolean mainCamp) {
        this.delegate = new AbstractCamp(name, color, material, parent, mainCamp) {
        };
    }
}

