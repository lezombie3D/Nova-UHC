package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum LuckyOreLang implements ScenarioLang {

    LUCKY_FIND("§6§l[LuckyOre] §f%player% §fa trouvé %reward% §fen minant du %ore% !"),
    LUCKY_PERSONAL("§6§l[LuckyOre] §fVOUS AVEZ EU DE LA CHANCE !"),
    INVENTORY_FULL("§6[LuckyOre] §fInventaire plein ! L'objet a été jeté au sol.");

    private final String defaultMessage;
    private static FileConfiguration config;

    LuckyOreLang(String defaultMessage) {
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
        LuckyOreLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%lucky_chance%", getConfig().getInt("lucky_chance"));
        return placeholders;
    }
}
