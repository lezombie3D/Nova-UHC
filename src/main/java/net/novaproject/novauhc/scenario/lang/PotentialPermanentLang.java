package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum PotentialPermanentLang implements ScenarioLang {

    HEALTH_STATUS,
    KILL_CONVERSION,
    KILL_ANNOUNCEMENT,
    STARTING_HEALTH,
    CONVERSION_INFO;

    private static FileConfiguration config;

    @Override
    public String getBasePath() {
        return "messages";
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void setConfig(FileConfiguration config) {
        PotentialPermanentLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%starting_permanent%", getConfig().getDouble("starting_permanent_health") / 2);
        placeholders.put("%starting_absorption%", getConfig().getDouble("starting_absorption_health") / 2);
        placeholders.put("%kill_reward%", getConfig().getDouble("kill_reward") / 2);
        return placeholders;
    }
}
