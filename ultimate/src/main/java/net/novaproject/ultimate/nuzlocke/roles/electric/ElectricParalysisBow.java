package net.novaproject.ultimate.nuzlocke.roles.electric;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.BowAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class ElectricParalysisBow extends BowAbility {

    @Var(name = "Nuzlocke Electric Para Chance", type = VariableType.PERCENTAGE)
    private double paralysisChance = 0.20;

    @Var(name = "Nuzlocke Electric Para Dur", type = VariableType.INTEGER)
    private int paralysisTicks = 40;

    @Override public String getName() { return "Paralysis"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (getTarget() == null) return false;
        Player victim = getTarget().getPlayer();
        if (victim == null) return false;
        if (ThreadLocalRandom.current().nextDouble() < paralysisChance) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, paralysisTicks, 6, true, false), true);
            victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, paralysisTicks, 200, true, false), true);
            victim.sendMessage("§e§lParalysis §8│ §7Vous êtes paralysé !");
        }
        return true;
    }
}

