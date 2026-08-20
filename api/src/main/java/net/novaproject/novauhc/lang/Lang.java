package net.novaproject.novauhc.lang;

import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public interface Lang {

    String name();

    String getKey();

    Map<String, String> getTranslations();

    default String getFallback() {
        Map<String, String> t = getTranslations();
        if (t.containsKey("fr_FR")) return t.get("fr_FR");
        if (!t.isEmpty()) return t.values().iterator().next();
        return "[" + getKey() + "]";
    }

    public interface ILangManager {

        void register(Lang[] values);

        void generateAndLoad();

        void requestReload();

        void importShipped(JavaPlugin module);

        String getRaw(Lang key, String locale);

        String get(Lang key, Player player, Map<String, Object> extra);

        String get(Lang key, Player player);

        String get(Lang key, Map<String, Object> extra);

        String get(Lang key);

        void send(Lang key, Player player);

        void send(Lang key, Player player, Map<String, Object> extra);

        void sendAll(Lang key);

        void sendAll(Lang key, Map<String, Object> extra);

        Map<String, Object> buildPlaceholders(Map<String, Object> extra);

        String getServerDefaultLocale();

        Set<String> getAvailableLocales();
    }
}
