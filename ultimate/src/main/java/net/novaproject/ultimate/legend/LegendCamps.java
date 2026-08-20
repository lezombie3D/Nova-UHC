package net.novaproject.ultimate.legend;

import lombok.experimental.Delegate;
import net.novaproject.novauhc.scenario.role.camps.AbstractCamp;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import org.bukkit.DyeColor;

public enum LegendCamps implements Camps {

    LEGEND("Légende", DyeColor.ORANGE);

    @Delegate private final Camps delegate;

    LegendCamps(String name, DyeColor color) {
        this.delegate = new AbstractCamp(name, color) {};
    }
}
