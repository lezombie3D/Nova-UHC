package net.novaproject.ultimate.nuzlocke.roles.rock;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class RockFallDamageListener extends Ability implements Hurtable {

    @Var(name = "Nuzlocke Rock Fall Mult", type = VariableType.DOUBLE)
    private double fallMultiplier = 1.5;

    @Override public String getName() { return "Vulnérabilité chute"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        event.setDamage(event.getDamage() * fallMultiplier);
    }
}

