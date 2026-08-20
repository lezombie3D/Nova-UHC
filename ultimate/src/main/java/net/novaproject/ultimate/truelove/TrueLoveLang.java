package net.novaproject.ultimate.truelove;

import net.novaproject.novauhc.lang.Lang;
import java.util.Map;

public enum TrueLoveLang implements Lang {

    TRUE_LOVE_NAME("True Love"),
    TRUE_LOVE_DESC("Les équipes se forment automatiquement entre les premiers joueurs qui se voient."),

    NO_TEAM_AVAILABLE("§cAucune équipe disponible !"),
    PLAYERS_TOO_FAR("§cLes joueurs ne sont plus disponibles ou trop éloignés !"),
    TEAM_JOINED("§aVous avez rejoint l'équipe §f%team% §aavec §f%count% §aautre(s) joueur(s) !"),

    VAR_SIGHT_RADIUS_NAME("Rayon de détection"),
    VAR_SIGHT_RADIUS_DESC("Distance en blocs pour considérer deux joueurs comme se voyant.");

    private final String fr;

    TrueLoveLang(String fr) {
        this.fr = fr;
    }

    @Override
    public String getKey() {
        return "truelove." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return Map.of("fr_FR", fr);
    }
}

