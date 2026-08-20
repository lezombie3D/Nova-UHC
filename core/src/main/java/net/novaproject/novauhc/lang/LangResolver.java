package net.novaproject.novauhc.lang;

import org.bukkit.entity.Player;

public final class LangResolver {


    @SuppressWarnings("unchecked")
    public static String resolve(Class<? extends Lang> langClass, String enumName, Player player) {
        if (langClass == null || enumName == null || enumName.isEmpty()) {
            return "";
        }

        try {

            Lang lang = (Lang) Enum.valueOf((Class<? extends Enum>) langClass, enumName);
            return LangManager.get().get(lang, player);
        } catch (IllegalArgumentException e) {

            return enumName;
        }
    }

    @SuppressWarnings("unchecked")
    public static String resolve(Class<? extends Lang> langClass, String enumName) {
        if (langClass == null || enumName == null || enumName.isEmpty()) {
            return "";
        }

        try {
            Lang lang = (Lang) Enum.valueOf((Class<? extends Enum>) langClass, enumName);
            return LangManager.get().get(lang);
        } catch (IllegalArgumentException e) {
            return enumName;
        }
    }

    public static String resolveVar(Class<? extends Lang> langClass, String key, String inline,
                                    String fallback, Player player) {
        if (langClass != null && langClass != Lang.class && key != null && !key.isEmpty()) {
            return player != null ? resolve(langClass, key, player) : resolve(langClass, key);
        }
        if (inline != null && !inline.isEmpty()) return inline;
        return fallback != null ? fallback : "";
    }
}

