package net.novaproject.novauhc.listener.entity;

import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;

public class EntityDeathEvent implements Listener{

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {

        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        //TODO VERIFIER QUE LA PARTIE EST LANCER
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onEntityDeath(entity, killer, event);
        });
    }

}