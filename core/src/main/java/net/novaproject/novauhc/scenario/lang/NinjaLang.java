package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum NinjaLang implements ScenarioLang {

    KILL_INVISIBILITY("§8[Ninja] §fVous devenez invisible pendant 10 secondes !"),
    INVISIBILITY_EXPIRED("§8[Ninja] §fVotre invisibilité s'estompe...");

    private final String defaultMessage;
    private static FileConfiguration config;

    NinjaLang(String defaultMessage) {
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
        NinjaLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%invisibility_duration%", getConfig().getInt("invisibility_duration") / 20);
        return placeholders;
    }
}
