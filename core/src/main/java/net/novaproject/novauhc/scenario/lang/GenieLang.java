package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum GenieLang implements ScenarioLang {

    WISHES_RECEIVED("§6[Genie] §fVous avez 3 souhaits ! Utilisez /wish pour voir vos options."),
    WISH_GRANTED("§6[Genie] §fSouhait exaucé ! Il vous reste %remaining% souhait(s)."),
    WISH_ANNOUNCED("§6[Genie] §f%player% a utilisé un souhait !"),
    NO_WISHES_LEFT("§6[Genie] §cVous n'avez plus de souhaits !"),
    NOT_ENOUGH_KILLS("§6[Genie] §cVous n'avez pas assez de kills pour ce souhait !"),
    WISH_OPTIONS_IMPROVED("§6[Genie] §fVos options de souhaits se sont améliorées avec ce kill !"),
    HEAL_GRANTED("§6[Genie] §fVous avez été soigné !"),
    FOOD_GRANTED("§6[Genie] §fVotre faim a été restaurée !"),
    SPEED_GRANTED("§6[Genie] §fVous avez reçu Speed II pendant %duration% minutes !"),
    STRENGTH_GRANTED("§6[Genie] §fVous avez reçu Strength I pendant %duration% minutes !"),
    RESISTANCE_GRANTED("§6[Genie] §fVous avez reçu Resistance I pendant %duration% minutes !"),
    INVISIBILITY_GRANTED("§6[Genie] §fVous êtes invisible pendant %duration% minutes !"),
    TELEPORT_GRANTED("§6[Genie] §fVous avez été téléporté !"),
    FLIGHT_GRANTED("§6[Genie] §fVous pouvez voler pendant %duration% secondes !");

    private final String defaultMessage;
    private static FileConfiguration config;

    GenieLang(String defaultMessage) {
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
