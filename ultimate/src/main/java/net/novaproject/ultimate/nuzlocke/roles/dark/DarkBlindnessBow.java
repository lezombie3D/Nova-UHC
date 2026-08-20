package net.novaproject.ultimate.nuzlocke.roles.dark;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.BowAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DarkBlindnessBow extends BowAbility {

    @Var(name = "Nuzlocke Dark Blind Dur", type = VariableType.INTEGER)
    private int blindTicks = 20;

    @Override public String getName() { return "Aveuglement"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (getTarget() == null) return false;
        Player victim = getTarget().getPlayer();
        if (victim == null) return false;
        victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindTicks, 0, true, false), true);
        return true;
    }
}

