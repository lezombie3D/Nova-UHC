package net.novaproject.novauhc.scenario.special.fallenkigdom;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum FKLang implements ScenarioLang {

    ANNONCE_NEW_EPISODE,
    ANNONCE_NETHER,
    ANNONCE_END,
    ANNONCE_ASSAUT,
    WELCOME_FK,
    CAPTURE_FK,
    CAPTURE_FK_FAIL,
    CAPTURE_FK_SUCCESS,
    WARNING_CAPTURE_FK,
    REMAINING_CAPTURE_FK,
    WARNING_REMAIN_CAPTURE_FK,
    TEAM_ELIMINATION,
    KILL_TEAM,
    ANNONCE_TEAM_ELIMINATION,
    ;

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
        FKLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = ScenarioLang.super.getScenarioPlaceholders(player);
        placeholders.put("%fk_episode%", FallenKingdom.get().getEpisode());
        placeholders.put("%capture_time%", getConfig().getInt("capture_time"));
        return placeholders;
    }
}
