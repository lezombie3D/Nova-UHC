package net.novaproject.novauhc.lang.lang;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum DevLang implements Lang {

    HELP_HEADER("§6● §7§l/DEV"),
    HELP_LOG("§6▸ §e/dev log [channel|all] [on|off] §7— logs debug (abilities, events, schem)"),
    HELP_SCHEM("§6▸ §e/dev schem §7— positions des schematics posés"),
    HELP_BYPASS("§6▸ §e/dev bypass §7— toggle OP sur soi"),
    HELP_SETROLE("§6▸ §e/dev setrole <joueur> <role> §7— force un rôle (clone de la config)"),
    HELP_ROLE("§6▸ §e/dev role <joueur> §7— inspecte rôle/camp/abilities/CDs"),
    HELP_RESETCD("§6▸ §e/dev resetcd <joueur> [ability] §7— reset les cooldowns"),
    HELP_REGIVE("§6▸ §e/dev regive <joueur> §7— rejoue le give complet du rôle (description + items + reveals)"),
    HELP_PENDING("§6▸ §e/dev pending <joueur> <cancel|finalize> §7— gère une mort différée"),
    HELP_TIMER("§6▸ §e/dev timer <sec> §7— saute le timer de partie"),
    HELP_WIN("§6▸ §e/dev win <on|off> §7— active/coupe la win condition"),
    HELP_VICTORY("§6▸ §e/dev victory §7— diagnostic de la victoire (groupes, morts différées, reconnexions)"),
    HELP_STATS("§6▸ §e/dev cavestats §7/ §e/dev orestats §7— stats de génération"),
    HELP_LOC("§6▸ §e/dev loc §7— coordonnées des points d'intérêt enregistrés par le mode actif"),
    HELP_FORCE("§6▸ §e/dev force [nom] §7— déclenche immédiatement une échéance planifiée du mode (trésors, vagues…)"),
    HELP_FORCESTART("§6▸ §e/dev forcestart §7— lance la partie immédiatement, sans attendre le minimum de joueurs"),
    FORCESTART_DONE("§a✔ §7Démarrage forcé §8(§f%players% §7joueur(s) en ligne§8)§7."),
    FORCESTART_ALREADY("§c✖ §7La partie n'est pas dans le lobby §8(§f%state%§8)§7."),

    STATE_ON("§aON"),
    STATE_OFF("§cOFF"),

    BYPASS_TOGGLED("§6§lBypass §8│ §7OP %state%"),

    WIN_STATUS("§6Win condition : %state% §7— /dev win <on|off>"),
    WIN_USAGE("§c✖ §7Usage: §f/dev win <on|off>"),
    WIN_ENABLED("%infotag%La win condition a été réactivée."),
    WIN_DISABLED("%infotag%La win condition a été désactivée (la partie ne se terminera pas toute seule)."),

    TIMER_USAGE("§c✖ §7Usage: §f/dev timer <secondes>"),
    TIMER_SET("§a✔ §7Timer de partie fixé à §f%seconds%s §7(%formatted%)."),
    INVALID_VALUE("§c✖ §7Valeur invalide."),

    TARGET_USAGE("§c✖ §7Usage: §f/dev %action% <joueur> [...]"),
    NO_ROLEMODE("§c✖ §7Aucun mode à rôles actif."),
    SETROLE_USAGE("§c✖ §7Usage: §f/dev setrole <joueur> <role>"),
    ROLE_NOT_FOUND("§c✖ §7Rôle introuvable pour §f%role%§7."),
    NO_ROLE("§c✖ §7Ce joueur n'a pas de rôle."),
    RESETCD_DONE("§a✔ §f%count% §7cooldown(s) reset pour §f%player%§7."),
    RESETCD_ONE("§a✔ §7Cooldown §f%ability% §7reset §7(%seconds%s restants annulés)."),
    RESETCD_NONE("§7Aucun cooldown actif sous §f%ability%§7."),
    NOT_PENDING("§c✖ §f%player% §7n'est pas en mort différée."),
    PENDING_FINALIZED("§a✔ §7Mort de §f%player% §7finalisée immédiatement."),
    PENDING_CANCELLED("§a✔ §7Mort différée de §f%player% §7annulée (ressuscité)."),

    LOG_HEADER("§6● §7§lDEBUG LOGS"),
    LOG_USAGE("§7/dev log <channel|all> [on|off]"),
    LOG_ALL_TOGGLED("§6§lDebug §8│ §7Tous les logs %state%"),
    LOG_CHANNEL_TOGGLED("§6§lDebug:%channel% §8│ §f%state%"),
    LOG_UNKNOWN_CHANNEL("§c✖ §7Channel inconnu. Disponibles : §fall, %channels%"),

    SCHEM_HEADER("§6● §7§lSCHEMATICS POSÉS"),
    SCHEM_NONE("§7Aucun schematic posé sur cette partie."),

    FORCE_NONE("§c✖ §7Aucun déclencheur enregistré par le mode actif."),
    FORCE_HEADER("§6● §7§l/DEV FORCE"),
    FORCE_LINE("§6▸ §e%name% §7— %description%"),
    FORCE_UNKNOWN("§c✖ §7Déclencheur inconnu : §f%name%"),
    FORCE_TRIGGERED("§a✔ §f%name% §7déclenché."),

    CAVESTATS_HEADER("§6● §7§lCAVE BOOST STATS"),
    ORESTATS_HEADER("§6● §7§lORE BOOST STATS"),
    VICTORY_HEADER("§6● §7§lDIAGNOSTIC VICTOIRE"),
    LOC_HEADER("§6● §7§lPOINTS D'INTÉRÊT"),
    LOC_NONE("§7Aucun point d'intérêt enregistré."),

    SETROLE_DONE("§a✔ §7Rôle %role% §7donné à §f%player%§7.%extra%"),
    SETROLE_EXTRA_REVEAL(" §7(reveals rejoués)"),
    ROLE_HEADER("§6● §7§lDEV %PLAYER%"),
    ROLE_PLAYING("§6▸ §ePlaying : §f%playing% §7| §ePending : §f%pending% §7| §eSpec : §f%spec%"),
    ROLE_NONE("§6▸ §eRôle : §cAUCUN"),
    ROLE_LINE("§6▸ §eRôle : §f%role% §7| §eCamp : §f%camp%"),
    ROLE_PARTNER("§6▸ §ePartenaire : §f%partner%"),
    ROLE_ABILITIES("§6▸ §eAbilities §8(§f%count%§8) §7:"),
    ROLE_ABILITY_LINE("  §8│ §f%ability% §7CD=%cd% §7uses=%uses%%flags%"),
    REGIVE_DONE("§a✔ §7Rôle redonné intégralement à §f%player%§7.%extra%"),

    LOG_CHANNEL_LINE("§6▸ §e%channel% §8: %state% §7— %description%"),
    SCHEM_LINE("§6▸ §f%file% §7— %info%"),

    STATS_WORLD("§6▸ §eMonde : §f%world%"),
    STATS_FOOTER(""),
    CAVESTATS_MULT("§6▸ §eMultipliers configurés : §fcaves ×%caves% §7| §fravines ×%ravines%"),
    CAVESTATS_CAVE_CALLS("§6▸ §eAppels gen caves : §f%chunks% chunks §8(§f%passes% passes total§8)"),
    CAVESTATS_RAV_CALLS("§6▸ §eAppels gen ravines : §f%chunks% chunks §8(§f%passes% passes total§8)"),
    CAVESTATS_BROKEN("§e⚠ §7Un des générateurs n'a jamais été appelé — boost cassé."),
    CAVESTATS_ANOMALY("§e⚠ §7Compteur passes < chunks × mult — anomalie."),
    CAVESTATS_OK("§a✔ §7Générateurs boostés actifs."),
    CAVESTATS_AIR("§6▸ §eAir Y%ymin%-%ymax% sur %size%×%size% : §f%pct%% §8(vanilla ≈ 9-12%, ×2 ≈ 13-17%, ×3 ≈ 17-22%)"),

    ORESTATS_SCAN("§6▸ §eMonde : §f%world% §7| §eScan : §f%chunks% chunks (Y 1-64)"),
    ORESTATS_CONFIG_HEADER("§6▸ §eConfig actuelle §8(veins/chunk avant gen)§7 :"),
    ORESTATS_CONFIG_LINE("  %ore% §8: §f%cfg% §7(x%mult%, boost=%boost%)"),
    ORESTATS_MEASURED_HEADER("§6▸ §eDécompte mesuré §8(blocs/chunk)§7 :"),
    ORESTATS_MEASURED_LINE("  %ore% §8: §f%value% §7(%ref%)"),

    VICTORY_ENABLED("§7Win condition active : %state%"),
    VICTORY_STATE_YES("§aoui"),
    VICTORY_STATE_NO("§cnon (/dev win on)"),
    VICTORY_GAMESTATE("§7État de partie : §f%state%"),
    VICTORY_PENDING_HEADER("§c⌛ §7Morts différées en cours (bloquent la victoire) :"),
    VICTORY_PENDING_LINE(" §8│ §f%player% §7(%seconds%s)"),
    VICTORY_PENDING_NONE("§7Morts différées : §aaucune"),
    VICTORY_RECONNECT_WAITING("§c⏳ Reconnexions en attente (bloquent la victoire jusqu'à 15 min)."),
    VICTORY_RECONNECT_NONE("§7Reconnexions en attente : §aaucune"),
    VICTORY_NO_ROLEMODE("§7Aucun scénario à rôles actif (victoire vanilla par teams)."),
    VICTORY_CUSTOM("§7winCondition custom : %state%"),
    VICTORY_CUSTOM_YES("§eoui"),
    VICTORY_CUSTOM_NO("§7non (policies)"),
    VICTORY_NOT_DISTRIBUTED("§7Rôles non distribués — isWin verrait §e0 §7groupe."),
    VICTORY_GROUPS_CUSTOM("§7Groupes (comptage indicatif — la victoire est décidée par la winCondition custom) : §e%count%"),
    VICTORY_GROUPS("§7Groupes de victoire distincts : §e%count% §7(la partie se termine à ≤ 1)"),
    VICTORY_GROUP_LINE(" §8%key% §7→ §f%players%"),
    VICTORY_IGNORED("§8Ignorés par isWin (sans rôle/camp) : §f%players%"),

    LOC_CATEGORY("§6▸ §e%category% §8(§f%count%§8)"),
    LOC_LINE("  §8│ §f%name% §7@ §e%coords%"),
    ;

    private final Map<String, String> translations;

    DevLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override public String getKey() { return "dev." + name(); }

    @Override public Map<String, String> getTranslations() { return translations; }
}
