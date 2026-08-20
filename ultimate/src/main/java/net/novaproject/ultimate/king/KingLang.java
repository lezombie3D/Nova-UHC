package net.novaproject.ultimate.king;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum KingLang implements Lang {

    YOU_ARE_KING("§6§l♚ King §r§aVous êtes le Roi de votre équipe ! Vos coéquipiers doivent vous protéger."),
    TEAM_KING_ANNOUNCE("§6§l♚ King §r§e%king% §fest le Roi de votre équipe. Protégez-le !"),
    KING_DIED("§6§l♚ King §r§c☠ Votre Roi est mort ! Poison infligé à toute l'équipe."),

    ;

    private final Map<String, String> translations;

    KingLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override
    public String getKey() { return "king." + name(); }

    @Override
    public Map<String, String> getTranslations() { return translations; }
}

