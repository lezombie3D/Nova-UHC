package net.novaproject.novauhc;

import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.listener.ListenerManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.scheduler.BukkitRunnable;

public class UHCManager {

    public static UHCManager get() {
        return Main.getUHCManager();
    }

    private UHCPlayerManager uhcPlayerManager;
    private ScenarioManager scenarioManager;


    public void setup() {
        uhcPlayerManager = new UHCPlayerManager();
        scenarioManager = new ScenarioManager();
        ListenerManager.setup();
        CommandManager.setup();
    }

    public void onSec(){

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
                scenario.onSec(player.getPlayer());
            });
        });

    }

    public UHCPlayerManager getUHCPlayerManager() {
        return uhcPlayerManager;
    }

    public ScenarioManager getScenarioManager() {
        return scenarioManager;
    }
}
