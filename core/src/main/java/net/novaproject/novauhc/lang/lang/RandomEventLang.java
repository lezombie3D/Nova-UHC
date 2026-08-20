package net.novaproject.novauhc.lang.lang;


import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum RandomEventLang implements Lang {

    ENABLED_NAME    ("Activé"),
    ENABLED_DESC    ("Active ou désactive cet événement"),
    CHANCE_NAME     ("Chance"),
    CHANCE_DESC     ("Probabilité que l'événement se déclenche"),
    MIN_TIME_NAME   ("Temps minimum"),
    MIN_TIME_DESC   ("Temps min (en secondes) depuis le début du jeu"),
    MAX_TIME_NAME   ("Temps maximum"),
    MAX_TIME_DESC   ("Temps max (en secondes) depuis le début du jeu"),
    REPEATING_NAME  ("Répétable"),
    REPEATING_DESC  ("Si l'événement se répète après déclenchement"),
    ;

    private final Map<String, String> translations;

    RandomEventLang(String fr) {
        this.translations = Map.of("fr_FR", fr);
    }

    @Override public String getKey() { return "random_event." + name(); }
    @Override public Map<String, String> getTranslations() { return translations; }
}

