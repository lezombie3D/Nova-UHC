package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerInteractEvent implements Listener {

    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event){
         Player player = event.getPlayer();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

            scenario.onPlayerInteract(player, event);
        });
    }
}
