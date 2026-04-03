package net.novaproject.novauhc.lang.ui;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;

public enum LunarApolloLang implements Lang {

    ITEM_NAME(
            "§8┃ §fCompatibilité %main_color%Lunar Apollo",
            "§8┃ §f%main_color%Lunar Apollo§f Compatibility"
    ),
    ITEM_DESC(
            "  §8┃ §fAffiche les §bflèches d'équipe§f et les §ecooldowns§f\n  §8┃ §fsur le HUD des joueurs utilisant §aLunar Client§f.",
            "  §8┃ §fDisplays §bteam arrows§f and §ecooldowns§f\n  §8┃ §fon the HUD of §aLunar Client§f players."
    ),
    ITEM_STATUS(
            " §8» §fStatut §f: ",
            " §8» §fStatus §f: "
    ),
    ITEM_ENABLED(
            "§aActivé",
            "§aEnabled"
    ),
    ITEM_DISABLED(
            "§cDésactivé",
            "§cDisabled"
    ),
    ITEM_NOT_AVAILABLE(
            "  §8» §cLunar Apollo §7n'est pas installé sur ce serveur.",
            "  §8» §cLunar Apollo §7is not installed on this server."
    );

    private final Map<String, String> translations;

    LunarApolloLang(String fr, String en) {
        this.translations = Map.of("fr_FR", fr, "en_US", en);
    }

    @Override
    public String getKey() {
        return "ui.lunar_apollo." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }
}
