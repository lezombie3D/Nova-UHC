package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum VampireLang implements ScenarioLang {

    KILL_HEAL("§c[Vampire] §fVous avez récupéré %heal_hearts% cœur(s) en tuant %victim% !"),
    SUN_DAMAGE("§c[Vampire] §fVous brûlez au soleil ! Équipez un casque ou trouvez de l'ombre !"),
    SUN_DAMAGE_SEVERE("§c[Vampire] §fVous gelez ! Trouvez de la chaleur rapidement !");

    private final String defaultMessage;
    private static FileConfiguration config;

    VampireLang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

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
