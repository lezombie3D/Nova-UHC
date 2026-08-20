package net.novaproject.ultimate.nuzlocke.roles.steel;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.MeleeAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class SteelMagnetMelee extends MeleeAbility {

    @Var(name = "Nuzlocke Steel Magnet Chance", type = VariableType.PERCENTAGE)
    private double procChance = 0.75;

    @Var(name = "Nuzlocke Steel Magnet Strength", type = VariableType.DOUBLE)
    private double magnetStrength = 1.5;

    @Override public String getName() { return "Aimant"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (getTarget() == null) return false;
        Player victim = getTarget().getPlayer();
        if (victim == null) return false;
        if (ThreadLocalRandom.current().nextDouble() >= procChance) return false;

        Vector dir = player.getLocation().toVector().subtract(victim.getLocation().toVector()).normalize();
        dir.setY(0.3);
        victim.setVelocity(dir.multiply(magnetStrength));
        return true;
    }
}

