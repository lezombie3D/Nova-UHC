package net.novaproject.ultimate.fallenkigdom;

import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;

public enum FKLang implements ScenarioLang {

    ANNONCE_NEW_EPISODE("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯\\n§3✦ ÉPISODE %fk_episode% ✦\\n§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"),
    ANNONCE_NETHER("§c Le Nether est maintenant accessible ! "),
    ANNONCE_END("§5✦ L'End est maintenant ouvert ! ✦"),
    ANNONCE_ASSAUT("§c⚔ L'ASSAUT EST AUTORISÉ ! ⚔"),
    WELCOME_FK("§6§l✦ BIENVENUE DANS FALLEN KINGDOM ✦\n\n§f◆ Chaque équipe possède sa propre zone de construction\n§f◆ Construisez et minez uniquement dans votre territoire\n§f◆ Objectif : Capturer le coffre de l'équipe ennemie\n§f◆ Défendez votre base et attaquez stratégiquement\n\n§a✦ Bonne chance, guerriers ! ✦"),
    CAPTURE_FK("§c⚠ ALERTE ! Votre coffre est en cours de capture !"),
    CAPTURE_FK_FAIL("§6⚔ Capture en cours du coffre de l'équipe §c%enemy_team%§6 !\n\n§f◆ Restez dans la zone pendant §e%capture_time% secondes\n§f◆ Si vous mourez, la capture sera annulée\n§f◆ Tenez bon pour la victoire !"),
    CAPTURE_FK_SUCCESS("§a§l✦ VICTOIRE ! ✦\n\n§aVous avez capturé le coffre de l'équipe §c%enemy_team%§a !\n§6Félicitations, vous avez remporté la partie !"),
    WARNING_CAPTURE_FK("§e Capture en cours : §c%remaining_capture_time%s restantes\n\n§fCoffre de l'équipe §c%enemy_team%\n§fRestez dans la zone pour continuer !"),
    REMAINING_CAPTURE_FK("§c⚠ DANGER ! §e%remaining_capture_time%s §cavant la perte de votre coffre !"),
    WARNING_REMAIN_CAPTURE_FK("§c⚠ DANGER ! §e%remaining_capture_time%s §cavant la perte de votre coffre !"),
    TEAM_ELIMINATION("§c☠ Votre équipe a été éliminée par §f%enemy_team%§c !"),
    KILL_TEAM("§a✦ Victoire ! Vous avez éliminé l'équipe §c%enemy_team%§a !"),
    ANNONCE_TEAM_ELIMINATION("§c☠ L'équipe §f%enemy_team% §ca été éliminée par §a%capturing_team%§c !"),
    ;
    private static FileConfiguration config;
    private final String defaultMessage;

    FKLang(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    @Override
    public String getBasePath() {
        // Aligné avec la clé racine utilisée dans fallenkingdom.yml
        return "message";
    }

    @Override
    public FileConfiguration getConfig() {
        return config;
    }

    @Override
    public void setConfig(FileConfiguration config) {
        FKLang.config = config;
    }

    @Override
    public String getDefaultMessage() {
        return defaultMessage;
    }

    @Override
    public Map<String, Object> getScenarioPlaceholders(UHCPlayer player) {
        Map<String, Object> placeholders = ScenarioLang.super.getScenarioPlaceholders(player);
        placeholders.put("%fk_episode%", FallenKingdom.get().getEpisode());
        placeholders.put("%capture_time%", getConfig().getInt("capture_time"));
        return placeholders;
    }
}
