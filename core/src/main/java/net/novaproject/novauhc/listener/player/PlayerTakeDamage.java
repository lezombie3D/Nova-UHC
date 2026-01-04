package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerTakeDamage implements Listener {

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event){

        if (UHCManager.get().isLobby()) {
            event.setCancelled(true);
            return;
        }


        Entity entity = event.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        int timer = UHCManager.get().getTimer();

        if (timer < 30) {
            event.setCancelled(true);
            return;
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

            scenario.onPlayerTakeDamage(entity, event);

        });
    }

}
