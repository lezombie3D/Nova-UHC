package net.novaproject.novauhc.scenario.special.beatthesanta;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum BeatTheSantaLang implements ScenarioLang {

    WARNING_LUTIN,
    WARNING_SANTA,
    WARNING_SANTA_DEATH,

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
        BeatTheSantaLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        return ScenarioLang.super.getScenarioPlaceholders(player);
    }
}
