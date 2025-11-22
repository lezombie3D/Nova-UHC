package net.novaproject.novauhc;

import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static net.novaproject.novauhc.utils.ConfigUtils.saveConfig;
import static net.novaproject.novauhc.utils.ConfigUtils.saveLangConfig;

public enum CommonString {

    CLICK_HERE_TO_APPLY(" §8» §fCliquez pour §a§lappliquer§f."),
    CLICK_HERE_TO_ACTIVATE(" §8» §fCliquez pour §a§lactiver§f."),
    CLICK_HERE_TO_DESACTIVATE(" §8» §fCliquez pour §c§ldésactiver§f."),
    CLICK_HERE_TO_MODIFY(" §8» §fCliquez pour §6§lmodifier§f."),
    CLICK_HERE_TO_ACCESS(" §8» §fCliquez pour y %main_color%accéder§f."),
    BAR("§f§m                                                                           §r"),
    NO_PERMISSION("§cDésolé, mais vous n'avez pas la permission d'exécuter cette commande. Veuillez contacter les administrateurs du serveur si vous pensez qu'il s'agit d'une erreur."),
    CLICK_GAUCHE(" §8» §fClic gauche pour §f"),
    CLICK_DROITE(" §8» §fClic droit pour §f"),
    CLICK_HERE_TO_TOGGLE(" §8» §fCliquez ici pour §6§lactiver/désactiver§f."),

    KICKED("§cVous avez été exclu de la partie par l'host"),
    KICKED_MESSAGE("§c» Le joueur §f%player% §ca été exclu de la partie par l'host !"),
    WELCOME("§a§lBienvenue §fsur la partie de §6%host%§f !"),
    WELCOME_HOST("\n§8§l«§8§m--------------------------------------------------§8§l»\n§f \n§6│ §fBienvenue §6§l%player% sur votre partie §f!\n§f \n§6│ §fVous pouvez dès à présent configurer : §l§c /h config§f!\n§f \n§8§l«§8§m--------------------------------------------------§8§l»\n"),
    WELCOME_SPECTATOR("§7Bienvenue en tant que §espectateur §fsur la partie de §6%host%§f !"),
    ORE_BOOST("§aModification enregistrée §f! N'oubliez pas de recréer l'arène pour appliquer les changements."),
    KICK_SPEC("§cConnexion refusée : L'host a désactivé le mode spectateur"),
    KICK_FULL("§cConnexion refusée : La partie est complète"),
    KICK_WHITELIST("§cConnexion refusée : Vous n'êtes pas dans la whitelist de l'host"),
    EXEDED_LIMITE("§cLimite dépassée"),
    DIM_NOT_ACCEIBLE("§cCette dimension n'est pas accessible"),
    CANT_TALK_DEATH("§cVous ne pouvez pas parler dans le chat car vous êtes mort"),
    CANT_TALK_SPECTATOR("§cVous ne pouvez pas parler dans le chat en tant que spectateur"),
    CHAT_GLOBAL_FORMAT("§7[§6Global§7] §f%s §8» §f%s"),
    LOBBY_CHAT_FORMAT("§7[§6Lobby§7] §f%s §8» §f%s"),
    SOLO_CHAT_FORMAT("§7[§6Solo§7] §f%s §8» §f%s"),
    TEAM_CHAT_FORMAT("§7[§6%team%§7] §f%s §8» §f%s"),
    HELPOP_MESSAGE("%helpop% §2» "),
    BLOCKED_ENCHANT("§cVous ne pouvez pas appliquer cet enchantement"),
    BLOCKED_POTION("§cVous ne pouvez pas créer cette potion"),
    BLOCKED_CRAFT("§cVous ne pouvez pas effectuer ce craft"),
    BLOCKED_CRAFT_ITEM("§cVous ne pouvez pas créer cet objet"),
    DIAMOND_LIMIT_REACHED("§cVous avez atteint la limite de diamants minés"),
    DIAMOND_LIMIT_INCREASED("§bDiamants minés : §f%mined_diamond%§7/§f%diamond_limite%"),
    TP_MESSAGE("§a» Le joueur §f%player% §aa été téléporté !"),
    DECONNECTION_LOBBY("§c» §f%player% §cse déconnecte du lobby"),
    DECONNECTION_GAME("§c» §f%player% §cse déconnecte de la partie"),
    CONNECTION_GAME("§a» §f%player% §ase connecte à la partie"),
    DEATH_MESSAGE("§c☠ §f%player% §ca été éliminé par §f%killer%§c !"),
    DEATH_MESSAGE_TEAM("§c☠ §7[§6%team%§7] §f%player% §ca été éliminé"),
    JOIN_TEAM_MESSAGE("§aVous avez rejoint l'équipe §6%team%§a !"),
    PREGEN_FINISHED("§a✓ Pré-génération terminée avec succès !"),
    PREGEN_STARTED("§e⚡ Pré-génération en cours..."),
    CHAT_DISABLED("§cLe chat est actuellement désactivé !"),
    REVIVE_MESSAGE("§a✦ §f%player% §aa été ressuscité par §6%host%§a !"),
    INVULNERABLE_OFF("§c⚠ Vous n'êtes plus invulnérable !"),
    MEETUP_START("§c§l♦ MEETUP ACTIVÉ ! §r§cAttention, la bordure se réduit !"),
    MEETUP_START_IN("§e⚡ Le Meetup sera activé dans §a%time_before%"),
    PVP_START("§c⚔ Le PVP est maintenant activé !"),
    PVP_START_IN("§e⚡ Le PVP sera activé dans §a%time_before%"),
    DISABLE_ACTION("§cVous ne pouvez pas effectuer cette action !"),
    SUCCESSFUL_ACTIVATION("§a✓ Activation effectuée avec succès !"),
    SUCCESSFUL_DESACTIVATION("§c✓ Désactivation effectuée avec succès !"),
    SUCCESSFUL_MODIFICATION("§6✓ Modification effectuée avec succès !"),
    TEAM_UPDATED("§6⚡ Les équipes ont été mises à jour !"),
    TEAM_DESACTIVATED("§c⚡ Les équipes ont été désactivées !"),
    GIVING_ROLES("§6♦ Distribution des rôles en cours..."),
    TEAM_UPDATED_MESSAGE("§6⚡ Les équipes ont été mises à jour !"),
    TEAM_DESACTIVATED_MESSAGE("§c⚡ Les équipes ont été désactivées !"),
    TEAM_REDFINIED_AUTO("§6◉ Les équipes ont été redéfinies automatiquement !"),

    // Messages de commandes
    MSG_USAGE("§c✗ Usage: §f/msg <joueur> <message>"),
    MSG_PLAYER_OFFLINE("§c✗ Ce joueur est hors ligne ou introuvable"),
    MSG_CANNOT_MESSAGE_SELF("§c✗ Vous ne pouvez pas vous envoyer un message à vous-même !"),
    MSG_SENT_FORMAT("§8│ §7§lMoi §7→ §7§l%target% §f%message%"),
    MSG_RECEIVED_FORMAT("§8│ §7§l%sender% §7→ §7§lMoi §f%message%"),
    DISCORD_MESSAGE("§9│ §f§lDiscord §8» "),
    DISCORD_MESSAGE_HOVER("%main_color% » Clique ici pour ouvrir le Discord !"),
    DOCUMENT_MESSAGE("§9│ §f§lDocument §8» "),
    DOCUMENT_MESSAGE_HOVER("%main_color% » Clique ici pour ouvrir le Document !"),
    HOST_GIVE("§a✓ §f%host% §a à donner %item% %amont%"),

    // Messages d'interface
    WHITELIST_ENABLE_BUTTON("§a✓ Activer la Whitelist"),
    WHITELIST_DISABLE_BUTTON("§c✗ Désactiver la Whitelist"),
    RANDOM_TEAMS_BUTTON("§f◆ Équipes aléatoires"),

    // Messages de scénarios
    FINAL_HEAL_BROADCAST("§a♥ Final Heal effectué ! Tous les joueurs ont été soignés."),
    TIMEBOMB_EXPLOSION("§c☠ Le corps de §f%player% §ca explosé !"),

    HOST_HELP_MESSAGE("§6◆ Commandes Host disponibles:\n§f/h config §7- Ouvre le menu de configuration\n§f/h bypass §7- Active/désactive le mode créatif\n§f/h cohost <add|remove> <joueur> §7- Gère les co-hosts\n§f/h whitelist <add|remove|list|clear|on|off> §7- Gère la whitelist\n§f/h say <message> §7- Diffuse un message\n§f/h title <message> §7- Affiche un titre\n§f/h heal §7- Soigne tous les joueurs\n§f/h forcepvp §7- Force le PvP\n§f/h forcemtp §7- Force le Meetup\n§f/h stuff <modif|save|clear> §7- Gère l'équipement"),
    HOST_SAY_USAGE("§c✗ Usage: §f/h say <message>"),
    HOST_COHOST_USAGE("§c✗ Usage: §f/h cohost <add|remove> <joueur>"),
    HOST_BYPASS_ENABLED("§a✓ Mode bypass activé (Créatif)"),
    HOST_BYPASS_DISABLED("§c✗ Mode bypass désactivé (Survie)"),

    PLAYER_HELP_MESSAGE("§6◆ Commandes Joueur disponibles:\n§f/p helpop <message> §7- Envoie un message aux hosts"),

    HEAL_BROADCAST("§a♥ Heal effectué ! Tous les joueurs ont été soignés."),
    CONFIG_CANNOT_INGAME("§c✗ Vous ne pouvez pas configurer en cours de partie"),
    TITLE_USAGE("§c✗ Usage: §f/h title <message>"),
    REVIVE_USAGE("§c✗ Usage: §f/h revive <joueur>"),
    WHITELIST_USAGE("§c✗ Usage: §f/h whitelist <add|remove|list|clear|on|off> [joueur]"),
    STUFF_USAGE("§c✗ Usage: §f/h stuff <modif|save|clear>"),
    COHOST_ADDED("§a✓ §f%player% §aa été ajouté comme co-host"),
    COHOST_REMOVED("§c✓ §f%player% §ca été retiré des co-hosts"),
    COHOST_ALREADY_EXISTS("§c✗ Ce joueur est déjà co-host"),
    COHOST_NOT_FOUND("§c✗ Ce joueur n'est pas co-host"),
    PLAYER_NOT_FOUND("§c✗ Joueur introuvable ou hors ligne"),
    FORCE_PVP_BROADCAST("§c⚔ Le PVP a été forcé par l'host !"),
    FORCE_MEETUP_BROADCAST("§c♦ Le Meetup a été forcé par l'host !"),

    ERROR_INVALID_NUMBER("§c✗ Nombre invalide. Veuillez entrer un nombre valide."),
    ERROR_NUMBER_TOO_LOW("§c✗ Le nombre doit être supérieur à %min%"),
    ERROR_NUMBER_TOO_HIGH("§c✗ Le nombre doit être inférieur à %max%"),
    ERROR_INVALID_ARGUMENT("§c✗ Argument invalide: §f%argument%"),
    ERROR_MISSING_ARGUMENT("§c✗ Argument manquant. Consultez l'aide de la commande."),
    ERROR_COMMAND_DISABLED("§c✗ Cette commande est actuellement désactivée"),
    ERROR_WORLD_NOT_FOUND("§c✗ Monde introuvable: §f%world%"),
    ERROR_LOCATION_INVALID("§c✗ Position invalide ou inaccessible"),
    ERROR_FILE_NOT_FOUND("§c✗ Fichier introuvable: §f%file%"),
    ERROR_PERMISSION_DENIED("§c✗ Vous n'avez pas la permission d'effectuer cette action"),
    ERROR_PLAYER_OFFLINE("§c✗ Le joueur §f%player% §cn'est pas en ligne"),
    ERROR_PLAYER_NOT_IN_GAME("§c✗ Le joueur §f%player% §cn'est pas en jeu"),
    ERROR_ALREADY_IN_PROGRESS("§c✗ Une opération est déjà en cours"),
    ERROR_NOT_STARTED("§c✗ Aucune partie n'est en cours"),
    ERROR_COOLDOWN_ACTIVE("§c✗ Veuillez attendre §f%time% §cavant de réutiliser cette commande"),

    SUCCESS_OPERATION_COMPLETED("§a✓ Opération terminée avec succès"),
    SUCCESS_PLAYER_ADDED("§a✓ Joueur §f%player% §aajouté avec succès"),
    SUCCESS_PLAYER_REMOVED("§a✓ Joueur §f%player% §cretiré avec succès"),
    SUCCESS_SETTINGS_SAVED("§a✓ Paramètres sauvegardés avec succès"),
    SUCCESS_WORLD_LOADED("§a✓ Monde §f%world% §achargé avec succès"),
    SUCCESS_TELEPORT_COMPLETED("§a✓ Téléportation effectuée"),

    INFO_LOADING("§e⚡ Chargement en cours..."),
    INFO_PROCESSING("§e⚡ Traitement en cours..."),
    INFO_PLEASE_WAIT("§e⌛ Veuillez patienter..."),
    INFO_OPERATION_CANCELLED("§7◆ Opération annulée"),
    INFO_NO_CHANGES_MADE("§7◆ Aucune modification effectuée"),

    ARENA_JOIN("%main_color%Arena §7» §c%player% §fa rejoint l'arène !"),
    ARENA_KILL("%main_color%Arena §7» §c%player_arena% §fa été éliminé par §c%killer_arena%"),
    ARENA_DEATH("%main_color%Arena §7» §c%player_arena% §fest mort.")
    ;

    private final String defaultMessage;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat YEARS_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private static final Map<CommonString, String> TRANSLATIONS = new HashMap<>();

    CommonString(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public static void loadMessages(FileConfiguration config) {
        TRANSLATIONS.clear();
        for (CommonString lang : values()) {
            String path = "message." + lang.name();
            String message = config.getString(path);
            if (message == null) {
                config.set(path, lang.getDefaultMessage());
            }
            TRANSLATIONS.put(lang, message != null ? message.replace("&", "§") : lang.getDefaultMessage());
        }
        saveLangConfig(config);
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
            placeHolders.put("%team%", uhcPlayer.getTeam().isPresent() ? uhcPlayer.getTeam().get().name() : "Solo");
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
        String message = TRANSLATIONS.getOrDefault(this, this.defaultMessage);
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        for (Map.Entry<String, Object> entry : getPlaceHolders(uhcPlayer).entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
        }
        return message;
    }

    public String getMessage() {
        try {
            String message = TRANSLATIONS.getOrDefault(this, this.defaultMessage);
            for (Map.Entry<String, Object> entry : getPlaceHolders().entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue() != null ? entry.getValue().toString() : "");
            }
            return message;
        } catch (Exception e) {
            return getRawMessage();
        }
    }

    public String getRawMessage() {
        return TRANSLATIONS.getOrDefault(this, this.defaultMessage);
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