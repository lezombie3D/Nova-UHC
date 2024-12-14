package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerDeathEvent implements Listener {


    @EventHandler
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(event.getEntity());
        UHCPlayer uhcKiller;

        if (event.getEntity().getKiller() != null) {
            uhcKiller = UHCPlayerManager.get().getPlayer(event.getEntity().getKiller());
        } else {
            uhcKiller = null;
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

            scenario.onDeath(uhcPlayer, uhcKiller, event);
        });

    }
}
