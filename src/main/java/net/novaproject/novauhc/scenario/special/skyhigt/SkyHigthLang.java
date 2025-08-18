package net.novaproject.novauhc.scenario.special.skyhigt;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum SkyHigthLang implements ScenarioLang {

    DAMAGE_FIRST_LAYER,
    DAMAGE_SECOND_LAYER,
    DAMAGE_THIRD_LAYER,
    WARNING_SKY_HIGH,

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
        SkyHigthLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = ScenarioLang.super.getScenarioPlaceholders(player);
        placeholders.put("%first_damage%", getConfig().getInt("firth_damage"));
        placeholders.put("%second_damage%", getConfig().getInt("second_damage"));
        placeholders.put("%third_damage%", getConfig().getInt("third_damage"));
        placeholders.put("%first_level%", getConfig().getInt("first_level"));
        placeholders.put("%second_level%", getConfig().getInt("second_level"));
        placeholders.put("%third_level%", getConfig().getInt("third_level"));
        return placeholders;
    }
}
