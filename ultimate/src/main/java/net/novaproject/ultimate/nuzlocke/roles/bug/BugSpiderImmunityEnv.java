package net.novaproject.ultimate.nuzlocke.roles.bug;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BugSpiderImmunityEnv extends Ability implements Attacking {

    @Override public String getName() { return "Cousin des Araignées"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victim, EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (getOwner() == null || !p.equals(getOwner().getPlayer())) return;
        if (event.getDamager() instanceof Spider || event.getDamager() instanceof CaveSpider) {
            event.setCancelled(true);
        }
    }
}

