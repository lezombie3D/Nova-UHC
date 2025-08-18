package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public interface ScenarioLang {
    String name();

    String getBasePath();

    FileConfiguration getConfig();

    void setConfig(FileConfiguration config);

    default String getPath() {
        return getBasePath() + "." + name();
    }

    default Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        return new HashMap<>();
    }
}

