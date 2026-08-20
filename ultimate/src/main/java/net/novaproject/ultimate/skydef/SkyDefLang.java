package net.novaproject.ultimate.skydef;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum SkyDefLang implements Lang {

    SKYDEF_NAME("SkyDef"),
    SKYDEF_DESC("Défendez ou détruisez la bannière pour remporter la partie."),
    BANNER_ZONE_PROTECTED("§cCette zone autour de la bannière est protégée !"),
    DEFENDERS_NOT_DEAD("§cLa team des défenseurs n'est pas encore morte."),
    BANNER_BROKEN_BROADCAST("§eBien joué à l'équipe %team% composée de : %players%"),
    BANNER_PLACE_FORBIDDEN("§cTu ne peux rien poser dans une sphère de %radius% blocs autour de la bannière !"),

    SKYDEF_UI_TITLE("§b§l SkyDef"),

    SKYDEF_UI_DEF_TEAM_SIZE_LORE("\n  §8┃ §fVous permet de %main_color%modifier\n  §8┃ §fle nombre de %main_color%joueurs§f\n  §8┃ §fdans l'equipe de §bDéfenseur§f\n"),
    VAR_COOLDOWN_TIME_NAME("Temps de Cooldown TP"),
    VAR_COOLDOWN_TIME_DESC("Durée du cooldown en secondes entre deux téléportations."),

    VAR_TEAM_SIZE_NAME("Taille de l'équipe Défenseur"),
    VAR_TEAM_SIZE_DESC("Nombre de joueurs dans l'équipe des défenseurs."),

    VAR_TP_RADIUS_NAME("Rayon de Téléportation"),
    VAR_TP_RADIUS_DESC("Rayon en blocs autour du point de TP pour déclencher la téléportation."),

    VAR_BANNER_PLACE_RADIUS_NAME("Rayon de Protection Bannière"),
    VAR_BANNER_PLACE_RADIUS_DESC("Rayon en blocs autour de la bannière où le placement de blocs est interdit."),

    VAR_ARMOR_ENCHANT_LEVEL_NAME("Niveau d'Enchantement Armure"),
    VAR_ARMOR_ENCHANT_LEVEL_DESC("Niveau de l'enchantement Protection sur l'armure des défenseurs."),

    VAR_GOLDEN_CARROT_AMOUNT_NAME("Quantité de Carottes Dorées"),
    VAR_GOLDEN_CARROT_AMOUNT_DESC("Nombre de carottes dorées données aux défenseurs au démarrage."),

    VAR_BOOK_AMOUNT_NAME("Quantité de Livres"),
    VAR_BOOK_AMOUNT_DESC("Nombre de livres donnés aux défenseurs au démarrage.");

    private final String fr;

    SkyDefLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "skydef." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

