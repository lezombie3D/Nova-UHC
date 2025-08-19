package net.novaproject.novauhc.scenario.special.taupegun;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum TaupeGunLang implements ScenarioLang {


    ;

    @Override
    public String getBasePath() {
        return "";
    }

    @Override
    public FileConfiguration getConfig() {
        return null;
    }

    @Override
    public void setConfig(FileConfiguration config) {

    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        return ScenarioLang.super.getScenarioPlaceholders(player);
    }
}
