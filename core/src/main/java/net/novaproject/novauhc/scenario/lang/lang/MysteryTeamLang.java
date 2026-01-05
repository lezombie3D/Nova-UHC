package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum MysteryTeamLang implements ScenarioLang {

    TEAMS_ASSIGNED("§5§l[MysteryTeam] §fLes équipes mystères ont été assignées !"),
    FIND_TEAMMATES("§5[MysteryTeam] §fTrouvez vos coéquipiers en comparant vos bannières !"),
    TEAMMATE_FOUND("§5[MysteryTeam] §f%teammate% §fest votre coéquipier ! (Équipe %team_name%)"),
    TEAMS_REVEALED("§5§l[MysteryTeam] §fTOUTES LES ÉQUIPES SONT RÉVÉLÉES !"),
    TEAM_ANNOUNCEMENT("§5[MysteryTeam] §fÉquipe %team_name%: %members%"),
    REVEAL_WARNING("§5[MysteryTeam] §fRévélation des équipes dans %time% !"),
    REVEAL_FORCED("§5[MysteryTeam] §fÉquipes révélées par un administrateur !");

    private final String defaultMessage;
    private static FileConfiguration config;

    MysteryTeamLang(String defaultMessage) {
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
