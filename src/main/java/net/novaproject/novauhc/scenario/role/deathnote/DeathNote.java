package net.novaproject.novauhc.scenario.role.deathnote;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.deathnote.roles.*;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeathNote extends ScenarioRole<DeathNoteRole> {

    // Statistiques de la partie
    private final Map<UHCPlayer, Integer> playerKills = new HashMap<>();
    private final List<UHCPlayer> deadPlayers = new ArrayList<>();
    // Gestionnaires des systèmes Death Note
    private DeathNoteManager deathNoteManager;
    private EpisodeManager episodeManager;
    private KiraChatManager kiraChatManager;

    @Override
    public String getName() {
        return "Death Note UHC";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            UHCManager.get().setTeam_size(3);
        }
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WRITTEN_BOOK)
                .setName("§c§lDeath Note UHC")
                .addLore("§7Mode de jeu avec rôles")
                .addLore("§7Traîtres vs Détectives")
                .addLore("")
                .addLore("§cRôles méchants :")
                .addLore("§8• §cKira §7(traître principal)")
                .addLore("§8• §5Shinigami §7(allié Kira)")
                .addLore("")
                .addLore("§aRôles gentils :")
                .addLore("§8• §aEnquêteur §7(détective)")
                .addLore("§8• §bNear §7(spécialiste)")
                .addLore("")
                .addLore("§6Rôle variable :")
                .addLore("§8• §6Mello §7(change de camp)");
    }

    @Override
    public void setup() {
        super.setup();

        // Ajouter tous les rôles disponibles
        addRole(KiraRole.class);
        addRole(EnqueteurRole.class);
        addRole(NearRole.class);
        addRole(MelloRole.class);
        addRole(ShinigamiRole.class);

        // Initialiser les gestionnaires
        this.deathNoteManager = new DeathNoteManager(this);
        this.episodeManager = new EpisodeManager(this);
        this.kiraChatManager = new KiraChatManager(this);

        // Configuration par défaut des rôles
        setupDefaultRoleConfiguration();
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() < 3) {
            UHCManager.get().setTeam_size(3);
            for (UHCPlayer player : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                player.getPlayer().sendMessage("§c[Death Note UHC] §fTaille d'équipe ajustée à 3 minimum pour ce mode de jeu.");
            }
        }
    }

    /**
     * Configure la répartition par défaut des rôles selon les équipes
     */
    private void setupDefaultRoleConfiguration() {
        // Configuration basée sur le nombre de joueurs et d'équipes
        int playerCount = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();
        int teamCount = UHCTeamManager.get().getAliveTeams().size();
        int teamSize = UHCManager.get().getTeam_size();

        // Calculer le nombre de traîtres par équipe (environ 1/3 des joueurs)
        int traitorsPerTeam = Math.max(1, teamSize / 3);
        int totalTraitors = teamCount * traitorsPerTeam;

        // Répartir les rôles traîtres
        int kiraCount = Math.max(1, totalTraitors / 2); // 50% des traîtres sont Kira
        int shinigamiCount = Math.max(0, totalTraitors / 4); // 25% sont Shinigami
        int melloCount = Math.max(0, totalTraitors - kiraCount - shinigamiCount); // Le reste sont Mello

        // Répartir les rôles gentils
        int gentilsCount = playerCount - totalTraitors;
        int enqueteurCount = Math.max(1, gentilsCount / 6); // 1 Enquêteur pour 6 gentils
        int nearCount = Math.max(0, gentilsCount / 8); // 1 Near pour 8 gentils

        // Ajouter les rôles
        for (int i = 0; i < kiraCount; i++) {
            incrementRole(KiraRole.class);
        }
        for (int i = 0; i < shinigamiCount; i++) {
            incrementRole(ShinigamiRole.class);
        }
        for (int i = 0; i < melloCount; i++) {
            incrementRole(MelloRole.class);
        }
        for (int i = 0; i < enqueteurCount; i++) {
            incrementRole(EnqueteurRole.class);
        }
        for (int i = 0; i < nearCount; i++) {
            incrementRole(NearRole.class);
        }

        // Log de la configuration
        System.out.println("[Death Note UHC] Configuration des rôles :");
        System.out.println("- Équipes : " + teamCount + " (taille " + teamSize + ")");
        System.out.println("- Kira : " + kiraCount);
        System.out.println("- Shinigami : " + shinigamiCount);
        System.out.println("- Mello : " + melloCount);
        System.out.println("- Enquêteur : " + enqueteurCount);
        System.out.println("- Near : " + nearCount);
    }

    @Override
    public void giveRoles() {
        super.giveRoles();

        // Démarrer les systèmes après attribution des rôles
        episodeManager.startEpisodeSystem();
        kiraChatManager.initializeKiraChat();

        // Annoncer le début du mode Death Note
        announceGameStart();
    }

    /**
     * Annonce le début du mode Death Note
     */
    private void announceGameStart() {
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player p = player.getPlayer();
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            p.sendMessage("§c§l[DEATH NOTE UHC]");
            p.sendMessage("");
            p.sendMessage("§fLe mode Death Note a commencé !");
            p.sendMessage("§fChaque équipe contient des §aGentils §fet des §cTraîtres§f.");
            p.sendMessage("§fLes §cKira §fdoivent trahir leur équipe.");
            p.sendMessage("§fLes §aGentils §fdoivent démasquer les traîtres.");
            p.sendMessage("");
            p.sendMessage("§7Que le meilleur camp gagne...");
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        }
    }

    @Override
    public void onSec(Player p) {
        super.onSec(p);

        // Mettre à jour les systèmes chaque seconde
        if (deathNoteManager != null) {
            deathNoteManager.onSecond();
        }
        if (episodeManager != null) {
            episodeManager.onSecond();
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        super.onDeath(uhcPlayer, killer, event);

        // Ajouter à la liste des morts
        deadPlayers.add(uhcPlayer);

        // Mettre à jour les statistiques de kill
        if (killer != null) {
            playerKills.put(killer, playerKills.getOrDefault(killer, 0) + 1);
        }

        // Notifier les gestionnaires
        if (deathNoteManager != null) {
            deathNoteManager.onPlayerDeath(uhcPlayer, killer);
        }

        // Les conditions de victoire sont vérifiées automatiquement par isWin()
    }

    @Override
    public void onDeathNoteCMD(Player player, String subCommand, String[] args) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        // Gérer les commandes spécifiques au Death Note
        switch (subCommand.toLowerCase()) {
            case "k":
                // Chat Kira
                if (kiraChatManager != null) {
                    kiraChatManager.handleKiraChat(uhcPlayer, args);
                }
                break;

            default:
                // Toutes les autres commandes sont des commandes Death Note
                handleDeathNoteCommands(uhcPlayer, subCommand, args);
                break;
        }
    }


    @Override
    public boolean isWin() {
        // Vérifier s'il ne reste qu'une seule équipe en vie
        List<UHCTeam> aliveTeams = UHCTeamManager.get().getAliveTeams();

        if (aliveTeams.size() <= 1) {
            // Une seule équipe survivante ou aucune équipe = fin de partie
            if (aliveTeams.size() == 1) {
                // Annoncer la victoire de l'équipe survivante
                UHCTeam winningTeam = aliveTeams.get(0);
                announceTeamWin(winningTeam);
            } else {
                // Égalité - toutes les équipes éliminées
                announceWin("§7ÉGALITÉ", "Toutes les équipes ont été éliminées simultanément !");
            }
            return true; // Fin de partie
        }

        // Vérifier le statut des traîtres dans chaque équipe (pour les messages informatifs)
        checkTraitorStatus();

        return false; // Partie continue
    }

    /**
     * Vérifie le statut des traîtres dans chaque équipe
     */
    private void checkTraitorStatus() {
        for (UHCTeam team : UHCTeamManager.get().getAliveTeams()) {
            List<UHCPlayer> teamPlayers = team.getPlayers();
            List<UHCPlayer> aliveTraitors = new ArrayList<>();
            List<UHCPlayer> aliveGentils = new ArrayList<>();

            for (UHCPlayer player : teamPlayers) {
                if (player.isPlaying() && !deadPlayers.contains(player)) {
                    DeathNoteRole role = getRoleByUHCPlayer(player);
                    if (role != null) {
                        if (role.isKiraTeam()) {
                            aliveTraitors.add(player);
                        } else if (role.getRoleCamp() == DeathNoteRole.DeathNoteCamps.GENTIL) {
                            aliveGentils.add(player);
                        }
                    }
                }
            }

            // Si tous les traîtres d'une équipe sont morts, informer les gentils
            if (aliveTraitors.isEmpty() && !aliveGentils.isEmpty()) {
                for (UHCPlayer gentil : aliveGentils) {
                    gentil.getPlayer().sendMessage("§a[Death Note] §fTous les traîtres de votre équipe ont été éliminés ! Votre équipe est maintenant unie.");
                }
            }
        }
    }

    /**
     * Annonce la victoire d'une équipe
     */
    private void announceTeamWin(UHCTeam winningTeam) {
        // Analyser la composition de l'équipe gagnante
        List<UHCPlayer> winners = winningTeam.getPlayers();
        List<UHCPlayer> gentilsWinners = new ArrayList<>();
        List<UHCPlayer> traitorsWinners = new ArrayList<>();

        for (UHCPlayer player : winners) {
            if (player.isPlaying()) {
                DeathNoteRole role = getRoleByUHCPlayer(player);
                if (role != null) {
                    if (role.isKiraTeam()) {
                        traitorsWinners.add(player);
                    } else if (role.getRoleCamp() == DeathNoteRole.DeathNoteCamps.GENTIL) {
                        gentilsWinners.add(player);
                    }
                }
            }
        }

        // Déterminer le type de victoire
        String victoryType;
        if (traitorsWinners.isEmpty()) {
            victoryType = "§a§lVICTOIRE DES GENTILS";
        } else if (gentilsWinners.isEmpty()) {
            victoryType = "§c§lVICTOIRE DES TRAÎTRES";
        } else {
            victoryType = "§6§lVICTOIRE MIXTE";
        }

        // Annoncer la victoire
        for (UHCPlayer player : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            Player p = player.getPlayer();
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            p.sendMessage("§6§l[DEATH NOTE UHC - VICTOIRE]");
            p.sendMessage("");
            p.sendMessage("§fÉquipe gagnante : " + winningTeam.getPrefix() + winningTeam.getName());
            p.sendMessage("§fType de victoire : " + victoryType);
            p.sendMessage("");

            if (!gentilsWinners.isEmpty()) {
                p.sendMessage("§a§lGentils survivants :");
                for (UHCPlayer gentil : gentilsWinners) {
                    DeathNoteRole role = getRoleByUHCPlayer(gentil);
                    String roleName = role != null ? role.getName() : "Gentil";
                    p.sendMessage("§8• §a" + gentil.getPlayer().getName() + " §7(" + roleName + ")");
                }
            }

            if (!traitorsWinners.isEmpty()) {
                p.sendMessage("§c§lTraîtres survivants :");
                for (UHCPlayer traitor : traitorsWinners) {
                    DeathNoteRole role = getRoleByUHCPlayer(traitor);
                    String roleName = role != null ? role.getName() : "Traître";
                    p.sendMessage("§8• §c" + traitor.getPlayer().getName() + " §7(" + roleName + ")");
                }
            }

            p.sendMessage("");
            p.sendMessage("§7Félicitations aux vainqueurs !");
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        }
    }

    /**
     * Annonce la victoire d'un camp (méthode de fallback)
     */
    private void announceWin(String camp, String reason) {
        for (UHCPlayer player : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            Player p = player.getPlayer();
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            p.sendMessage("§6§l[VICTOIRE DEATH NOTE]");
            p.sendMessage("");
            p.sendMessage("§fVictoire du camp : " + camp);
            p.sendMessage("§fRaison : §7" + reason);
            p.sendMessage("");
            p.sendMessage("§7Félicitations aux vainqueurs !");
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        }
    }

    @Override
    public String getPath() {
        return "deathnote";
    }

    @Override
    public ScenarioLang[] getLang() {
        return DeathNoteLang.values();
    }

    @Override
    public Camps[] getCamps() {
        return DeathNoteRole.DeathNoteCamps.values();
    }

    @Override
    public CustomInventory getMenu(Player player) {
        return new DeathNoteConfigUi(player);
    }

    public DeathNoteManager getDeathNoteManager() {
        return deathNoteManager;
    }

    public EpisodeManager getEpisodeManager() {
        return episodeManager;
    }

    public KiraChatManager getKiraChatManager() {
        return kiraChatManager;
    }

    public Map<UHCPlayer, Integer> getPlayerKills() {
        return playerKills;
    }

    public List<UHCPlayer> getDeadPlayers() {
        return deadPlayers;
    }

    public Plugin getPlugin() {
        return Main.get();
    }

    /**
     * Gère toutes les commandes Death Note
     */
    private void handleDeathNoteCommands(UHCPlayer player, String subCommand, String[] args) {
        DeathNoteRole role = getRoleByUHCPlayer(player);

        switch (subCommand.toLowerCase()) {
            case "use":
                handleUseCommand(player, args, role);
                break;

            case "claim":
                handleClaimCommand(player, role);
                break;

            case "reveal":
                handleRevealCommand(player, role);
                break;

            case "abandon":
                handleAbandonCommand(player, role);
                break;

            case "investigate":
                handleInvestigateCommand(player, args, role);
                break;

            case "analyze":
                handleAnalyzeCommand(player, args, role);
                break;

            case "detect":
                handleDetectCommand(player, args, role);
                break;

            case "form":
                handleFormCommand(player, role);
                break;

            case "pact":
                handlePactCommand(player, args, role);
                break;

            case "track":
                handleTrackCommand(player, role);
                break;

            case "info":
                handleInfoCommand(player, role);
                break;

            case "status":
                handleStatusCommand(player);
                break;

            case "help":
            default:
                // Si aucune commande spécifique ou "help", afficher l'aide
                sendDeathNoteHelp(player);
                break;
        }
    }

    /**
     * Gère la commande /dnote use <nom>
     */
    private void handleUseCommand(UHCPlayer player, String[] args, DeathNoteRole role) {
        if (args.length < 2) {
            player.getPlayer().sendMessage("§c[Death Note] §fUsage : /dnote use <nom>");
            return;
        }

        if (role == null || !role.canUseDeathNote()) {
            player.getPlayer().sendMessage("§c[Death Note] §fVous ne pouvez pas utiliser le Death Note !");
            return;
        }

        String targetName = args[1];
        int currentEpisode = episodeManager.getCurrentEpisode();
        deathNoteManager.useDeathNote(player, targetName, currentEpisode);
    }

    /**
     * Gère la commande /dnote claim
     */
    private void handleClaimCommand(UHCPlayer player, DeathNoteRole role) {
        if (role == null) {
            player.getPlayer().sendMessage("§c[Death Note] §fVous n'avez pas de rôle assigné !");
            return;
        }

        // Redonner les items du rôle
        role.giveRoleItems(player);
        player.getPlayer().sendMessage("§a[Death Note] §fVous avez récupéré vos pouvoirs !");
    }

    /**
     * Gère la commande /dnote reveal
     */
    private void handleRevealCommand(UHCPlayer player, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.KiraRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls les Kira peuvent se révéler !");
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.KiraRole kira =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.KiraRole) role;

        if (kira.revealIdentity()) {
            // Annoncer publiquement
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                uhcPlayer.getPlayer().sendMessage("§c§l[RÉVÉLATION] §c" + player.getPlayer().getName() +
                        " §fest un Kira et s'est révélé publiquement !");
            }
            player.getPlayer().sendMessage("§c[Death Note] §fVous vous êtes révélé ! Vous gagnez une pomme en or et pouvez cibler tous les joueurs.");
        } else {
            player.getPlayer().sendMessage("§c[Death Note] §fVous vous êtes déjà révélé !");
        }
    }

    /**
     * Gère la commande /dnote abandon
     */
    private void handleAbandonCommand(UHCPlayer player, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.KiraRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls les Kira peuvent utiliser le pouvoir d'abandon !");
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.KiraRole kira =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.KiraRole) role;

        if (kira.useAbandonPower()) {
            player.getPlayer().sendMessage("§9[Death Note] §fVous avez activé le pouvoir d'abandon ! Vous paraissez innocent pendant 20 minutes.");
        } else {
            player.getPlayer().sendMessage("§c[Death Note] §fVous êtes déjà en mode abandon !");
        }
    }

    /**
     * Gère la commande /dnote investigate <nom>
     */
    private void handleInvestigateCommand(UHCPlayer player, String[] args, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.EnqueteurRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls les Enquêteurs peuvent enquêter !");
            return;
        }

        if (args.length < 2) {
            player.getPlayer().sendMessage("§c[Death Note] §fUsage : /dnote investigate <nom>");
            return;
        }

        String targetName = args[1];
        UHCPlayer target = findPlayerByName(targetName);
        if (target == null) {
            player.getPlayer().sendMessage("§c[Death Note] §fJoueur introuvable : " + targetName);
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.EnqueteurRole enqueteur =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.EnqueteurRole) role;

        enqueteur.investigate(player, target);
    }

    /**
     * Gère la commande /dnote analyze <nom>
     */
    private void handleAnalyzeCommand(UHCPlayer player, String[] args, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.NearRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls Near peut analyser !");
            return;
        }

        if (args.length < 2) {
            player.getPlayer().sendMessage("§c[Death Note] §fUsage : /dnote analyze <nom>");
            return;
        }

        String targetName = args[1];
        UHCPlayer target = findPlayerByName(targetName);
        if (target == null) {
            player.getPlayer().sendMessage("§c[Death Note] §fJoueur introuvable : " + targetName);
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.NearRole near =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.NearRole) role;

        near.performAnalysis(player, target);
    }

    /**
     * Gère la commande /dnote detect <nom>
     */
    private void handleDetectCommand(UHCPlayer player, String[] args, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.NearRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls Near peut détecter les Kira !");
            return;
        }

        if (args.length < 2) {
            player.getPlayer().sendMessage("§c[Death Note] §fUsage : /dnote detect <nom>");
            return;
        }

        String targetName = args[1];
        UHCPlayer target = findPlayerByName(targetName);
        if (target == null) {
            player.getPlayer().sendMessage("§c[Death Note] §fJoueur introuvable : " + targetName);
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.NearRole near =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.NearRole) role;

        near.useKiraDetector(player, target);
    }

    /**
     * Gère la commande /dnote form
     */
    private void handleFormCommand(UHCPlayer player, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.MelloRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls Mello peut changer de forme !");
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.MelloRole mello =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.MelloRole) role;

        if (mello.changeForm(player)) {
            // Mettre à jour l'équipe Kira si nécessaire
            kiraChatManager.updateKiraTeamMembers();
        }
    }

    /**
     * Gère la commande /dnote pact <nom>
     */
    private void handlePactCommand(UHCPlayer player, String[] args, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.ShinigamiRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls les Shinigami peuvent faire des pactes !");
            return;
        }

        if (args.length < 2) {
            player.getPlayer().sendMessage("§c[Death Note] §fUsage : /dnote pact <nom>");
            return;
        }

        String targetName = args[1];
        UHCPlayer target = findPlayerByName(targetName);
        if (target == null) {
            player.getPlayer().sendMessage("§c[Death Note] §fJoueur introuvable : " + targetName);
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.ShinigamiRole shinigami =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.ShinigamiRole) role;

        shinigami.makePactWithKira(player, target);
    }

    /**
     * Gère la commande /dnote track
     */
    private void handleTrackCommand(UHCPlayer player, DeathNoteRole role) {
        if (!(role instanceof net.novaproject.novauhc.scenario.role.deathnote.roles.ShinigamiRole)) {
            player.getPlayer().sendMessage("§c[Death Note] §fSeuls les Shinigami peuvent tracker !");
            return;
        }

        net.novaproject.novauhc.scenario.role.deathnote.roles.ShinigamiRole shinigami =
                (net.novaproject.novauhc.scenario.role.deathnote.roles.ShinigamiRole) role;

        shinigami.useTracker(player);
    }

    /**
     * Gère la commande /dnote info
     */
    private void handleInfoCommand(UHCPlayer player, DeathNoteRole role) {
        Player p = player.getPlayer();

        p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        p.sendMessage("§c§l[DEATH NOTE - INFORMATIONS]");
        p.sendMessage("");

        if (role != null) {
            p.sendMessage("§fVotre rôle : " + role.getColor() + "§l" + role.getName());
            p.sendMessage("§fCamp : " + role.getRoleCamp().getColor() + role.getRoleCamp().getName());
            p.sendMessage("");
            p.sendMessage("§7" + role.getDescription());
        } else {
            p.sendMessage("§7Vous n'avez pas encore de rôle assigné.");
        }

        p.sendMessage("");
        p.sendMessage("§e§lInformations de partie :");
        p.sendMessage("§8• §fÉpisode actuel : §e" + episodeManager.getCurrentEpisode());
        p.sendMessage("§8• §f" + episodeManager.getEpisodeInfo());
        p.sendMessage("§8• §fKira vivants : §c" + kiraChatManager.getAliveKiraCount());

        p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
    }

    /**
     * Gère la commande /dnote status
     */
    private void handleStatusCommand(UHCPlayer player) {
        Player p = player.getPlayer();

        p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        p.sendMessage("§3§l[DEATH NOTE - STATUT DE PARTIE]");
        p.sendMessage("");

        // Statistiques générales
        int totalPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size();
        int deadPlayers = getDeadPlayers().size();
        int alivePlayers = totalPlayers - deadPlayers;

        p.sendMessage("§e§lJoueurs :");
        p.sendMessage("§8• §fTotal : §e" + totalPlayers);
        p.sendMessage("§8• §aVivants : §e" + alivePlayers);
        p.sendMessage("§8• §cMorts : §e" + deadPlayers);
        p.sendMessage("");

        // Statistiques des équipes
        List<UHCTeam> aliveTeams = UHCTeamManager.get().getAliveTeams();

        p.sendMessage("§e§lÉquipes :");
        p.sendMessage("§8• §fÉquipes survivantes : §e" + aliveTeams.size());

        for (UHCTeam team : aliveTeams) {
            TeamStats stats = getTeamStats(team);
            p.sendMessage("§8• " + team.getPrefix() + team.getName() + " §7: §a" + stats.getGentils().size() +
                    " gentils, §c" + stats.getTraitors().size() + " traîtres");
        }
        p.sendMessage("");

        // Informations d'épisode
        p.sendMessage("§e§lÉpisode :");
        p.sendMessage("§8• §fÉpisode actuel : §e" + episodeManager.getCurrentEpisode());
        p.sendMessage("§8• §f" + episodeManager.getEpisodeInfo());

        p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
    }

    /**
     * Trouve un joueur par son nom
     */
    private UHCPlayer findPlayerByName(String name) {
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            if (player.getPlayer().getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    /**
     * Envoie l'aide des commandes Death Note
     */
    private void sendDeathNoteHelp(UHCPlayer player) {
        // Utiliser la même logique que DeathNoteCMD
        DeathNoteRole role = getRoleByUHCPlayer(player);
        Player p = player.getPlayer();

        p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        p.sendMessage("§c§l[DEATH NOTE UHC - COMMANDES]");
        p.sendMessage("");

        if (role != null) {
            p.sendMessage("§fVotre rôle : " + role.getColor() + "§l" + role.getName());
            p.sendMessage("§fCamp : " + role.getRoleCamp().getColor() + role.getRoleCamp().getName());
            p.sendMessage("");

            // Commandes spécifiques au rôle
            sendRoleSpecificHelp(p, role);
        } else {
            p.sendMessage("§7Vous n'avez pas encore de rôle assigné.");
            p.sendMessage("");
        }

        // Commandes générales
        p.sendMessage("§e§lCommandes générales :");
        p.sendMessage("§8• §a/dnote info §7- Informations sur votre rôle");
        p.sendMessage("§8• §a/dnote status §7- Statut de la partie");
        p.sendMessage("§8• §a/dnote help §7- Afficher cette aide");

        p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
    }

    /**
     * Envoie l'aide spécifique au rôle du joueur
     */
    private void sendRoleSpecificHelp(Player player, DeathNoteRole role) {
        String roleName = role.getName().toLowerCase();

        switch (roleName) {
            case "kira":
                player.sendMessage("§c§lCommandes Kira :");
                player.sendMessage("§8• §c/dnote use <nom> §7- Utiliser le Death Note");
                player.sendMessage("§8• §c/dnote claim §7- Récupérer vos pouvoirs");
                player.sendMessage("§8• §c/dnote reveal §7- Révéler votre identité");
                player.sendMessage("§8• §c/dnote abandon §7- Activer le mode abandon");
                player.sendMessage("§8• §c/k <message> §7- Chat privé Kira");
                break;

            case "enquêteur":
            case "enqueteur":
                player.sendMessage("§a§lCommandes Enquêteur :");
                player.sendMessage("§8• §a/dnote investigate <nom> §7- Enquêter sur un joueur");
                break;

            case "near":
                player.sendMessage("§b§lCommandes Near :");
                player.sendMessage("§8• §b/dnote analyze <nom> §7- Analyser un joueur");
                player.sendMessage("§8• §b/dnote detect <nom> §7- Détecter si c'est un Kira");
                break;

            case "mello":
                player.sendMessage("§6§lCommandes Mello :");
                player.sendMessage("§8• §6/dnote form §7- Changer de forme");
                if (role.canUseKiraChat()) {
                    player.sendMessage("§8• §6/k <message> §7- Chat Kira (si méchant)");
                }
                break;

            case "shinigami":
                player.sendMessage("§5§lCommandes Shinigami :");
                player.sendMessage("§8• §5/dnote pact <nom> §7- Faire un pacte avec un Kira");
                player.sendMessage("§8• §5/dnote track §7- Localiser votre Kira");
                break;

            default:
                player.sendMessage("§7Aucune commande spécifique pour votre rôle.");
                break;
        }
        player.sendMessage("");
    }

    /**
     * Retourne les statistiques d'une équipe (gentils vs traîtres)
     */
    public TeamStats getTeamStats(UHCTeam team) {
        List<UHCPlayer> gentils = new ArrayList<>();
        List<UHCPlayer> traitors = new ArrayList<>();
        List<UHCPlayer> unknown = new ArrayList<>();

        for (UHCPlayer player : team.getPlayers()) {
            if (player.isPlaying()) {
                DeathNoteRole role = getRoleByUHCPlayer(player);
                if (role != null) {
                    if (role.isKiraTeam()) {
                        traitors.add(player);
                    } else if (role.getRoleCamp() == DeathNoteRole.DeathNoteCamps.GENTIL) {
                        gentils.add(player);
                    } else {
                        unknown.add(player); // Mello variable
                    }
                } else {
                    unknown.add(player); // Pas encore de rôle
                }
            }
        }

        return new TeamStats(team, gentils, traitors, unknown);
    }

    /**
     * Vérifie si un joueur peut cibler un autre joueur
     */
    public boolean canTarget(UHCPlayer attacker, UHCPlayer target) {
        // Vérifications de base
        if (attacker.equals(target)) return false;
        if (!target.isPlaying()) return false;

        // Vérifier les restrictions de coéquipiers
        if (getConfig().getBoolean("restrictions.no_teammates", true)) {
            if (attacker.getTeam().isPresent() && target.getTeam().isPresent()) {
                if (attacker.getTeam().get().equals(target.getTeam().get())) {
                    return false;
                }
            }
        }

        // Vérifier l'immunité du rôle
        DeathNoteRole targetRole = getRoleByUHCPlayer(target);
        return targetRole == null || targetRole.canBeTargetedByDeathNote();
    }

    /**
     * Retourne tous les traîtres vivants de toutes les équipes
     */
    public List<UHCPlayer> getAllAliveTraitors() {
        List<UHCPlayer> traitors = new ArrayList<>();

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            if (player.isPlaying() && !deadPlayers.contains(player)) {
                DeathNoteRole role = getRoleByUHCPlayer(player);
                if (role != null && role.isKiraTeam()) {
                    traitors.add(player);
                }
            }
        }

        return traitors;
    }

    /**
     * Classe pour stocker les statistiques d'une équipe
     */
    public static class TeamStats {
        private final UHCTeam team;
        private final List<UHCPlayer> gentils;
        private final List<UHCPlayer> traitors;
        private final List<UHCPlayer> unknown;

        public TeamStats(UHCTeam team, List<UHCPlayer> gentils, List<UHCPlayer> traitors, List<UHCPlayer> unknown) {
            this.team = team;
            this.gentils = gentils;
            this.traitors = traitors;
            this.unknown = unknown;
        }

        public UHCTeam getTeam() {
            return team;
        }

        public List<UHCPlayer> getGentils() {
            return gentils;
        }

        public List<UHCPlayer> getTraitors() {
            return traitors;
        }

        public List<UHCPlayer> getUnknown() {
            return unknown;
        }

        public int getTotalAlive() {
            return gentils.size() + traitors.size() + unknown.size();
        }

        public boolean hasTraitors() {
            return !traitors.isEmpty();
        }

        public boolean hasGentils() {
            return !gentils.isEmpty();
        }
    }
}