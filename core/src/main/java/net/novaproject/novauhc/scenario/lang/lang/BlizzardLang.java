package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum BlizzardLang implements ScenarioLang {

    BLIZZARD_START("§b[Blizzard] §fTempête de neige ! Visibilité réduite et ralentissement !"),
    BLIZZARD_END("§b[Blizzard] §fLa tempête de neige se calme."),
    BLIZZARD_WARNING("§b[Blizzard] §fTempête de neige dans %time% secondes !"),
    IN_BLIZZARD("§b[Blizzard] §fVous êtes dans la tempête !");

    private final String defaultMessage;
    private static FileConfiguration config;

    BlizzardLang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public String getBasePath() {
        return "";
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void setConfig(FileConfiguration config) {
        BlizzardLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%slowness_level%", getConfig().getInt("slowness_level") + 1);
        placeholders.put("%blindness_level%", getConfig().getInt("blindness_level") + 1);
        return placeholders;
    }
}
