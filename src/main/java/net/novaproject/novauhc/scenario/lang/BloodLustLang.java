package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum BloodLustLang implements ScenarioLang {

    KILL_BOOST("§c[BloodLust] §fVous ressentez la soif de sang ! Speed II et Strength I pendant 30 secondes !"),
    BOOST_EXPIRED("§c[BloodLust] §fVotre soif de sang s'estompe...");

    private final String defaultMessage;
    private static FileConfiguration config;

    BloodLustLang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

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
        BloodLustLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%speed_duration%", getConfig().getInt("speed_duration") / 20);
        placeholders.put("%strength_duration%", getConfig().getInt("strength_duration") / 20);
        return placeholders;
    }
}
