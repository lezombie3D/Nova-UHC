package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ScenarioLangManager {
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();
    private static final String DEFAULT_MESSAGE = "§cMissing Translation";

    public static void loadMessages(FileConfiguration config, ScenarioLang[] values) {
        for (ScenarioLang key : values) {
            String path = key.getPath();
            String message = config.getString(path);
            TRANSLATIONS.put(path, message != null ? message.replace("&", "§") : DEFAULT_MESSAGE);
        }
    }

    public static String get(ScenarioLang key, UHCPlayer uhcPlayer, Map<String, Object> extraPlaceholders) {
        String message = TRANSLATIONS.getOrDefault(key.getPath(), DEFAULT_MESSAGE);

        Map<String, Object> placeholders = new HashMap<>(CommonString.getPlaceHolders(uhcPlayer));
        placeholders.putAll(key.getScenarioPlaceholders(uhcPlayer));

        if (extraPlaceholders != null) {
            placeholders.putAll(extraPlaceholders);
        }

        for (Map.Entry<String, Object> entry : placeholders.entrySet()) {
            message = message.replace(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return message;
    }


    public static String get(ScenarioLang key, UHCPlayer uhcPlayer) {
        return get(key, uhcPlayer, null);
    }

    public static String getMessage(ScenarioLang key) {
        return getMessage(key, null);
    }

    public static String getMessage(ScenarioLang key, Map<String, Object> extraPlaceholders) {
        String message = TRANSLATIONS.getOrDefault(key.getPath(), DEFAULT_MESSAGE);

        if (extraPlaceholders != null) {
            for (Map.Entry<String, Object> entry : extraPlaceholders.entrySet()) {
                message = message.replace(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }

        return message;
    }

    public static void send(UHCPlayer uhcPlayer, ScenarioLang key) {
        send(uhcPlayer.getPlayer(), key, null);
    }

    public static void send(UHCPlayer uhcPlayer, ScenarioLang key, Map<String, Object> extraPlaceholders) {
        send(uhcPlayer.getPlayer(), key, extraPlaceholders);
    }

    public static void send(Player player, ScenarioLang key) {
        send(player, key, null);
    }

    public static void send(Player player, ScenarioLang key, Map<String, Object> extraPlaceholders) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        player.sendMessage(get(key, uhcPlayer, extraPlaceholders));
    }

    public static void sendAll(ScenarioLang key) {
        sendAll(key, null);
    }

    public static void sendAll(ScenarioLang key, Map<String, Object> extraPlaceholders) {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            send(uhcPlayer.getPlayer(), key, extraPlaceholders);
        }
    }
}
