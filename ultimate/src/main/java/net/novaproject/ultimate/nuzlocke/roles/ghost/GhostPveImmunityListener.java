package net.novaproject.ultimate.nuzlocke.roles.ghost;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class GhostPveImmunityListener extends Ability implements Hurtable {

    @Override public String getName() { return "Immunité PvE"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        EntityDamageEvent.DamageCause c = event.getCause();
        if (c == EntityDamageEvent.DamageCause.FIRE || c == EntityDamageEvent.DamageCause.FIRE_TICK
                || c == EntityDamageEvent.DamageCause.LAVA) return;
        if (event instanceof EntityDamageByEntityEvent ed) {
            if (ed.getDamager() instanceof Player) return;
        }
        event.setCancelled(true);
    }
}

