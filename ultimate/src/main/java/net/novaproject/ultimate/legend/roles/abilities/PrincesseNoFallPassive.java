package net.novaproject.ultimate.legend.roles.abilities;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PrincesseNoFallPassive extends PassiveAbility {

    public PrincesseNoFallPassive() { setCooldown(0); }

    @Override public String getName() { return "Grâce Royale"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {

        return true;
    }
}

