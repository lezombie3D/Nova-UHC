package net.novaproject.novauhc;

import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
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

    // Messages de commandes
    MSG_USAGE,
    MSG_PLAYER_OFFLINE,
    MSG_CANNOT_MESSAGE_SELF,
    MSG_SENT_FORMAT,
    MSG_RECEIVED_FORMAT,
    DISCORD_MESSAGE,
    DISCORD_MESSAGE_HOVER,
    DOCUMENT_MESSAGE,
    DOCUMENT_MESSAGE_HOVER,

    // Messages d'interface
    WHITELIST_ENABLE_BUTTON,
    WHITELIST_DISABLE_BUTTON,
    RANDOM_TEAMS_BUTTON,

    // Messages de scénarios
    FINAL_HEAL_BROADCAST,
    TIMEBOMB_EXPLOSION,

    HOST_HELP_MESSAGE,
    HOST_SAY_USAGE,
    HOST_COHOST_USAGE,
    HOST_BYPASS_ENABLED,
    HOST_BYPASS_DISABLED,

    PLAYER_HELP_MESSAGE,

    HEAL_BROADCAST,
    CONFIG_CANNOT_INGAME,
    TITLE_USAGE,
    REVIVE_USAGE,
    WHITELIST_USAGE,
    STUFF_USAGE,
    COHOST_ADDED,
    COHOST_REMOVED,
    COHOST_ALREADY_EXISTS,
    COHOST_NOT_FOUND,
    PLAYER_NOT_FOUND,
    FORCE_PVP_BROADCAST,
    FORCE_MEETUP_BROADCAST,

    ERROR_INVALID_NUMBER,
    ERROR_NUMBER_TOO_LOW,
    ERROR_NUMBER_TOO_HIGH,
    ERROR_INVALID_ARGUMENT,
    ERROR_MISSING_ARGUMENT,
    ERROR_COMMAND_DISABLED,
    ERROR_WORLD_NOT_FOUND,
    ERROR_LOCATION_INVALID,
    ERROR_FILE_NOT_FOUND,
    ERROR_PERMISSION_DENIED,
    ERROR_PLAYER_OFFLINE,
    ERROR_PLAYER_NOT_IN_GAME,
    ERROR_ALREADY_IN_PROGRESS,
    ERROR_NOT_STARTED,
    ERROR_COOLDOWN_ACTIVE,

    SUCCESS_OPERATION_COMPLETED,
    SUCCESS_PLAYER_ADDED,
    SUCCESS_PLAYER_REMOVED,
    SUCCESS_SETTINGS_SAVED,
    SUCCESS_WORLD_LOADED,
    SUCCESS_TELEPORT_COMPLETED,

    INFO_LOADING,
    INFO_PROCESSING,
    INFO_PLEASE_WAIT,
    INFO_OPERATION_CANCELLED,
    INFO_NO_CHANGES_MADE,

    ARENA_JOIN,
    ARENA_KILL,
    ARENA_DEATH
    ;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat YEARS_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private static final Map<CommonString, String> TRANSLATIONS = new HashMap<>();
    private static final String DEFAULT_MESSAGE = "§cMissing Translation";

    public static void loadMessages(FileConfiguration config) {
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

        try {
            if (Common.get() != null && Common.get().getArena() != null) {
                placeHolders.put("%border%", String.valueOf((int) Common.get().getArena().getWorldBorder().getSize()));
            } else {
                placeHolders.put("%border%", "N/A");
            }
        } catch (Exception e) {
            placeHolders.put("%border%", "N/A");
        }

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

        try {
            if (UHCPlayerManager.get() != null) {
                placeHolders.put("%players%", String.valueOf(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size()));
            } else {
                placeHolders.put("%players%", "0");
            }
        } catch (Exception e) {
            placeHolders.put("%players%", "0");
        }

        try {
            placeHolders.put("%host%", PlayerConnectionEvent.getHost() != null ? PlayerConnectionEvent.getHost().getName() : "Aucun");
        } catch (Exception e) {
            placeHolders.put("%host%", "Aucun");
        }

        return placeHolders;
    }

    public static Map<String, Object> getPlaceHolders(UHCPlayer uhcPlayer) {
        Map<String, Object> placeHolders = getPlaceHolders();

        if (uhcPlayer != null && uhcPlayer.getPlayer() != null) {
            placeHolders.put("%player%", uhcPlayer.getPlayer().getName());
            placeHolders.put("%kills%", uhcPlayer.getKill());
            placeHolders.put("%team%", uhcPlayer.getTeam().isPresent() ? uhcPlayer.getTeam().get().getName() : "Solo");
            placeHolders.put("%state%", uhcPlayer.isPlaying() ? "En jeu" : "Mort");
            placeHolders.put("%health%", uhcPlayer.getPlayer().getHealth());
            placeHolders.put("%mined_diamond%", uhcPlayer.getMinedDiamond());
            placeHolders.put("%killer%", uhcPlayer.getKiller() != null ? uhcPlayer.getKiller().getName() : "Aucun");

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

            try {
                if (Common.get() != null && Common.get().getArena() != null) {
                    String arrow = uhcPlayer.getArrowDirection(
                            uhcPlayer.getPlayer().getLocation(),
                            Common.get().getArena().getHighestBlockAt(0, 0).getLocation(),
                            uhcPlayer.getPlayer().getLocation().getYaw()
                    );
                    placeHolders.put("%arrow%", arrow);
                    placeHolders.put("%distance%", uhcPlayer.getDistanceToCenter(Common.get().getArena().getHighestBlockAt(0, 0).getLocation()));
                } else {
                    placeHolders.put("%arrow%", "→");

                }
            } catch (Exception e) {
                placeHolders.put("%arrow%", "→");
            }
        } else {
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
            return getRawMessage();
        }
    }

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