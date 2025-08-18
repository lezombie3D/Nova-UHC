package net.novaproject.novauhc.scenario.special.slavemarket;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum SlaveMarketLang implements ScenarioLang {


    ;

    private static FileConfiguration config;

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
        SlaveMarketLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        return ScenarioLang.super.getScenarioPlaceholders(player);
    }
}
