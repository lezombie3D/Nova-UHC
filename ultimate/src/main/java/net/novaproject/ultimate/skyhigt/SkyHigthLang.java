package net.novaproject.ultimate.skyhigt;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum SkyHigthLang implements ScenarioLang {

    DAMAGE_FIRST_LAYER("Vous prenez des dégats %first_dagame% allez a la couche %first_level%!"),
    DAMAGE_SECOND_LAYER("Vous prenez des dégats %second_damage% allez a la couche %second_level%!"),
    DAMAGE_THIRD_LAYER("Vous prenez des dégats %third_damage% allez a la couche %third_level%!"),
    WARNING_SKY_HIGH("Attention, vous devez monter dans le ciel !"),

    ;

    private static FileConfiguration config;
    private final String defaultMessage;

    SkyHigthLang() {
        this("");
    }

    SkyHigthLang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
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
        SkyHigthLang.config = config;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
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
