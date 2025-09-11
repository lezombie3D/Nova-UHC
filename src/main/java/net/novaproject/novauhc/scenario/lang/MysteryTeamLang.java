package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum MysteryTeamLang implements ScenarioLang {

    TEAMS_ASSIGNED,
    FIND_TEAMMATES,
    TEAMMATE_FOUND,
    TEAMS_REVEALED,
    TEAM_ANNOUNCEMENT,
    REVEAL_WARNING,
    REVEAL_FORCED;

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
        MysteryTeamLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%reveal_time%", getConfig().getInt("reveal_time") / 60);
        placeholders.put("%team_size%", getConfig().getInt("team_size"));
        return placeholders;
    }
}
