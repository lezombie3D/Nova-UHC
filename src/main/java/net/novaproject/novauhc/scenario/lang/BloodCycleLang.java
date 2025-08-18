package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum BloodCycleLang implements ScenarioLang {

    TAKE_DAMAGE,
    DIAMOND,
    GOLD,
    IRON,
    COAL,
    LAPIS,
    REDSTONE;

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
        BloodCycleLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = ScenarioLang.super.getScenarioPlaceholders(player);
        placeholders.put("%blood_timer%", getConfig().getInt("timer"));
        return placeholders;
    }
}
