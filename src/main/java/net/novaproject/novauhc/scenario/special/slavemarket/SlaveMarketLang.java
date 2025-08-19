package net.novaproject.novauhc.scenario.special.slavemarket;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum SlaveMarketLang implements ScenarioLang {

    // Messages d'enchères
    OWNER_ADDED,
    OWNER_REMOVED,
    AUCTION_START,
    AUCTION_SOLD,
    AUCTION_RANDOM_ASSIGN,
    BID_PLACED,
    BID_NOT_ENOUGH_DIAMONDS,
    BID_ALREADY_HIGHEST,
    AUCTION_TIMER,

    // Messages d'interface
    INVALID_PLAYER,
    MAX_OWNERS_REACHED,
    AUCTION_CANCELLED,

    // Items d'enchères
    BID_ONE_NAME,
    BID_FIVE_NAME,
    DIAMONDS_NAME,

    ;

    private static FileConfiguration config;

    @Override
    public String getBasePath() {
        return "message";
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void setConfig(FileConfiguration config) {
        SlaveMarketLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        return ScenarioLang.super.getScenarioPlaceholders(player);
    }
}
