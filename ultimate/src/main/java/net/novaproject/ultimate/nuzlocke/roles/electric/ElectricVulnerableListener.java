package net.novaproject.ultimate.nuzlocke.roles.electric;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class ElectricVulnerableListener extends Ability implements Hurtable {

    @Var(name = "Nuzlocke Electric Vuln Mult", type = VariableType.DOUBLE)
    private double damageMultiplier = 1.10;

    @Override public String getName() { return "Vulnérabilité électrique"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (event instanceof EntityDamageByEntityEvent ed && ed.getDamager() instanceof Creeper creeper) {
            if (creeper.getWorld().hasStorm()) {
                event.setCancelled(true);
                return;
            }
        }
        event.setDamage(event.getDamage() * damageMultiplier);
    }
}

