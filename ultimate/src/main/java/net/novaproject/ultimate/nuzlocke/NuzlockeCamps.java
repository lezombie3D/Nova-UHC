package net.novaproject.ultimate.nuzlocke;

import lombok.experimental.Delegate;
import net.novaproject.novauhc.scenario.role.camps.AbstractCamp;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.DyeColor;

public enum NuzlockeCamps implements Camps {

    NUZLOCKE("Nuzlocke", DyeColor.ORANGE);

    @Delegate private final Camps delegate;

    NuzlockeCamps(String name, DyeColor color) {
        this.delegate = new AbstractCamp(name, color) {};
    }
}
