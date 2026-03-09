package net.novaproject.novauhc.lang.lang;

import net.novaproject.novauhc.lang.Lang;

import java.util.Map;


public enum CommonLang implements Lang {

    
    CLICK_HERE_TO_APPLY(
        " §8» §fCliquez pour §a§lappliquer§f.",
        " §8» §fClick to §a§lapply§f."
    ),
    CLICK_HERE_TO_ACTIVATE(
        " §8» §fCliquez pour §a§lactiver§f.",
        " §8» §fClick to §a§lactivate§f."
    ),
    CLICK_HERE_TO_DESACTIVATE(
        " §8» §fCliquez pour §c§ldésactiver§f.",
        " §8» §fClick to §c§ldeactivate§f."
    ),
    CLICK_HERE_TO_MODIFY(
        " §8» §fCliquez pour §6§lmodifier§f.",
        " §8» §fClick to §6§lmodify§f."
    ),
    CLICK_HERE_TO_ACCESS(
        " §8» §fCliquez pour y %main_color%accéder§f.",
        " §8» §fClick to %main_color%access§f."
    ),
    CLICK_HERE_TO_TOGGLE(
        " §8» §fCliquez ici pour §6§lactiver/désactiver§f.",
        " §8» §fClick here to §6§ltoggle§f."
    ),
    CLICK_GAUCHE(" §8» §fClic gauche pour §f", " §8» §fLeft click to §f"),
    CLICK_DROITE(" §8» §fClic droit pour §f", " §8» §fRight click to §f"),
    BAR(
        "§f§m                                                                           §r",
        "§f§m                                                                           §r"
    ),

    PAGE_NEXT(" §8» §fCliquez ici pour la %main_color%Suivante"," §8» §fClick here to go to the %main_color%Next page "),
    PAGE_PREVIOUS(" §8» §fCliquez ici pour la %main_color%Précédente"," §8» §fClick here to go to the %main_color%Previous page "),
    CONFIRM_HEADER(
            "§6§lConfirmation",
            "§6§lConfirmation"
    ),

    CONFIRM_BUTTON(
            "§aConfirmer",
            "§aConfirm"
    ),

    CONFIRM_BUTTON_LORE(
            "§7Cliquez pour valider l'action.",
            "§7Click to validate the action."
    ),

    CANCEL_BUTTON(
            "§cAnnuler",
            "§cCancel"
    ),

    CANCEL_BUTTON_LORE(
            "§7Cliquez pour revenir en arrière.",
            "§7Click to go back."
    ),
    
    NO_PERMISSION(
        "§cDésolé, vous n'avez pas la permission d'exécuter cette commande.",
        "§cSorry, you don't have permission to run this command."
    ),
    DISABLE_ACTION(
        "§cVous ne pouvez pas effectuer cette action !",
        "§cYou cannot perform this action!"
    ),
    SUCCESSFUL_ACTIVATION("§a✓ Activation effectuée avec succès !", "§a✓ Successfully activated!"),
    SUCCESSFUL_DESACTIVATION("§c✓ Désactivation effectuée avec succès !", "§c✓ Successfully deactivated!"),
    SUCCESSFUL_MODIFICATION("§6✓ Modification effectuée avec succès !", "§6✓ Successfully modified!"),
    RECONNECT_SUCCESS(
            "§a§lReconnexion réussie ! §7Vous avez été replacé dans la partie.",
            "§a§lReconnected! §7You have been placed back in the game."
    ),
    PLAYER_DISCONNECTED_TIMER(
            "§e%player% §7s'est déconnecté ! Il a §c%time% minutes §7pour se reconnecter.",
            "§e%player% §7disconnected! They have §c%time% minutes §7to reconnect."
    ),

    PLAYER_RECONNECTED_TIMER(
            "§e%player% §7s'est reconnecté avec §a%time% minutes §7restantes !",
            "§e%player% §7reconnected with §a%time% minutes §7remaining!"
    ),

    PLAYER_TIMEOUT_DEATH(
            "§c☠ %player% §7est mort (délai de reconnexion expiré)",
            "§c☠ %player% §7died (reconnection timeout)"
    ),

    PLAYER_RECONNECTED(
            "§e%player% §7s'est reconnecté !",
            "§e%player% §7reconnected!"
    ),

    CONNECTION_GAME(
            "§e%player% §7s'est connecté pendant la partie !",
            "§e%player% §7connected during the game!"
    ),
    KICKED("§cVous avez été exclu de la partie par l'host", "§cYou have been kicked from the game by the host"),
    KICKED_MESSAGE("§c» Le joueur §f%player% §ca été exclu de la partie par l'host !", "§c» Player §f%player% §cwas kicked by the host!"),
    WELCOME("§a§lBienvenue §fsur la partie de §6%host%§f !", "§a§lWelcome §fto §6%host%§f's game!"),
    WELCOME_HOST(
        "\n§8§l«§8§m--------------------------------------------------§8§l»\n§f \n§6│ §fBienvenue §6§l%player% sur votre partie §f!\n§f \n§6│ §fVous pouvez dès à présent configurer : §l§c /h config§f!\n§f \n§8§l«§8§m--------------------------------------------------§8§l»\n",
        "\n§8§l«§8§m--------------------------------------------------§8§l»\n§f \n§6│ §fWelcome §6§l%player% to your game§f!\n§f \n§6│ §fYou can now configure it with: §l§c /h config§f!\n§f \n§8§l«§8§m--------------------------------------------------§8§l»\n"
    ),
    WELCOME_SPECTATOR("§7Bienvenue en tant que §espectateur §fsur la partie de §6%host%§f !", "§7Welcome as §espectator §fto §6%host%§f's game!"),
    KICK_SPEC("§cConnexion refusée : L'host a désactivé le mode spectateur", "§cConnection refused: spectator mode disabled"),
    KICK_FULL("§cConnexion refusée : La partie est complète", "§cConnection refused: game is full"),
    KICK_WHITELIST("§cConnexion refusée : Vous n'êtes pas dans la whitelist", "§cConnection refused: not whitelisted"),
    ORE_BOOST("§aModification enregistrée §f! Recréez l'arène pour appliquer.", "§aChange saved§f! Regenerate the arena to apply."),

    
    CANT_TALK_DEATH("§cVous ne pouvez pas parler car vous êtes mort", "§cYou cannot chat because you are dead"),
    CANT_TALK_SPECTATOR("§cVous ne pouvez pas parler en tant que spectateur", "§cYou cannot chat as a spectator"),
    CHAT_GLOBAL_FORMAT("§7[§6Global§7] §f%s §8» §f%s", "§7[§6Global§7] §f%s §8» §f%s"),
    LOBBY_CHAT_FORMAT("§7[§6Lobby§7] §f%s §8» §f%s", "§7[§6Lobby§7] §f%s §8» §f%s"),
    SOLO_CHAT_FORMAT("§7[§6Solo§7] §f%s §8» §f%s", "§7[§6Solo§7] §f%s §8» §f%s"),
    TEAM_CHAT_FORMAT("§7[§6%team%§7] §f%s §8» §f%s", "§7[§6%team%§7] §f%s §8» §f%s"),
    HELPOP_MESSAGE("%helpop% §2» ", "%helpop% §2» "),
    CHAT_DISABLED("§cLe chat est actuellement désactivé !", "§cChat is currently disabled!"),
    MSG_USAGE("§c✗ Usage: §f/msg <joueur> <message>", "§c✗ Usage: §f/msg <player> <message>"),
    MSG_PLAYER_OFFLINE("§c✗ Ce joueur est hors ligne ou introuvable", "§c✗ Player not found or offline"),
    MSG_CANNOT_MESSAGE_SELF("§c✗ Vous ne pouvez pas vous envoyer un message à vous-même !", "§c✗ You cannot message yourself!"),
    MSG_SENT_FORMAT("§8│ §7§lMoi §7→ §7§l%target% §f%message%", "§8│ §7§lMe §7→ §7§l%target% §f%message%"),
    MSG_RECEIVED_FORMAT("§8│ §7§l%sender% §7→ §7§lMoi §f%message%", "§8│ §7§l%sender% §7→ §7§lMe §f%message%"),

    
    BLOCKED_ENCHANT("§cVous ne pouvez pas appliquer cet enchantement", "§cYou cannot apply this enchantment"),
    BLOCKED_POTION("§cVous ne pouvez pas créer cette potion", "§cYou cannot brew this potion"),
    BLOCKED_CRAFT("§cVous ne pouvez pas effectuer ce craft", "§cYou cannot perform this craft"),
    BLOCKED_CRAFT_ITEM("§cVous ne pouvez pas créer cet objet", "§cYou cannot craft this item"),
    DIAMOND_LIMIT_REACHED("§cVous avez atteint la limite de diamants minés", "§cYou have reached the diamond mining limit"),
    DIAMOND_LIMIT_INCREASED("§bDiamants minés : §f%mined_diamond%§7/§f%diamond_limite%", "§bDiamonds mined: §f%mined_diamond%§7/§f%diamond_limite%"),
    EXEDED_LIMITE("§cLimite dépassée", "§cLimit exceeded"),

    
    TP_MESSAGE("§a» §f%player% §aa été téléporté !", "§a» §f%player% §ahas been teleported!"),
    DECONNECTION_LOBBY("§c» §f%player% §cse déconnecte du lobby", "§c» §f%player% §cdisconnected from lobby"),
    DECONNECTION_GAME("§c» §f%player% §cse déconnecte de la partie", "§c» §f%player% §cdisconnected from game"),
    DEATH_MESSAGE("§c☠ §f%player% §ca été éliminé par §f%killer%§c !", "§c☠ §f%player% §cwas eliminated by §f%killer%§c!"),
    DEATH_MESSAGE_TEAM("§c☠ §7[§6%team%§7] §f%player% §ca été éliminé", "§c☠ §7[§6%team%§7] §f%player% §cwas eliminated"),
    JOIN_TEAM_MESSAGE("§aVous avez rejoint l'équipe §6%team%§a !", "§aYou joined team §6%team%§a!"),
    REVIVE_MESSAGE("§a✦ §f%player% §aa été ressuscité par §6%host%§a !", "§a✦ §f%player% §ahas been revived by §6%host%§a!"),
    INVULNERABLE_OFF("§c⚠ Vous n'êtes plus invulnérable !", "§c⚠ You are no longer invulnerable!"),
    SOLO_WIN(
            "§8§m─────────────────────────§r\n" +
                    "§6§lVictoire Solo\n" +
                    "§fLe joueur §e%player% §fa remporté la partie !\n" +
                    "§8§m─────────────────────────§r",
            "§8§m─────────────────────────§r\n" +
                    "§6§lSolo Victory\n" +
                    "§fPlayer §e%player% §fhas won the game!\n" +
                    "§8§m─────────────────────────§r"
    ),

    TEAM_WIN(
            "§8§m─────────────────────────§r\n" +
                    "§6§lVictoire d'équipe\n" +
                    "§fFélicitations à l'équipe §e%team% §fpour sa §6victoire§f !\n" +
                    "§6§lClassement :\n" +
                    "§8§m─────────────────────────§r",
            "§8§m─────────────────────────§r\n" +
                    "§6§lTeam Victory\n" +
                    "§fCongratulations to team §e%team% §ffor their §6victory§f!\n" +
                    "§6§lRanking:\n" +
                    "§8§m─────────────────────────§r"
    ),

    TEAM_RANK_LINE(
            "%prefix%§e#%rank% §f%player% §7- §c%kills% kills",
            "%prefix%§e#%rank% §f%player% §7- §c%kills% kills"
    ),

    TEAM_TOTAL_KILLS(
            "§6Total des kills : §c%total%",
            "§6Total kills: §c%total%"
    ),
    
    PVP_START("§c⚔ Le PVP est maintenant activé !", "§c⚔ PVP is now enabled!"),
    PVP_START_IN("§e⚡ Le PVP sera activé dans §a%time_before%", "§e⚡ PVP will be enabled in §a%time_before%"),
    MEETUP_START("§c§l♦ MEETUP ACTIVÉ ! §r§cLa bordure se réduit !", "§c§l♦ MEETUP ACTIVATED! §r§cThe border is shrinking!"),
    MEETUP_START_IN("§e⚡ Le Meetup sera activé dans §a%time_before%", "§e⚡ Meetup will be activated in §a%time_before%"),
    PREGEN_FINISHED("§a✓ Pré-génération terminée avec succès !", "§a✓ Pre-generation completed successfully!"),
    PREGEN_STARTED("§e⚡ Pré-génération en cours...", "§e⚡ Pre-generation in progress..."),

    
    TEAM_UPDATED("§6⚡ Les équipes ont été mises à jour !", "§6⚡ Teams have been updated!"),
    TEAM_DESACTIVATED("§c⚡ Les équipes ont été désactivées !", "§c⚡ Teams have been disabled!"),
    TEAM_REDFINIED_AUTO("§6◉ Les équipes ont été redéfinies automatiquement !", "§6◉ Teams have been automatically redefined!"),
    GIVING_ROLES("§6♦ Distribution des rôles en cours...", "§6♦ Distributing roles..."),

    
    HOST_SAY_USAGE("§c✗ Usage: §f/h say <message>", "§c✗ Usage: §f/h say <message>"),
    HOST_COHOST_USAGE("§c✗ Usage: §f/h cohost <add|remove> <joueur>", "§c✗ Usage: §f/h cohost <add|remove> <player>"),
    HOST_BYPASS_ENABLED("§a✓ Mode bypass activé (Créatif)", "§a✓ Bypass mode enabled (Creative)"),
    HOST_BYPASS_DISABLED("§c✗ Mode bypass désactivé (Survie)", "§c✗ Bypass mode disabled (Survival)"),
    HOST_HELP_MESSAGE(
        "§6◆ Commandes Host:\n§f/h config §7- Menu de configuration\n§f/h bypass §7- Créatif\n§f/h cohost <add|remove> <joueur>\n§f/h whitelist <add|remove|list|clear|on|off>\n§f/h say <message>\n§f/h title <message>\n§f/h heal §7- Soigne tous\n§f/h forcepvp §7- Force PvP\n§f/h forcemtp §7- Force Meetup\n§f/h stuff <modif|save|clear>",
        "§6◆ Host Commands:\n§f/h config §7- Configuration menu\n§f/h bypass §7- Creative mode\n§f/h cohost <add|remove> <player>\n§f/h whitelist <add|remove|list|clear|on|off>\n§f/h say <message>\n§f/h title <message>\n§f/h heal §7- Heal all\n§f/h forcepvp §7- Force PvP\n§f/h forcemtp §7- Force Meetup\n§f/h stuff <modif|save|clear>"
    ),
    PLAYER_HELP_MESSAGE(
        "§6◆ Commandes Joueur:\n§f/p helpop <message> §7- Message aux hosts",
        "§6◆ Player Commands:\n§f/p helpop <message> §7- Message to hosts"
    ),
    HEAL_BROADCAST("§a♥ Heal effectué ! Tous les joueurs ont été soignés.", "§a♥ Heal done! All players healed."),
    TITLE_USAGE("§c✗ Usage: §f/h title <message>", "§c✗ Usage: §f/h title <message>"),
    REVIVE_USAGE("§c✗ Usage: §f/h revive <joueur>", "§c✗ Usage: §f/h revive <player>"),
    WHITELIST_USAGE("§c✗ Usage: §f/h whitelist <add|remove|list|clear|on|off> [joueur]", "§c✗ Usage: §f/h whitelist <add|remove|list|clear|on|off> [player]"),
    STUFF_USAGE("§c✗ Usage: §f/h stuff <modif|save|clear>", "§c✗ Usage: §f/h stuff <modif|save|clear>"),
    COHOST_ADDED("§a✓ §f%player% §aa été ajouté comme co-host", "§a✓ §f%player% §aadded as co-host"),
    COHOST_REMOVED("§c✓ §f%player% §ca été retiré des co-hosts", "§c✓ §f%player% §cremoved from co-hosts"),
    COHOST_ALREADY_EXISTS("§c✗ Ce joueur est déjà co-host", "§c✗ Player is already co-host"),
    COHOST_NOT_FOUND("§c✗ Ce joueur n'est pas co-host", "§c✗ Player is not co-host"),
    CONFIG_CANNOT_INGAME("§c✗ Impossible de configurer en cours de partie", "§c✗ Cannot configure during a game"),
    PLAYER_NOT_FOUND("§c✗ Joueur introuvable ou hors ligne", "§c✗ Player not found or offline"),
    FORCE_PVP_BROADCAST("§c⚔ Le PVP a été forcé par l'host !", "§c⚔ PVP forced by the host!"),
    FORCE_MEETUP_BROADCAST("§c♦ Le Meetup a été forcé par l'host !", "§c♦ Meetup forced by the host!"),
    HOST_GIVE("§a✓ §f%host% §aa donné %item% %amont%", "§a✓ §f%host% §agave %item% %amont%"),
    ACCESS_HOST("§7» Accès %main_color%Host","§7» Access %main_color%Host"),

    
    FINAL_HEAL_BROADCAST("§a♥ Final Heal effectué ! Tous les joueurs ont été soignés.", "§a♥ Final Heal done! All players healed."),
    TIMEBOMB_EXPLOSION("§c☠ Le corps de §f%player% §ca explosé !", "§c☠ §f%player%§c's body exploded!"),

    
    ERROR_INVALID_NUMBER("§c✗ Nombre invalide.", "§c✗ Invalid number."),
    ERROR_NUMBER_TOO_LOW("§c✗ Le nombre doit être supérieur à %min%", "§c✗ Number must be greater than %min%"),
    ERROR_NUMBER_TOO_HIGH("§c✗ Le nombre doit être inférieur à %max%", "§c✗ Number must be less than %max%"),
    ERROR_INVALID_ARGUMENT("§c✗ Argument invalide: §f%argument%", "§c✗ Invalid argument: §f%argument%"),
    ERROR_MISSING_ARGUMENT("§c✗ Argument manquant. Consultez l'aide.", "§c✗ Missing argument. Check the help."),
    ERROR_COMMAND_DISABLED("§c✗ Cette commande est actuellement désactivée", "§c✗ This command is currently disabled"),
    ERROR_PLAYER_OFFLINE("§c✗ §f%player% §cn'est pas en ligne", "§c✗ §f%player% §cis not online"),
    ERROR_PLAYER_NOT_IN_GAME("§c✗ §f%player% §cn'est pas en jeu", "§c✗ §f%player% §cis not in the game"),
    ERROR_ALREADY_IN_PROGRESS("§c✗ Une opération est déjà en cours", "§c✗ An operation is already in progress"),
    ERROR_NOT_STARTED("§c✗ Aucune partie n'est en cours", "§c✗ No game is running"),
    ERROR_COOLDOWN_ACTIVE("§c✗ Attendez §f%time% §cavant de réutiliser cette commande", "§c✗ Wait §f%time% §cbefore using this command again"),
    ERROR_PERMISSION_DENIED("§c✗ Permission refusée", "§c✗ Permission denied"),
    DIM_NOT_ACCEIBLE("§cCette dimension n'est pas accessible", "§cThis dimension is not accessible"),

    
    SUCCESS_OPERATION_COMPLETED("§a✓ Opération terminée avec succès", "§a✓ Operation completed successfully"),
    SUCCESS_PLAYER_ADDED("§a✓ §f%player% §aajouté avec succès", "§a✓ §f%player% §aadded successfully"),
    SUCCESS_PLAYER_REMOVED("§a✓ §f%player% §cretiré avec succès", "§a✓ §f%player% §cremoved successfully"),
    SUCCESS_SETTINGS_SAVED("§a✓ Paramètres sauvegardés avec succès", "§a✓ Settings saved successfully"),
    SUCCESS_TELEPORT_COMPLETED("§a✓ Téléportation effectuée", "§a✓ Teleportation completed"),

    
    INFO_LOADING("§e⚡ Chargement en cours...", "§e⚡ Loading..."),
    INFO_PROCESSING("§e⚡ Traitement en cours...", "§e⚡ Processing..."),
    INFO_PLEASE_WAIT("§e⌛ Veuillez patienter...", "§e⌛ Please wait..."),
    INFO_OPERATION_CANCELLED("§7◆ Opération annulée", "§7◆ Operation cancelled"),
    INFO_NO_CHANGES_MADE("§7◆ Aucune modification effectuée", "§7◆ No changes made"),


    
    DISCORD_MESSAGE("§9│ §f§lDiscord §8» ", "§9│ §f§lDiscord §8» "),
    DISCORD_MESSAGE_HOVER("%main_color% » Clique ici pour ouvrir le Discord !", "%main_color% » Click here to open Discord!"),
    DOCUMENT_MESSAGE("§9│ §f§lDocument §8» ", "§9│ §f§lDocument §8» "),
    DOCUMENT_MESSAGE_HOVER("%main_color% » Clique ici pour ouvrir le Document !", "%main_color% » Click here to open the Document!"),

    
    ARENA_JOIN("%main_color%Arena §7» §c%player% §fa rejoint l'arène !", "%main_color%Arena §7» §c%player% §fjoined the arena!"),
    ARENA_KILL("%main_color%Arena §7» §c%player_arena% §fa été éliminé par §c%killer_arena%", "%main_color%Arena §7» §c%player_arena% §fwas eliminated by §c%killer_arena%"),
    ARENA_DEATH("%main_color%Arena §7» §c%player_arena% §fest mort.", "%main_color%Arena §7» §c%player_arena% §fdied."),

    
    WHITELIST_ENABLE_BUTTON("§a✓ Activer la Whitelist", "§a✓ Enable Whitelist"),
    WHITELIST_DISABLE_BUTTON("§c✗ Désactiver la Whitelist", "§c✗ Disable Whitelist"),
    RANDOM_TEAMS_BUTTON("§f◆ Équipes aléatoires", "§f◆ Random Teams"),

    ;


    private final Map<String, String> translations;

    CommonLang(String fr, String en) {
        this.translations = Map.of("fr_FR", fr, "en_US", en);
    }

    @Override
    public String getKey() {
        return "common." + name();
    }

    @Override
    public Map<String, String> getTranslations() {
        return translations;
    }
}
