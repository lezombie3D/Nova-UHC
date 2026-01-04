package net.novaproject.novauhc.scenario.role.monsterhunter.mhdragonfall;

import lombok.experimental.Delegate;
import net.novaproject.novauhc.scenario.role.camps.AbstractCamp;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.Material;

public enum Tiers implements Camps {

    SS("SS", "§6§l", Material.NETHER_STAR, null, true),
    S("S", "§2§l", Material.TNT, null, true),
    A("A", "§a§l", Material.BEACON, null, true),
    B("B", "§c§l", Material.CACTUS, null, true),
    C("C", "§3§l", Material.BUCKET, null, true);

    @Delegate
    private final Camps delegate;

    Tiers(String name, String color, Material material, Camps parent, boolean mainCamp) {
        this.delegate = new AbstractCamp(name, color, material, parent, mainCamp) {
        };
    }
}
