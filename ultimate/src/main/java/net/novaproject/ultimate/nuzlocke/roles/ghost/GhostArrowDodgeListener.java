package net.novaproject.ultimate.nuzlocke.roles.ghost;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

public class GhostArrowDodgeListener extends Ability implements Attacking {

    @Var(name = "Nuzlocke Ghost Dodge Chance", type = VariableType.PERCENTAGE)
    private double dodgeChance = 0.20;

    @Override public String getName() { return "Esquive Spectrale"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victim, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (!(event.getDamager() instanceof Arrow)) return;
        if (ThreadLocalRandom.current().nextDouble() < dodgeChance) {
            event.setCancelled(true);
            p.sendMessage("§5§lGhost §8│ §7La flèche vous traverse.");
        }
    }
}

