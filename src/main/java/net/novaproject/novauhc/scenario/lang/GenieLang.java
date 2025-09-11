package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum GenieLang implements ScenarioLang {

    WISHES_RECEIVED,
    WISH_GRANTED,
    WISH_ANNOUNCED,
    NO_WISHES_LEFT,
    NOT_ENOUGH_KILLS,
    WISH_OPTIONS_IMPROVED,
    HEAL_GRANTED,
    FOOD_GRANTED,
    SPEED_GRANTED,
    STRENGTH_GRANTED,
    RESISTANCE_GRANTED,
    INVISIBILITY_GRANTED,
    TELEPORT_GRANTED,
    FLIGHT_GRANTED;

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
        GenieLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%max_wishes%", getConfig().getInt("max_wishes"));
        placeholders.put("%speed_duration%", getConfig().getInt("wish_effects.speed_duration") / 20 / 60);
        placeholders.put("%strength_duration%", getConfig().getInt("wish_effects.strength_duration") / 20 / 60);
        placeholders.put("%resistance_duration%", getConfig().getInt("wish_effects.resistance_duration") / 20 / 60);
        placeholders.put("%invisibility_duration%", getConfig().getInt("wish_effects.invisibility_duration") / 20 / 60);
        placeholders.put("%flight_duration%", getConfig().getInt("wish_effects.flight_duration") / 20);
        return placeholders;
    }
}
