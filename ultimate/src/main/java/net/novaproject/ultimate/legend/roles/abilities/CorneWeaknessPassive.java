package net.novaproject.ultimate.legend.roles.abilities;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CorneWeaknessPassive extends PassiveAbility {

    public CorneWeaknessPassive() { setCooldown(0); }

    @Override public String getName() { return "Faiblesse du Barde"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false));
        return true;
    }
}

