package net.novaproject.novauhc;

import net.novaproject.novauhc.listener.ListenerManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;

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
    }

    public UHCPlayerManager getUHCPlayerManager() {
        return uhcPlayerManager;
    }

    public ScenarioManager getScenarioManager() {
        return scenarioManager;
    }
}
