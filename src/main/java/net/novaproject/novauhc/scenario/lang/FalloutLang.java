package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum FalloutLang implements ScenarioLang {

    FALLOUT_STARTED,
    FALLOUT_INSTRUCTION,
    FALLOUT_WARNING,
    FALLOUT_PREPARE,
    RADIATION_LIGHT,
    RADIATION_MODERATE,
    RADIATION_SEVERE,
    FALLOUT_FORCED;

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
        FalloutLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%safe_y%", getConfig().getInt("safe_y_level"));
        placeholders.put("%fallout_time%", getConfig().getInt("fallout_start_time") / 60);
        return placeholders;
    }
}
