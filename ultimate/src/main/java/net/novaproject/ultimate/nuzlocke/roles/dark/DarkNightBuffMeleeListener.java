package net.novaproject.ultimate.nuzlocke.roles.dark;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DarkNightBuffMeleeListener extends Ability implements Attacking {

    @Var(name = "Nuzlocke Dark Night Mult", type = VariableType.DOUBLE)
    private double nightDamageMult = 1.15;

    @Override public String getName() { return "Nuit Vengeresse"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victim, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (getOwner() == null || !attacker.equals(getOwner().getPlayer())) return;
        if (!Dark.isExposedToSun(attacker)) {
            event.setDamage(event.getDamage() * nightDamageMult);
        }
    }
}

