package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum AcidRainLang implements ScenarioLang {

    ACID_RAIN_START("§2[AcidRain] §fPluie acide ! Abritez-vous sous des blocs !"),
    ACID_RAIN_END("§2[AcidRain] §fLa pluie acide s'arrête."),
    ACID_RAIN_WARNING("§2[AcidRain] §fPluie acide dans %time% secondes !"),
    TAKING_DAMAGE("§2[AcidRain] §cVous prenez des dégâts de la pluie acide !"),
    SAFE_SHELTER("§2[AcidRain] §fVous êtes à l'abri de la pluie acide.");

    private final String defaultMessage;
    private static FileConfiguration config;

    AcidRainLang(String defaultMessage) {
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
        AcidRainLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%rain_damage%", getConfig().getDouble("rain_damage"));
        placeholders.put("%rain_interval%", getConfig().getInt("rain_interval") / 20);
        return placeholders;
    }
}
