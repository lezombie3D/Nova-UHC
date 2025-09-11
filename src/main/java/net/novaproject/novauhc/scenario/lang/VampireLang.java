package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum VampireLang implements ScenarioLang {

    KILL_HEAL,
    SUN_DAMAGE,
    SUN_DAMAGE_SEVERE;

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
        VampireLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%heal_hearts%", getConfig().getDouble("heal_amount") / 2);
        placeholders.put("%sun_damage%", getConfig().getDouble("sun_damage_amount"));
        return placeholders;
    }
}
