package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum LootCrateLang implements ScenarioLang {

    CRATES_SPAWNED,
    CRATES_ANNOUNCEMENT,
    CRATE_NEARBY,
    CRATES_WARNING_1MIN,
    CRATES_WARNING_10SEC,
    CRATES_FORCED;

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
        LootCrateLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%spawn_interval%", getConfig().getInt("spawn_interval") / 60);
        placeholders.put("%min_crates%", getConfig().getInt("min_crates"));
        placeholders.put("%max_crates%", getConfig().getInt("max_crates"));
        return placeholders;
    }
}
