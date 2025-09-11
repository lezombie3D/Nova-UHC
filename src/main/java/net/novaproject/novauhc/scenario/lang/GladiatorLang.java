package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum GladiatorLang implements ScenarioLang {

    ARENA_CREATED,
    TELEPORTING,
    COMBAT_STARTED,
    COMBAT_TIMEOUT,
    WINNER_ANNOUNCED,
    ARENA_CLEANUP;

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
        GladiatorLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%arena_size%", getConfig().getInt("arena_size"));
        placeholders.put("%combat_timeout%", getConfig().getInt("combat_timeout") / 60);
        return placeholders;
    }
}
