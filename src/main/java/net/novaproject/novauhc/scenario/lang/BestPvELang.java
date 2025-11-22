package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum BestPvELang implements ScenarioLang {
    LIST_QUIT("Vous avez quittez la list BestPve. Vous la rejoidnrais de nouveaux dans %best_timer%"),
    LIST_JOIN("Vous avez rejoint la list BestPve. Attention a ne plus prendre de degats !"),
    GAIN_MESSAGE("Vous avez gagné §a%heart_gain% §fcoeur(s) !");

    private final String defaultMessage;
    private static FileConfiguration config;

    BestPvELang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
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
        BestPvELang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%heart_gain%", getConfig().getInt("heart_gain"));
        placeholders.put("%best_timer%", getConfig().getInt("timer"));
        return placeholders;
    }
}
