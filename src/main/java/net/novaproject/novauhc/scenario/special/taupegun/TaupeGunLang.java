package net.novaproject.novauhc.scenario.special.taupegun;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum TaupeGunLang implements ScenarioLang {

    // Messages de chat
    NOT_TAUPE_ERROR,
    TAUPE_CHAT_FORMAT,
    TEAM_CHAT_FORMAT,
    TAUPE_COORDS_FORMAT,
    TEAM_COORDS_FORMAT,

    // Messages de commandes
    NOT_TAUPE_COMMAND_ERROR,
    UNKNOWN_COMMAND,
    KIT_RECEIVED,
    REVEAL_SUCCESS,
    REVEAL_NOT_TAUPE,

    // Messages d'assignation
    TAUPE_ASSIGNED_TITLE,
    TAUPE_ASSIGNED_SUBTITLE,

    // Messages de kits
    KIT_DESCRIPTION_0,
    KIT_DESCRIPTION_1,
    KIT_DESCRIPTION_2,
    KIT_DESCRIPTION_3,
    KIT_DESCRIPTION_4,
    KIT_DESCRIPTION_5,
    KIT_DESCRIPTION_6,

    ;

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
        TaupeGunLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = ScenarioLang.super.getScenarioPlaceholders(player);

        // Ajoute le placeholder %oldteam%
        TaupeGun taupeGun = ScenarioManager.get().getScenario(TaupeGun.class);
        if (taupeGun != null) {
            UHCTeam oldTeam = taupeGun.getOldTeamforPlayer(player);
            if (oldTeam != null) {
                placeholders.put("%oldteam%", oldTeam.getName());
            } else {
                placeholders.put("%oldteam%", "");
            }
        }

        return placeholders;
    }
}
