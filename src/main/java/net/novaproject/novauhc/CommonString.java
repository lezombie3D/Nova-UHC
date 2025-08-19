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

    public static Map<String, Object> getPlaceHolders() {
        Map<String, Object> placeHolders = new HashMap<>();
        placeHolders.put("%time%", SIMPLE_DATE_FORMAT.format(new Date()));
        placeHolders.put("%date%", YEARS_FORMAT.format(new Date()));
        placeHolders.put("%border%", String.valueOf(Common.get().getArena().getWorldBorder().getSize()));
        placeHolders.put("%timer%", UHCManager.get().getTimerFormatted());
        placeHolders.put("%serveurname%", Common.get().getServername());
        placeHolders.put("%gamestate%", UHCManager.get().getGameState().name());
        placeHolders.put("%players%", String.valueOf(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size()));
        placeHolders.put("%slot%", String.valueOf(UHCManager.get().getSlot()));
        placeHolders.put("%main_color%", Common.get().getMainColor());
        placeHolders.put("%host%", PlayerConnectionEvent.getHost() != null ? PlayerConnectionEvent.getHost().getName() : "Aucun");
        placeHolders.put("%infotag%", Common.get().getInfoTag());
        placeHolders.put("%servertag%", Common.get().getServertag());
        placeHolders.put("%servername%", Common.get().getServername());
        placeHolders.put("%diamond_limite%", String.valueOf(UHCManager.get().getDimamondLimit()));
        return placeHolders;
    }

    public static String getMessage(String text, UHCPlayer uhcPlayer) {
        String message = text;
        for (Map.Entry<String, Object> entry : getPlaceHolders(uhcPlayer).entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return message;
    }

    public static Map<String, Object> getPlaceHolders(UHCPlayer uhcPlayer) {
        Map<String, Object> placeHolders = new HashMap<>();
        placeHolders.put("%time%", SIMPLE_DATE_FORMAT.format(new Date()));
        placeHolders.put("%date%", YEARS_FORMAT.format(new Date()));
        placeHolders.put("%border%", String.valueOf(Common.get().getArena().getWorldBorder().getSize()));
        placeHolders.put("%timer%", UHCManager.get().getTimerFormatted());
        placeHolders.put("%gamestate%", UHCManager.get().getGameState().name());
        placeHolders.put("%players%", String.valueOf(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size()));
        placeHolders.put("%slot%", String.valueOf(UHCManager.get().getSlot()));
        placeHolders.put("%main_color%", Common.get().getMainColor());
        placeHolders.put("%player%", uhcPlayer.getPlayer().getName());
        placeHolders.put("%kills%", uhcPlayer.getKill());
        placeHolders.put("%team%", uhcPlayer.getTeam().isPresent() ? uhcPlayer.getTeam().get().getName() : "Solo");
        placeHolders.put("%state%", uhcPlayer.isPlaying() ? "En jeu" : "Mort");
        placeHolders.put("%health%", uhcPlayer.getPlayer().getHealth());
        placeHolders.put("%role%", ScenarioManager.get().getActiveSpecialScenarios().stream()
                .filter(scenario -> scenario instanceof ScenarioRole)
                .findFirst()
                .map(scenario -> ((ScenarioRole<?>) scenario).getRoleByUHCPlayer(uhcPlayer).getName())
                .orElse("Aucun"));
        placeHolders.put("%arrow%", uhcPlayer.getArrowDirection(uhcPlayer.getPlayer().getLocation(), new Location(Common.get().getArena(), 0, 100, 0), uhcPlayer.getPlayer().getLocation().getYaw()));
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
        String message = TRANSLATIONS.getOrDefault(this, DEFAULT_MESSAGE);
        for (Map.Entry<String, Object> entry : getPlaceHolders().entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return message;
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