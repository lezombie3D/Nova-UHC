package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum DemocracyLang implements ScenarioLang {

    VOTE_STARTED,
    VOTE_INSTRUCTION,
    VOTE_DURATION,
    VOTE_WARNING,
    VOTE_COUNTDOWN_1MIN,
    VOTE_COUNTDOWN_10SEC,
    VOTE_RESULTS,
    VOTE_RESULT_LINE,
    PLAYER_ELIMINATED,
    NO_ELIMINATION,
    NO_VOTES,
    TIE_RANDOM,
    NOT_ENOUGH_PLAYERS,
    VOTE_CAST,
    AVAILABLE_PLAYERS,
    AVAILABLE_PLAYER_LINE;

    private static FileConfiguration config;

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
        DemocracyLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%vote_interval%", getConfig().getInt("vote_interval") / 60);
        placeholders.put("%vote_duration%", getConfig().getInt("vote_duration") / 60);
        placeholders.put("%min_players%", getConfig().getInt("min_players_for_vote"));
        return placeholders;
    }
}
