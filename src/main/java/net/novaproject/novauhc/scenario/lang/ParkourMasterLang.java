package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum ParkourMasterLang implements ScenarioLang {

    PARKOUR_SPAWNED,
    PARKOUR_BROADCAST,
    CHECKPOINT_REACHED,
    PARKOUR_COMPLETED,
    PARKOUR_FAILED,
    PARKOUR_EXPIRED,
    INVENTORY_FULL;

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
        ParkourMasterLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%spawn_interval%", getConfig().getInt("spawn_interval") / 60);
        placeholders.put("%timeout%", getConfig().getInt("parkour_timeout") / 60);
        placeholders.put("%min_checkpoints%", getConfig().getInt("min_checkpoints"));
        placeholders.put("%max_checkpoints%", getConfig().getInt("max_checkpoints"));
        return placeholders;
    }
}
