package net.novaproject.ultimate.beatthesanta;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum BeatTheSantaLang implements ScenarioLang {

    WARNING_LUTIN("§c§lAttention ! Vous dvez gagnez seul, pas en equipe avec les autre lutins"),
    WARNING_SANTA("§c§lAttention ! Vous devez gagner seul, les lutin essairont de vous tuer en premeier avant de se battre entre eux"),
    WARNING_SANTA_DEATH("Le père Noël est mort, les lutins doivent maintenant s'entretuer pour gagner !"),

    ;
    private static FileConfiguration config;
    private final String defaultMessage;

    BeatTheSantaLang(String defaultMessage) {
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
        BeatTheSantaLang.config = config;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        return ScenarioLang.super.getScenarioPlaceholders(player);
    }
}
