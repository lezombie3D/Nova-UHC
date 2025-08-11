package net.novaproject.novauhc.listener.entity;

import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageEntity implements Listener {

    @EventHandler
    public void EntityDamageEntityEVent(EntityDamageByEntityEvent event) {

        Entity entity = event.getEntity();
        Entity damager = event.getDamager();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onHit(entity, damager, event);
        });

    }

    @EventHandler
    public void EntityTakeDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();


        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onDamage(player, event);
        });
    }
}
