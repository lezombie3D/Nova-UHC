package net.novaproject.novauhc.scenario.lang;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum ParkourMasterLang implements ScenarioLang {

    PARKOUR_SPAWNED("§a[ParkourMaster] §fUn parcours est apparu près de vous ! Complétez-le pour une récompense !"),
    PARKOUR_BROADCAST("§a[ParkourMaster] §fUn parcours est apparu près de %player% !"),
    CHECKPOINT_REACHED("§a[ParkourMaster] §fCheckpoint %current%/%total% atteint !"),
    PARKOUR_COMPLETED("§a§l[ParkourMaster] §fParcours complété ! Récompense : %reward%"),
    PARKOUR_FAILED("§c[ParkourMaster] §fVous avez échoué au parcours ! Réessayez la prochaine fois."),
    PARKOUR_EXPIRED("§c[ParkourMaster] §fLe parcours a expiré !"),
    INVENTORY_FULL("§a[ParkourMaster] §fInventaire plein ! Récompense jetée au sol.");

    private final String defaultMessage;
    private static FileConfiguration config;

    ParkourMasterLang(String defaultMessage) {
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
        ParkourMasterLang.config = config;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%spawn_interval%", getConfig().getInt("spawn_interval") / 60);
        placeholders.put("%timeout%", getConfig().getInt("parkour_timeout") / 60);
        placeholders.put("%min_checkpoints%", getConfig().getInt("min_checkpoints"));
        placeholders.put("%max_checkpoints%", getConfig().getInt("max_checkpoints"));
        return placeholders;
    }
}
