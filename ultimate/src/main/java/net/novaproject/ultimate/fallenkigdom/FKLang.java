package net.novaproject.ultimate.fallenkigdom;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum FKLang implements Lang {

    WELCOME_FK("§6⚔ §7FK §r§fBienvenue ! Protégez votre base et éliminez les équipes adverses !"),
    DISABLE_ACTION("§c✘ §7Cette action est désactivée pour le moment."),

    ANNONCE_NEW_EPISODE("§6⚔ §7FK §r§7Nouvel épisode ! Episode : %episode%"),
    ANNONCE_NETHER("§6⚔ §7FK §r§7Le Nether est désormais accessible !"),
    ANNONCE_END("§6⚔ §7FK §r§5L'End est désormais accessible !"),
    ANNONCE_ASSAUT("§6⚔ §7FK §r§7§lL'ASSAUT EST LANCÉ ! §r§fVous pouvez désormais attaquer les bases ennemies !"),

    CAPTURE_START("§6⚔ §7FK §r§7Capture de la base de §f%enemy_team% §7en cours !"),
    CAPTURE_WARNING("§6⚔ §7FK §r§7⚠ Votre base est attaquée ! Défendez-la !"),
    CAPTURE_PROGRESS("§6⚔ §7FK §r§7Capture en cours... §f%remaining_time%s §7restantes."),
    CAPTURE_PROGRESS_WARNING("§6⚔ §7FK §r§7⚠ Base attaquée ! §f%remaining_time%s §7avant la capture !"),
    CAPTURE_FAIL("§6⚔ §7FK §r§7Capture annulée ! Tous les membres doivent rester près du coffre."),

    TEAM_ELIMINATED("§6⚔ §7FK §r§7☠ Votre équipe a été éliminée !"),
    TEAM_CAPTURED("§6⚔ §7FK §r§7✓ Vous avez éliminé l'équipe §f%enemy_team% §7!"),
    TEAM_ELIMINATION_BROADCAST("§6⚔ §7FK §r§fL'équipe §f%enemy_team% §fa été éliminée par §f%capturing_team% §f!"),

    TNT_DISABLED("§c✘ §7La TNT est désactivée avant l'assaut."),
    BUILD_DENIED("§c✘ §7Vous ne pouvez pas construire dans cette base."),

    ;

    private final Map<String, String> translations;

    FKLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override
    public String getKey() { return "fk." + name(); }

    @Override
    public Map<String, String> getTranslations() { return translations; }
}

