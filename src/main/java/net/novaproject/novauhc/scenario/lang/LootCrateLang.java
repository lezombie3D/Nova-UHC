package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum LootCrateLang implements ScenarioLang {

    CRATES_SPAWNED("§d§l[LootCrate] §f%count% coffres de loot sont apparus !"),
    CRATES_ANNOUNCEMENT("§d[LootCrate] §fCourrez les récupérer avant les autres !"),
    CRATE_NEARBY("§d[LootCrate] §fUn coffre de loot est apparu près de vous ! (%distance% blocs)"),
    CRATES_WARNING_1MIN("§d[LootCrate] §fNouveaux coffres dans 1 minute !"),
    CRATES_WARNING_10SEC("§d[LootCrate] §fNouveaux coffres dans 10 secondes !"),
    CRATES_FORCED("§d[LootCrate] §fCoffres forcés par un administrateur !");

    private final String defaultMessage;
    private static FileConfiguration config;

    LootCrateLang(String defaultMessage) {
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
        LootCrateLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%spawn_interval%", getConfig().getInt("spawn_interval") / 60);
        placeholders.put("%min_crates%", getConfig().getInt("min_crates"));
        placeholders.put("%max_crates%", getConfig().getInt("max_crates"));
        return placeholders;
    }
}
