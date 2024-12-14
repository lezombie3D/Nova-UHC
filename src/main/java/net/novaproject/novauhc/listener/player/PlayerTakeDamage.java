package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerTakeDamage implements Listener {

    /*@EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event){

        Player uhcplayer = event.getEntity();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

            scenario.onPlayerTakeDamage(uhcplayer, event);
        });
    }*/
}
