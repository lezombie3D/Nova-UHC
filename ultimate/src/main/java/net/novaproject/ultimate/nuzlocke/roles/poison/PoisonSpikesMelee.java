package net.novaproject.ultimate.nuzlocke.roles.poison;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PoisonSpikesMelee extends Ability implements Attacking {

    @Var(name = "Nuzlocke Poison Spikes Mult", type = VariableType.PERCENTAGE)
    private double thornsRatio = 0.20;

    @Var(name = "Nuzlocke Poison Spikes Min", type = VariableType.DOUBLE)
    private double minReflectHearts = 0.5;

    @Override public String getName() { return "Poison Spikes"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (getOwner() == null || !victim.equals(getOwner().getPlayer())) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        double reflect = Math.max(2 * minReflectHearts, event.getDamage() * thornsRatio);
        attacker.damage(reflect, victim);
    }
}

