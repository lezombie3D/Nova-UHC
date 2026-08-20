package net.novaproject.ultimate.nuzlocke.roles.fighting;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FightingResistancePiercerListener extends Ability implements Attacking {

    @Override public String getName() { return "Brise-Résistance"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (getOwner() == null) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!attacker.equals(getOwner().getPlayer())) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        PotionEffect res = null;
        for (PotionEffect e : victim.getActivePotionEffects()) {
            if (e.getType().equals(PotionEffectType.DAMAGE_RESISTANCE)) { res = e; break; }
        }
        if (res == null) return;
        int amp = res.getAmplifier() + 1;
        double reduction = 1.0 - Math.min(amp * 0.20, 1.0);
        if (reduction > 0) {
            event.setDamage(event.getDamage() / reduction);
        }
    }
}

