package net.novaproject.novauhc.scenario.lang.lang;

import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum PotentialPermanentLang implements ScenarioLang {

    HEALTH_STATUS("§e[PotentialPermanent] §fVie permanente : %permanent_hearts% cœurs | Absorption : %absorption_hearts% cœurs"),
    KILL_CONVERSION("§e[PotentialPermanent] §fKill ! %converted_hearts% cœur(s) d'absorption convertis en vie permanente !"),
    KILL_ANNOUNCEMENT("§e[PotentialPermanent] §f%player% a maintenant %permanent_hearts% cœurs permanents !"),
    STARTING_HEALTH("§e[PotentialPermanent] §fVous commencez avec %permanent_hearts% cœurs permanents + %absorption_hearts% cœurs d'absorption !"),
    CONVERSION_INFO("§e[PotentialPermanent] §fTuez des joueurs pour convertir l'absorption en vie permanente !");

    private final String defaultMessage;
    private static FileConfiguration config;

    PotentialPermanentLang(String defaultMessage) {
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
        PotentialPermanentLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%starting_permanent%", getConfig().getDouble("starting_permanent_health") / 2);
        placeholders.put("%starting_absorption%", getConfig().getDouble("starting_absorption_health") / 2);
        placeholders.put("%kill_reward%", getConfig().getDouble("kill_reward") / 2);
        return placeholders;
    }
}
