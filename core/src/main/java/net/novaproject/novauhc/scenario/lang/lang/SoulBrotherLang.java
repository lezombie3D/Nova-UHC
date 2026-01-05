package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum SoulBrotherLang implements ScenarioLang {

    SOULS_SEPARATED("§d§l[SoulBrother] §fLes âmes sœurs sont séparées dans des mondes parallèles !"),
    WORLD_ASSIGNMENT("§d[SoulBrother] §fVous êtes dans le monde %world_name% !"),
    REUNION_STARTED("§d§l[SoulBrother] §fLES ÂMES SŒURS SE RETROUVENT !"),
    REUNION_MESSAGE("§d[SoulBrother] §fVous avez été réuni avec votre âme sœur !"),
    SOUL_BROTHER_INFO("§d[SoulBrother] §fVotre âme sœur est §d%brother_name% §f!"),
    REUNION_BONUS("§d[SoulBrother] §fBonus de réunion reçu !"),
    REUNION_WARNING("§d[SoulBrother] §fRéunion des âmes sœurs dans %time% !"),
    REUNION_FORCED("§d[SoulBrother] §fRéunion forcée par un administrateur !"),
    SOUL_BROTHER_UPDATE("§d[SoulBrother] §fVotre âme sœur %brother_name% est en %x%, %z% (Vie: %health%/20)");

    private final String defaultMessage;
    private static FileConfiguration config;

    SoulBrotherLang(String defaultMessage) {
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
        SoulBrotherLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%reunion_time%", getConfig().getInt("reunion_time") / 60);
        placeholders.put("%spawn_radius%", getConfig().getInt("spawn_radius"));
        return placeholders;
    }
}
