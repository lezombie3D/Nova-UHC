package net.novaproject.ultimate.nuzlocke.roles.grass;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Hurtable;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class GrassBurnVulnerableListener extends Ability implements Hurtable {

    @Var(name = "Nuzlocke Grass Burn Mult", type = VariableType.DOUBLE)
    private double burnMultiplier = 2.0;

    @Override public String getName() { return "Vulnérabilité au feu"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onTakeDamage(EntityDamageEvent event) {
        if (getOwner() == null) return;
        if (!(event.getEntity() instanceof Player p)) return;
        if (!p.equals(getOwner().getPlayer())) return;
        EntityDamageEvent.DamageCause c = event.getCause();
        if (c == EntityDamageEvent.DamageCause.FIRE
                || c == EntityDamageEvent.DamageCause.FIRE_TICK
                || c == EntityDamageEvent.DamageCause.LAVA) {
            event.setDamage(event.getDamage() * burnMultiplier);
        }
    }
}

