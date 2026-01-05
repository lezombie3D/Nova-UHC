package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum DemocracyLang implements ScenarioLang {

    VOTE_STARTED("§9§l[Democracy] §fLE VOTE DÉMOCRATIQUE COMMENCE !"),
    VOTE_INSTRUCTION("§9[Democracy] §fUtilisez /vote <joueur> pour voter !"),
    VOTE_DURATION("§9[Democracy] §fVous avez %duration% minutes pour voter !"),
    VOTE_WARNING("§9[Democracy] §fVote démocratique dans %time% !"),
    VOTE_COUNTDOWN_1MIN("§9[Democracy] §fPlus qu'1 minute pour voter !"),
    VOTE_COUNTDOWN_10SEC("§9[Democracy] §fPlus que 10 secondes pour voter !"),
    VOTE_RESULTS("§9§l[Democracy] §fRÉSULTATS DU VOTE :"),
    VOTE_RESULT_LINE("§9[Democracy] §f%player%: %votes% vote(s)"),
    PLAYER_ELIMINATED("§9§l[Democracy] §f%player% §fa été éliminé par vote démocratique !"),
    NO_ELIMINATION("§9[Democracy] §fAucun joueur n'a reçu assez de votes pour être éliminé !"),
    NO_VOTES("§9[Democracy] §fAucun vote ! Personne n'est éliminé."),
    TIE_RANDOM("§9[Democracy] §fÉgalité ! Sélection aléatoire..."),
    NOT_ENOUGH_PLAYERS("§9[Democracy] §fPas assez de joueurs pour un vote !"),
    VOTE_CAST("§9[Democracy] §f%voter% a voté ! (%current%/%total% votes reçus)"),
    AVAILABLE_PLAYERS("§9[Democracy] §fJoueurs disponibles :"),
    AVAILABLE_PLAYER_LINE("§9[Democracy] §f- %player%");

    private final String defaultMessage;
    private static FileConfiguration config;

    DemocracyLang(String defaultMessage) {
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
