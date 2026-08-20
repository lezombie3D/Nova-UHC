package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PrisonnierSpeedPassive extends PassiveAbility {

    @Var(name = "Prisoner Speed Level", desc = "Permanent Speed level.", type = VariableType.INTEGER)
    private int speedLevel = 1;

    public PrisonnierSpeedPassive() { setCooldown(0); }

    @Override public String getName() { return "Vitesse Prisonnier"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (speedLevel <= 0) return false;
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, speedLevel - 1, false, false));
        return true;
    }
}

