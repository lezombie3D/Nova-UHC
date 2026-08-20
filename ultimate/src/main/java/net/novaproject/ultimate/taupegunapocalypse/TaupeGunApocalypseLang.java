package net.novaproject.ultimate.taupegunapocalypse;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum TaupeGunApocalypseLang implements Lang {

    TAUPE_GUN_APOC_NAME("TaupeGun Apocalypse"),
    TAUPE_GUN_APOC_DESC("Comme TaupeGun, mais chaque équipe taupe contient un Super-Taupe qui gagne en solo."),

    HELP_MESSAGE("§5Utilisation de la commande :\n" +
                    "/tga tc : Envoie vos coordonnées à votre équipe d'origine.\n" +
                    "/tga ti : Ouvrir l'inventaire de l'équipe d'origine.\n" +
                    "/tga taupeti : Ouvrir l'inventaire de votre équipe taupe.\n" +
                    "/tga kit : Récupérer votre kit.\n" +
                    "/tga reveal : Vous révéler en tant que Taupe.\n" +
                    "/tga superReveal : Vous révéler en tant que Super-Taupe (Super-Taupe uniquement, après /tga reveal)."),

    NOT_TAUPE_COMMAND_ERROR("§cVous devez être une taupe pour utiliser cette commande."),
    NOT_TAUPE_ERROR("§cVous n'êtes pas une taupe."),
    UNKNOWN_COMMAND("§cCommande inconnue. Utilisez /tga pour voir l'aide."),
    KIT_ALREADY_CLAIMED("§cTu as déjà récupéré ton kit !"),
    KIT_RECEIVED("§aVotre kit a bien été reçu !"),

    REVEAL_SUCCESS("§c%player% §7s'est révélé en tant que Taupe !"),
    SUPER_REVEAL_SUCCESS("§4§l%player% §7s'est révélé en tant que §4§lSUPER-TAUPE §7! Il joue désormais seul !"),
    REVEAL_NOT_TAUPE("§cVous n'êtes pas une taupe, vous ne pouvez pas vous révéler."),
    NOT_SUPER_TAUPE("§cVous n'êtes pas un Super-Taupe."),
    MUST_REVEAL_FIRST("§cVous devez d'abord vous révéler en tant que Taupe."),
    ALREADY_SUPER_REVEALED("§cVous êtes déjà révélé en tant que Super-Taupe."),
    ALREADY_REVEALED("§cVous êtes déjà révélé en tant que Taupe."),

    TAUPE_ASSIGNED_TITLE("§c§lTAUPE"),
    TAUPE_ASSIGNED_SUBTITLE("§7Vous êtes une taupe secrète !"),
    SUPER_TAUPE_ASSIGNED_TITLE("§4§lSUPER-TAUPE"),
    SUPER_TAUPE_ASSIGNED_SUBTITLE("§7Vous êtes le Super-Taupe de votre équipe — votre but : gagner SEUL !"),

    TAUPE_COORDS_FORMAT("§c● §c§lTAUPE §8│ §f%player% §8: §7%co%"),
    TEAM_COORDS_FORMAT("§b● §b§lÉQUIPE §8│ §f%player% §8: §7%co%"),
    TAUPE_CHAT_FORMAT("§c● §c§lTAUPE §8│ §f%player%§8: §7%message%"),
    TEAM_CHAT_FORMAT("§b● §b§lÉQUIPE §8│ §f%player%§8: §7%message%"),

    KIT_DESCRIPTION_0("§6Kit Archer §7: Punch 1, Power 3, 64 flèches, 3 fils."),
    KIT_DESCRIPTION_1("§6Kit Mobilité §7: 4 perles de l'Ender, Chute Plume 4."),
    KIT_DESCRIPTION_2("§6Kit Potions §7: Speed 1, Résistance au Feu 1, Poison 1."),
    KIT_DESCRIPTION_3("§6Kit Combat §7: Protection 3, Tranchant 3, Puissance 3."),
    KIT_DESCRIPTION_4("§6Kit Ressources §7: 14 obsidienne, 10 diamants, 32 or, 64 fer."),
    KIT_DESCRIPTION_5("§6Kit Feu §7: Aspect du Feu 3, Flèches Enflammées 1."),
    KIT_DESCRIPTION_6("§6Kit Discret §7: Invisibilité 2 (1min), Force 1 (8min)."),

    VAR_MOLE_COUNT_NAME("Nombre de Taupes par Équipe"),
    VAR_MOLE_COUNT_DESC("Nombre de joueurs désignés taupes par équipe normale."),

    VAR_MOLE_TEAM_SIZE_NAME("Taille des Équipes Taupe"),
    VAR_MOLE_TEAM_SIZE_DESC("Nombre de joueurs maximum par équipe taupe."),

    UI_TITLE("§4TaupeGun Apocalypse");

    private final String fr;

    TaupeGunApocalypseLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "taupegunapocalypse." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

