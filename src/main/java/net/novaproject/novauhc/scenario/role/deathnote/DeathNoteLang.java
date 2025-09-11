package net.novaproject.novauhc.scenario.role.deathnote;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;


public enum DeathNoteLang implements ScenarioLang {

    // Messages de base du Death Note
    DEATH_NOTE_RECEIVED,
    DEATH_NOTE_USED,
    DEATH_NOTE_COOLDOWN,
    DEATH_NOTE_READY,

    // Messages de validation
    TARGET_INVALID,
    TARGET_NOT_FOUND,
    TARGET_TEAMMATE,
    TARGET_ALREADY_DEAD,
    TARGET_SELF,

    // Messages de compte à rebours
    DEATH_COUNTDOWN_START,
    DEATH_COUNTDOWN_WARNING_30,
    DEATH_COUNTDOWN_WARNING_10,
    DEATH_COUNTDOWN_WARNING_5,
    DEATH_COUNTDOWN_FINAL,

    // Messages de mort
    DEATH_BY_DEATH_NOTE,
    DEATH_ANNOUNCEMENT,
    DEATH_CANCELLED,

    // Messages d'épisodes
    EPISODE_START,
    EPISODE_REGEN,
    EPISODE_TIME_LEFT,

    // Messages de chat Kira
    KIRA_CHAT_FORMAT,
    KIRA_TEAM_FORMED,
    KIRA_MEMBER_JOINED,
    KIRA_MEMBER_LEFT,
    KIRA_MEMBER_DIED,

    // Messages de victoire
    WIN_GENTILS,
    WIN_KIRA,
    WIN_REASON_ALL_KIRA_DEAD,
    WIN_REASON_ALL_GENTILS_DEAD,
    WIN_REASON_KIRA_MAJORITY,

    // Messages d'erreur
    ERROR_NO_PERMISSION,
    ERROR_SCENARIO_DISABLED,
    ERROR_INVALID_INPUT,
    ERROR_BOOK_NOT_FOUND;

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
        DeathNoteLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();

        // Placeholders liés au cooldown
        placeholders.put("%cooldown%", getConfig().getInt("cooldown") / 60);
        placeholders.put("%cooldown_seconds%", getConfig().getInt("cooldown"));

        // Placeholders liés au timer de mort
        placeholders.put("%death_timer%", getConfig().getInt("death_timer"));

        // Placeholders liés aux avertissements
        placeholders.put("%warning_30%", getConfig().getBoolean("warnings.enabled") ? "30" : "0");
        placeholders.put("%warning_10%", getConfig().getBoolean("warnings.enabled") ? "10" : "0");
        placeholders.put("%warning_5%", getConfig().getBoolean("warnings.enabled") ? "5" : "0");

        // Placeholders liés aux restrictions
        placeholders.put("%no_teammates%", getConfig().getBoolean("restrictions.no_teammates") ? "activé" : "désactivé");
        placeholders.put("%only_alive%", getConfig().getBoolean("restrictions.only_alive_players") ? "activé" : "désactivé");

        return placeholders;
    }
}
