package net.novaproject.ultimate.nuzlocke.roles.fairy;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.BowAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class FairySweetKissBow extends BowAbility {

    @Var(name = "Nuzlocke Fairy Kiss Chance", type = VariableType.PERCENTAGE)
    private double chance = 0.50;

    @Var(name = "Nuzlocke Fairy Kiss Dur", type = VariableType.INTEGER)
    private int durationTicks = 120;

    @Override public String getName() { return "Sweet Kiss"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (getTarget() == null) return false;
        Player victim = getTarget().getPlayer();
        if (victim == null) return false;
        if (ThreadLocalRandom.current().nextDouble() < chance) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, durationTicks, 0, true, false), true);
        }
        return true;
    }
}

