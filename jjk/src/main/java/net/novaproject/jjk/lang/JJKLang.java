package net.novaproject.jjk.lang;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum JJKLang implements Lang {

    GOJO_DESC("Je suis Gojo","I am Gojo"),
    BLUE_VAR_DESC("",""),
    BLUE_VAR_TITLE("",""),
    ;

    private final Map<String, String> translations;
    JJKLang(String fr, String en) { this.translations = Map.of("fr_FR", fr, "en_US", en); }
    @Override public String getKey() { return "cmd." + name(); }
    @Override public Map<String, String> getTranslations() { return translations; }
}
