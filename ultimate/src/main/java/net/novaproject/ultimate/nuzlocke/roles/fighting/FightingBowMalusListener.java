package net.novaproject.ultimate.nuzlocke.roles.fighting;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class FightingBowMalusListener extends Ability implements Attacking {

    @Var(name = "Nuzlocke Fighting Bow Mult", type = VariableType.DOUBLE)
    private double bowMultiplier = 0.85;

    @Override public String getName() { return "Bow Malus"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victim, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;
        event.setDamage(event.getDamage() * bowMultiplier);
    }
}

