package net.novaproject.ultimate.taupedefender;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum TaupeDefenderLang implements Lang {

    TAUPE_DEFENDER_NAME("TaupeDefender"),
    TAUPE_DEFENDER_DESC("SkyDef sans défenseur — après %delay% min, une taupe par équipe rejoint les défenseurs."),

    DEFENDER_ASSIGNED_TITLE("§9§lDÉFENSEUR"),
    DEFENDER_ASSIGNED_SUBTITLE("§7Tu es la taupe de ton équipe !"),

    DEFENDER_REVEALED_BROADCAST("§9§lDéfenseur §8│ §f%player% §7s'est révélé en tant que §9Défenseur§7 !"),
    REVEAL_ALREADY_REVEALED("§cTu es déjà révélé en tant que défenseur."),

    CAPTURE_STARTED_BROADCAST("§e%player% §7tente de §ccrocher la bannière §7! Défendez !"),
    CAPTURE_CANCELLED_BROADCAST("§aLe crochetage de la bannière a été §lannulé§a."),
    BANNER_CAPTURED_BROADCAST("§eBien joué à l'équipe %team% composée de : %players%"),

    DEFENDERS_NOT_DEAD("§cLes défenseurs sont encore en vie."),
    BANNER_ZONE_PROTECTED("§cCette zone autour de la bannière est protégée !"),
    BANNER_PLACE_FORBIDDEN("§cTu ne peux rien poser dans une sphère de %radius% blocs autour de la bannière !"),

    TP_MUST_REVEAL("§cTu dois te §l/td reveal§c avant d'utiliser le téléporteur !"),
    GROUP_LIMIT_EXCEEDED("§cTrop de défenseurs regroupés ! Weakness appliqué."),

    NOT_DEFENDER_CMD("§cTu n'es pas un défenseur."),
    KIT_ALREADY_CLAIMED("§cTu as déjà récupéré ton kit !"),
    KIT_RECEIVED("§aVotre kit a bien été reçu !"),
    UNKNOWN_COMMAND("§cCommande inconnue. Utilise /td reveal ou /td claim."),

    VAR_MOLE_DELAY_NAME("Délai avant désignation (min)"),
    VAR_MOLE_DELAY_DESC("Minutes après le PvP avant qu'une taupe soit désignée par équipe."),

    VAR_TEAM_SIZE_NAME("Taille équipe Défenseur"),
    VAR_TEAM_SIZE_DESC("Nombre maximum de joueurs dans l'équipe des défenseurs."),

    VAR_COOLDOWN_TIME_NAME("Temps de Cooldown TP"),
    VAR_COOLDOWN_TIME_DESC("Durée du cooldown en secondes entre deux téléportations."),

    VAR_TP_RADIUS_NAME("Rayon de Téléportation"),
    VAR_TP_RADIUS_DESC("Rayon en blocs autour du point de TP pour déclencher la téléportation."),

    VAR_BANNER_PLACE_RADIUS_NAME("Rayon de Protection Bannière"),
    VAR_BANNER_PLACE_RADIUS_DESC("Rayon en blocs autour de la bannière où le placement de blocs est interdit."),

    VAR_ARMOR_ENCHANT_LEVEL_NAME("Niveau d'Enchantement Armure"),
    VAR_ARMOR_ENCHANT_LEVEL_DESC("Niveau de l'enchantement Protection sur l'armure des défenseurs."),

    VAR_GOLDEN_CARROT_AMOUNT_NAME("Quantité de Carottes Dorées"),
    VAR_GOLDEN_CARROT_AMOUNT_DESC("Nombre de carottes dorées données aux défenseurs lors de la désignation."),

    VAR_BOOK_AMOUNT_NAME("Quantité de Livres"),
    VAR_BOOK_AMOUNT_DESC("Nombre de livres donnés aux défenseurs lors de la désignation."),

    VAR_START_INV_NAME("Inventaire de départ"),
    VAR_START_INV_DESC("Donner l'inventaire de départ aux défenseurs lors de leur désignation."),

    VAR_GROUP_CHECK_INTERVAL_NAME("Intervalle check groupe (min)"),
    VAR_GROUP_CHECK_INTERVAL_DESC("Minutes entre chaque vérification de la limite de regroupement des défenseurs.");

    private final String fr;

    TaupeDefenderLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "taupedefender." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

