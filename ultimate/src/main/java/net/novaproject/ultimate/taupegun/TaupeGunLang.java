package net.novaproject.ultimate.taupegun;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum TaupeGunLang implements Lang {

    TAUPE_GUN_NAME("TaupeGun"),
    TAUPE_GUN_DESC("Certains joueurs deviennent des taupes secrètes avec des kits spéciaux."),

    HELP_MESSAGE("§5Utilisation de la commande :\n" +
                    "/t tc : Envoie vos coordonnées à votre équipe normale.\n" +
                    "/t ti : Ouvrir l'inventaire de l'équipe normale.\n" +
                    "/t kit : Récupérer votre kit.\n" +
                    "/t reveal : Vous révéler en tant que Taupe.\n" +
                    "/tc : Envoyer vos coordonnées à votre équipe taupe.\n" +
                    "/ti : Accéder au TI de votre équipe taupe."),
    NOT_TAUPE_COMMAND_ERROR("§cVous devez être une taupe pour utiliser cette commande."),
    NOT_TAUPE_ERROR("§cVous n'êtes pas une taupe."),
    UNKNOWN_COMMAND("§cCommande inconnue. Utilisez /t pour voir l'aide."),
    KIT_ALREADY_CLAIMED("§cTu as déjà récupéré ton kit !"),
    KIT_RECEIVED("§aVotre kit a bien été reçu !"),

    REVEAL_SUCCESS("§c%player% §7s'est révélé en tant que Taupe !"),
    REVEAL_NOT_TAUPE("§cVous n'êtes pas une taupe, vous ne pouvez pas vous révéler."),

    TAUPE_ASSIGNED_TITLE("§c§lTAUPE"),
    TAUPE_ASSIGNED_SUBTITLE("§7Vous êtes une taupe secrète !"),

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

    UI_TITLE("§5TaupeGun"),
    UI_MOLE_COUNT_NAME("§6Nombre de Taupes par Équipe"),
    UI_MOLE_COUNT_LORE("\n §7► Clic gauche pour §aaugmenter\n §7► Clic droit pour §cdiminuer\n §7Nombre : §b%value%\n"),
    UI_MOLE_TEAM_SIZE_NAME("§6Taille des Équipes Taupe"),
    UI_MOLE_TEAM_SIZE_LORE("\n §7► Clic gauche pour §aaugmenter\n §7► Clic droit pour §cdiminuer\n §7Taille : §b%value%\n");

    private final String fr;

    TaupeGunLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "taupegun." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

