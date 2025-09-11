package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum WeakestLinkLang implements ScenarioLang {

    WEAKEST_PLAYER,
    DAMAGE_TAKEN,
    NO_LONGER_WEAKEST;

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
        WeakestLinkLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%multiplier%", getConfig().getDouble("damage_multiplier"));
        placeholders.put("%update_interval%", getConfig().getInt("update_interval") / 60);
        return placeholders;
    }
}
