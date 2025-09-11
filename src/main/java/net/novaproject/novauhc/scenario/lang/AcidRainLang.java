package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum AcidRainLang implements ScenarioLang {

    ACID_RAIN_START,
    ACID_RAIN_END,
    ACID_RAIN_WARNING,
    TAKING_DAMAGE,
    SAFE_SHELTER;

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
        AcidRainLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%rain_damage%", getConfig().getDouble("rain_damage"));
        placeholders.put("%rain_interval%", getConfig().getInt("rain_interval") / 20);
        return placeholders;
    }
}
