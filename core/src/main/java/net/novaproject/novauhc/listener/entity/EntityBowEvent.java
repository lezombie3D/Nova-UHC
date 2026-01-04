package net.novaproject.novauhc.listener.entity;

import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;


public class EntityBowEvent implements Listener {

    @EventHandler
    public void onBow(EntityShootBowEvent event) {

        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();


        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onBow(entity, player, event);
        });

    }
}
