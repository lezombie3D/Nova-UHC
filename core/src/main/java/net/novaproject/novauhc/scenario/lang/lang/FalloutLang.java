package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum FalloutLang implements ScenarioLang {

    FALLOUT_STARTED("§c§l[Fallout] §fLES RADIATIONS COMMENCENT !"),
    FALLOUT_INSTRUCTION("§c[Fallout] §fDescendez sous Y=%safe_y% pour éviter les radiations !"),
    FALLOUT_WARNING("§c[Fallout] §fRadiations dans %time% !"),
    FALLOUT_PREPARE("§c[Fallout] §fPréparez vos abris souterrains !"),
    RADIATION_LIGHT("§c[Fallout] §fVous êtes exposé aux radiations !"),
    RADIATION_MODERATE("§c[Fallout] §fRadiation modérée ! Trouvez un abri !"),
    RADIATION_SEVERE("§c[Fallout] §fRadiation SÉVÈRE ! Descendez immédiatement !"),
    FALLOUT_FORCED("§c[Fallout] §fRadiations forcées par un administrateur !");

    private final String defaultMessage;
    private static FileConfiguration config;

    FalloutLang(String defaultMessage) {
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
        FalloutLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%safe_y%", getConfig().getInt("safe_y_level"));
        placeholders.put("%fallout_time%", getConfig().getInt("fallout_start_time") / 60);
        return placeholders;
    }
}
