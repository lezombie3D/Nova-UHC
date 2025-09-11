package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum SoulBrotherLang implements ScenarioLang {

    SOULS_SEPARATED,
    WORLD_ASSIGNMENT,
    REUNION_STARTED,
    REUNION_MESSAGE,
    SOUL_BROTHER_INFO,
    REUNION_BONUS,
    REUNION_WARNING,
    REUNION_FORCED,
    SOUL_BROTHER_UPDATE;

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
        SoulBrotherLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%reunion_time%", getConfig().getInt("reunion_time") / 60);
        placeholders.put("%spawn_radius%", getConfig().getInt("spawn_radius"));
        return placeholders;
    }
}
