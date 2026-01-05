package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum GladiatorLang implements ScenarioLang {

    ARENA_CREATED("§4[Gladiator] §fArène créée ! Combat entre %player1% et %player2% !"),
    TELEPORTING("§4[Gladiator] §fTéléportation dans l'arène dans %seconds% secondes..."),
    COMBAT_STARTED("§4[Gladiator] §fQue le combat commence ! Bonne chance !"),
    COMBAT_TIMEOUT("§4[Gladiator] §fTemps écoulé ! Les deux combattants sont téléportés."),
    WINNER_ANNOUNCED("§4[Gladiator] §f%winner% remporte le duel !"),
    ARENA_CLEANUP("§4[Gladiator] §fArène nettoyée.");

    private final String defaultMessage;
    private static FileConfiguration config;

    GladiatorLang(String defaultMessage) {
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
        GladiatorLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%arena_size%", getConfig().getInt("arena_size"));
        placeholders.put("%combat_timeout%", getConfig().getInt("combat_timeout") / 60);
        return placeholders;
    }
}
