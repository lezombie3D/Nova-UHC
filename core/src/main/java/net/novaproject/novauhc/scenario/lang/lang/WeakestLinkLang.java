package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum WeakestLinkLang implements ScenarioLang {

    WEAKEST_PLAYER("§c[WeakestLink] §f%player% est maintenant le maillon faible ! (Dégâts x%multiplier%)"),
    DAMAGE_TAKEN("§c[WeakestLink] §fVous êtes le maillon faible ! Vous prenez %multiplier%x plus de dégâts !"),
    NO_LONGER_WEAKEST("§c[WeakestLink] §fVous n'êtes plus le maillon faible !");

    private final String defaultMessage;
    private static FileConfiguration config;

    WeakestLinkLang(String defaultMessage) {
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
        WeakestLinkLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%multiplier%", getConfig().getDouble("damage_multiplier"));
        placeholders.put("%update_interval%", getConfig().getInt("update_interval") / 60);
        return placeholders;
    }
}
