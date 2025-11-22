package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum OreSwapLang implements ScenarioLang {

    SWAP_ANNOUNCEMENT("§6[OreSwap] §fLes minerais ont été mélangés !"),
    SWAP_WARNING("§6[OreSwap] §fMélange des minerais dans %time% !"),
    SWAP_FORCED("§6[OreSwap] §fMélange forcé des minerais par un administrateur !"),
    ORE_SWAPPED("§6[OreSwap] §f%original_ore% → %swapped_ore% !"),
    NEW_MAPPING("§6[OreSwap] §fNouveau mapping des minerais :"),
    MAPPING_LINE("§6[OreSwap] §f%original% §7→ §f%swapped%"),
    MAPPING_UNCHANGED("§6[OreSwap] §f%original% §7→ §f%swapped% §7(inchangé)");

    private final String defaultMessage;
    private static FileConfiguration config;

    OreSwapLang(String defaultMessage) {
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
        OreSwapLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%swap_interval%", getConfig().getInt("swap_interval") / 60);
        return placeholders;
    }
}
