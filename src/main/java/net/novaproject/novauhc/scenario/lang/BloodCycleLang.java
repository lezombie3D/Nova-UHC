package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum BloodCycleLang implements ScenarioLang {

    TAKE_DAMAGE("Ahhh ! Force a toi mauvais minerais"),
    DIAMOND("Vous ne devez pas miner §b§lDiamant. Changement du cycle dans §6§l%blood_timer%§f secondes"),
    GOLD("Vous ne devez pas miner §6§lOr "),
    IRON("Vous ne devez pas miner §7§lFer "),
    COAL("Vous ne devez pas miner §8§lCharbon "),
    LAPIS("Vous ne devez pas miner §9§lLapis "),
    REDSTONE("Vous ne devez pas miner §c§lRedstone ");

    private final String defaultMessage;
    private static FileConfiguration config;

    BloodCycleLang(String defaultMessage) {
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
        BloodCycleLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = ScenarioLang.super.getScenarioPlaceholders(player);
        placeholders.put("%blood_timer%", getConfig().getInt("timer"));
        return placeholders;
    }
}
