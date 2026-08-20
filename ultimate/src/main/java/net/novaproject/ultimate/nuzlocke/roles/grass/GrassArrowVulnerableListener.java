package net.novaproject.ultimate.nuzlocke.roles.grass;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class GrassArrowVulnerableListener extends Ability implements Attacking {

    @Var(name = "Nuzlocke Grass Arrow Mult", type = VariableType.DOUBLE)
    private double arrowMultiplier = 1.10;

    @Override public String getName() { return "Vulnérabilité aux flèches"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victim, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (!(event.getDamager() instanceof Arrow)) return;
        event.setDamage(event.getDamage() * arrowMultiplier);
    }
}

