package net.novaproject.novauhc;

import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public enum CommonString {

    CLICK_HERE_TO_APPLY,
    CLICK_HERE_TO_ACTIVATE,
    CLICK_HERE_TO_DESACTIVATE,
    CLICK_HERE_TO_MODIFY,
    CLICK_HERE_TO_ACCESS,
    BAR,
    NO_PERMISSION,
    CLICK_GAUCHE,
    CLICK_DROITE,
    CLICK_HERE_TO_TOGGLE,

    // language

    KICKED,
    KICKED_MESSAGE,
    WELCOME,
    WELCOME_HOST,
    WELCOME_SPECTATOR,
    ORE_BOOST,
    KICK_SPEC,
    KICK_FULL,
    KICK_WHITELIST,
    EXEDED_LIMITE,
    DIM_NOT_ACCEIBLE,
    CANT_TALK_DEATH,
    CANT_TALK_SPECTATOR,
    CHAT_GLOBAL_FORMAT,
    LOBBY_CHAT_FORMAT,
    SOLO_CHAT_FORMAT,
    TEAM_CHAT_FORMAT,
    HELPOP_MESSAGE,
    BLOCKED_ENCHANT,
    BLOCKED_POTION,
    BLOCKED_CRAFT,
    BLOCKED_CRAFT_ITEM,
    DIAMOND_LIMIT_REACHED,
    DIAMOND_LIMIT_INCREASED,
    TP_MESSAGE,
    DECONNECTION_LOBBY,
    DECONNECTION_GAME,
    CONNECTION_GAME,
    DEATH_MESSAGE,
    DEATH_MESSAGE_TEAM,
    JOIN_TEAM_MESSAGE,
    PREGEN_FINISHED,
    PREGEN_STARTED,
    CHAT_DISABLED,
    REVIVE_MESSAGE,
    INVULNERABLE_OFF,
    MEETUP_START,
    MEETUP_START_IN,
    PVP_START,
    PVP_START_IN,
    DISABLE_ACTION,
    SUCCESSFUL_ACTIVATION,
    SUCCESSFUL_DESACTIVATION,
    SUCCESSFUL_MODIFICATION,
    TEAM_UPDATED,
    TEAM_DESACTIVATED,
    GIVING_ROLES,
    TEAM_UPDATED_MESSAGE,
    TEAM_DESACTIVATED_MESSAGE,
    TEAM_REDFINIED_AUTO,

    // Command messages
    MSG_USAGE,
    MSG_PLAYER_OFFLINE,
    MSG_CANNOT_MESSAGE_SELF,
    MSG_SENT_FORMAT,
    MSG_RECEIVED_FORMAT,
    REP_USAGE,
    REP_NO_ONE_TO_REPLY,
    REP_PLAYER_OFFLINE,
    COMMAND_PLAYERS_ONLY,
    COMMAND_UNKNOWN,

    // Host command messages
    HOST_SAY_USAGE,
    HOST_COHOST_USAGE,
    HOST_WHITELIST_USAGE,
    HOST_STUFF_CLEARED,
    HOST_STUFF_LIST,
    HOST_STUFF_SAVE_CONFIRM,
    HOST_STUFF_CANCEL,
    HOST_BYPASS_ENABLED,
    HOST_BYPASS_DISABLED,
    HOST_COHOST_ADDED,
    HOST_COHOST_REMOVED,
    HOST_COHOST_NOT_FOUND,
    HOST_WHITELIST_ADDED,
    HOST_WHITELIST_REMOVED,
    HOST_WHITELIST_ENABLED,
    HOST_WHITELIST_DISABLED,
    HOST_WHITELIST_CLEARED,
    HOST_WHITELIST_LIST,
    HOST_PLAYER_NOT_FOUND,
    HOST_REVIVE_SUCCESS,
    HOST_HEAL_ALL,
    HOST_LIMITE_SET,
    HOST_PVP_FORCED,
    HOST_MEETUP_FORCED,
    HOST_HELP_MESSAGE,

    // Player command messages
    PLAYER_HELPOP_USAGE,
    PLAYER_HELP_MESSAGE,

    // UI/Menu messages
    CONFIRM_TITLE,
    CONFIRM_BUTTON,
    CONFIRM_BUTTON_LORE,
    CANCEL_BUTTON_LORE,
    MUMBLE_TITLE,
    MUMBLE_CLICK_TO_JOIN,
    MUMBLE_SERVER_INFO,
    MUMBLE_PORT_LABEL,
    MUMBLE_IP_LABEL,
    RANDOM_TEAMS,


    // Chat formats for scenarios
    CHAT_GLOBAL_SCENARIO_FORMAT,
    CHAT_SOLO_SCENARIO_FORMAT,
    ;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat YEARS_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private static final Map<CommonString, String> TRANSLATIONS = new HashMap<>();
    private static final String DEFAULT_MESSAGE = "§cMissing Translation";

    public static void loadMessages(FileConfiguration config) {
        System.out.print(config);
        TRANSLATIONS.clear();
        for (CommonString lang : values()) {
            String path = "message." + lang.name();
            String message = config.getString(path);
            TRANSLATIONS.put(lang, message != null ? message.replace("&", "§") : DEFAULT_MESSAGE);
        }
    }

    public static String getMessage(String text, UHCPlayer uhcPlayer) {
        String message = text;
        for (Map.Entry<String, Object> entry : getPlaceHolders(uhcPlayer).entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return message;
    }

    public static Map<String, Object> getPlaceHolders() {
        Map<String, Object> placeHolders = new HashMap<>();
        placeHolders.put("%time%", SIMPLE_DATE_FORMAT.format(new Date()));
        placeHolders.put("%date%", YEARS_FORMAT.format(new Date()));

        // Safe access to arena border
        try {
            if (Common.get() != null && Common.get().getArena() != null) {
                placeHolders.put("%border%", String.valueOf(Common.get().getArena().getWorldBorder().getSize()));
            } else {
                placeHolders.put("%border%", "N/A");
            }
        } catch (Exception e) {
            placeHolders.put("%border%", "N/A");
        }

        // Safe access to UHC Manager
        try {
            if (UHCManager.get() != null) {
                placeHolders.put("%timer%", UHCManager.get().getTimerFormatted());
                placeHolders.put("%gamestate%", UHCManager.get().getGameState().name());
                placeHolders.put("%slot%", String.valueOf(UHCManager.get().getSlot()));
                placeHolders.put("%diamond_limite%", String.valueOf(UHCManager.get().getDimamondLimit()));
            } else {
                placeHolders.put("%timer%", "00:00");
                placeHolders.put("%gamestate%", "UNKNOWN");
                placeHolders.put("%slot%", "0");
                placeHolders.put("%diamond_limite%", "0");
            }
        } catch (Exception e) {
            placeHolders.put("%timer%", "00:00");
            placeHolders.put("%gamestate%", "UNKNOWN");
            placeHolders.put("%slot%", "0");
            placeHolders.put("%diamond_limite%", "0");
        }

        // Safe access to Common
        try {
            if (Common.get() != null) {
                placeHolders.put("%serveurname%", Common.get().getServername() != null ? Common.get().getServername() : "NovaUHC");
                placeHolders.put("%main_color%", Common.get().getMainColor() != null ? Common.get().getMainColor() : "§e§l");
                placeHolders.put("%infotag%", Common.get().getInfoTag() != null ? Common.get().getInfoTag() : "");
                placeHolders.put("%servertag%", Common.get().getServertag() != null ? Common.get().getServertag() : "");
                placeHolders.put("%servername%", Common.get().getServername() != null ? Common.get().getServername() : "NovaUHC");
            } else {
                placeHolders.put("%serveurname%", "NovaUHC");
                placeHolders.put("%main_color%", "§e§l");
                placeHolders.put("%infotag%", "");
                placeHolders.put("%servertag%", "");
                placeHolders.put("%servername%", "NovaUHC");
            }
        } catch (Exception e) {
            placeHolders.put("%serveurname%", "NovaUHC");
            placeHolders.put("%main_color%", "§e§l");
            placeHolders.put("%infotag%", "");
            placeHolders.put("%servertag%", "");
            placeHolders.put("%servername%", "NovaUHC");
        }

        // Safe access to player count
        try {
            if (UHCPlayerManager.get() != null) {
                placeHolders.put("%players%", String.valueOf(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size()));
            } else {
                placeHolders.put("%players%", "0");
            }
        } catch (Exception e) {
            placeHolders.put("%players%", "0");
        }

        // Safe access to host
        try {
            placeHolders.put("%host%", PlayerConnectionEvent.getHost() != null ? PlayerConnectionEvent.getHost().getName() : "Aucun");
        } catch (Exception e) {
            placeHolders.put("%host%", "Aucun");
        }

        return placeHolders;
    }

    public static Map<String, Object> getPlaceHolders(UHCPlayer uhcPlayer) {
        Map<String, Object> placeHolders = getPlaceHolders(); // Use the safe version

        // Add player-specific placeholders safely
        if (uhcPlayer != null && uhcPlayer.getPlayer() != null) {
            placeHolders.put("%player%", uhcPlayer.getPlayer().getName());
            placeHolders.put("%kills%", uhcPlayer.getKill());
            placeHolders.put("%team%", uhcPlayer.getTeam().isPresent() ? uhcPlayer.getTeam().get().getName() : "Solo");
            placeHolders.put("%state%", uhcPlayer.isPlaying() ? "En jeu" : "Mort");
            placeHolders.put("%health%", uhcPlayer.getPlayer().getHealth());
            placeHolders.put("%mined_diamond%", uhcPlayer.getMinedDiamond());
            placeHolders.put("%killer%", uhcPlayer.getKiller() != null ? uhcPlayer.getKiller().getName() : "Aucun");

            // Safe role access
            try {
                if (ScenarioManager.get() != null) {
                    String role = ScenarioManager.get().getActiveSpecialScenarios().stream()
                            .filter(scenario -> scenario instanceof ScenarioRole)
                            .findFirst()
                            .map(scenario -> ((ScenarioRole<?>) scenario).getRoleByUHCPlayer(uhcPlayer).getName())
                            .orElse("Aucun");
                    placeHolders.put("%role%", role);
                } else {
                    placeHolders.put("%role%", "Aucun");
                }
            } catch (Exception e) {
                placeHolders.put("%role%", "Aucun");
            }

            // Safe arrow direction
            try {
                if (Common.get() != null && Common.get().getArena() != null) {
                    String arrow = uhcPlayer.getArrowDirection(
                            uhcPlayer.getPlayer().getLocation(),
                            new Location(Common.get().getArena(), 0, 100, 0),
                            uhcPlayer.getPlayer().getLocation().getYaw()
                    );
                    placeHolders.put("%arrow%", arrow);
                } else {
                    placeHolders.put("%arrow%", "→");
                }
            } catch (Exception e) {
                placeHolders.put("%arrow%", "→");
            }
        } else {
            // Default values when player is null
            placeHolders.put("%player%", "Unknown");
            placeHolders.put("%kills%", "0");
            placeHolders.put("%team%", "Solo");
            placeHolders.put("%state%", "Unknown");
            placeHolders.put("%health%", "0");
            placeHolders.put("%role%", "Aucun");
            placeHolders.put("%arrow%", "→");
            placeHolders.put("%mined_diamond%", "0");
            placeHolders.put("%killer%", "Aucun");
        }
        placeHolders.put("%host%", PlayerConnectionEvent.getHost() != null ? PlayerConnectionEvent.getHost().getName() : "Aucun");
        placeHolders.put("%infotag%", Common.get().getInfoTag());
        placeHolders.put("%servertag%", Common.get().getServertag());
        placeHolders.put("%servername%", Common.get().getServername());
        placeHolders.put("%mined_diamond%", uhcPlayer.getMinedDiamond());
        placeHolders.put("%diamond_limite%", String.valueOf(UHCManager.get().getDimamondLimit()));
        placeHolders.put("%killer%", uhcPlayer.getKiller() != null ? uhcPlayer.getKiller().getName() : "Aucun");
        return placeHolders;
    }

    public String getMessage(Player player) {
        String message = TRANSLATIONS.getOrDefault(this, DEFAULT_MESSAGE);
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        for (Map.Entry<String, Object> entry : getPlaceHolders(uhcPlayer).entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return message;
    }

    public String getMessage() {
        try {
            String message = TRANSLATIONS.getOrDefault(this, DEFAULT_MESSAGE);
            for (Map.Entry<String, Object> entry : getPlaceHolders().entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
            }
            return message;
        } catch (Exception e) {
            // Fallback to raw message if placeholders fail
            return getRawMessage();
        }
    }

    /**
     * Get the raw message without placeholder replacement.
     * Useful during plugin initialization when some systems aren't ready yet.
     */
    public String getRawMessage() {
        return TRANSLATIONS.getOrDefault(this, DEFAULT_MESSAGE);
    }

    public void send(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        send(player, getPlaceHolders(uhcPlayer));
    }

    public void sendAll() {
        for (UHCPlayer player : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            send(player.getPlayer());
        }
    }

    public void send(Player player, Map<String, Object> placeHolders) {
        String message = getMessage();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        placeHolders.putAll(getPlaceHolders(uhcPlayer));
        for (Map.Entry<String, Object> entry : placeHolders.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        player.sendMessage(message);
    }
}