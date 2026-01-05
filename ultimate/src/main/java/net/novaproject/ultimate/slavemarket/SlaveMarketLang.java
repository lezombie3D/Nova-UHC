package net.novaproject.ultimate.slavemarket;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum SlaveMarketLang implements ScenarioLang {

    // Messages d'enchères
    OWNER_ADDED("§a✓ §6%player% §aa été ajouté comme propriétaire d'équipe !"),
    OWNER_REMOVED("§c✓ §6%player% §ca été retiré des propriétaires d'équipe !"),
    AUCTION_START("§e⚡ §6%player% §ea été mis en vente ! Enchère de départ: §b0 diamants"),
    AUCTION_SOLD("§a✦ §6%player% §aa été acheté par §e%buyer% §apour §b%bid% diamants"),
    AUCTION_RANDOM_ASSIGN("§7◆ §6%player% §7a été assigné aléatoirement à §e%owner%"),
    BID_PLACED("§e⚡ §6%bidder% §ea enchéri §b%bid% diamants §epour §a%player%"),
    BID_NOT_ENOUGH_DIAMONDS("§c✗ Vous n'avez pas assez de diamants !"),
    BID_ALREADY_HIGHEST("§c✗ Vous êtes déjà le plus offrant !"),
    AUCTION_TIMER("§e⏰ Enchère se termine dans §c%timer% §esecondes"),

    // Messages d'interface
    INVALID_PLAYER("§c✗ Joueur invalide"),
    MAX_OWNERS_REACHED("§c✗ Impossible, il y a déjà le maximum de propriétaires !"),
    AUCTION_CANCELLED("§c✗ Enchère annulée..."),

    // Items d'enchères
    BID_ONE_NAME("§a✦ Enchérir +1"),
    BID_FIVE_NAME("§a✦ Enchérir +5"),
    DIAMONDS_NAME("§b◆ Diamants: %amount%"),

    ;

    private static FileConfiguration config;
    private final String defaultMessage;

    SlaveMarketLang() {
        this("");
    }

    SlaveMarketLang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

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
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        return ScenarioLang.super.getScenarioPlaceholders(player);
    }
}
