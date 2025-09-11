package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum OreSwapLang implements ScenarioLang {

    SWAP_ANNOUNCEMENT,
    SWAP_WARNING,
    SWAP_FORCED,
    ORE_SWAPPED,
    NEW_MAPPING,
    MAPPING_LINE,
    MAPPING_UNCHANGED;

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
        OreSwapLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%swap_interval%", getConfig().getInt("swap_interval") / 60);
        return placeholders;
    }
}
