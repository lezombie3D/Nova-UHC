package net.novaproject.ultimate.legend.roles.abilities;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TankResistancePassive extends PassiveAbility {

    @Var(name = "Tank HP Threshold", desc = "HP below which Resistance activates.", type = VariableType.DOUBLE)
    private double healthThreshold = 11.0;

    @Var(name = "Tank Resistance Level", desc = "Resistance level.", type = VariableType.INTEGER)
    private int resistLevel = 1;

    public TankResistancePassive() { setCooldown(0); }

    @Override public String getName() { return "Résistance Tank"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (resistLevel <= 0) return false;
        if (player.getHealth() < healthThreshold) {
            player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, resistLevel - 1, false, false));
            return true;
        }
        return false;
    }
}

