package net.novaproject.novauhc.scenario.role.scenario.deathnote;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.role.scenario.deathnote.roles.DeathNoteRole;
import net.novaproject.novauhc.scenario.role.scenario.deathnote.roles.EnqueteurRole;
import net.novaproject.novauhc.scenario.role.scenario.deathnote.roles.KiraRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire principal du système Death Note
 * Gère les timers de mort, les pouvoirs et les interactions Death Note
 *
 * @author NovaProject
 * @version 1.0
 */
public class DeathNoteManager {

    private final DeathNote scenario;

    // Gestion des timers Death Note
    private final Map<UUID, DeathNoteTimer> activeTimers = new HashMap<>();

    // Gestion des cooldowns
    private final Map<UUID, Long> deathNoteCooldowns = new HashMap<>();

    // Configuration
    private int deathTimer = 40; // 40 secondes par défaut
    private int cooldownTime = 600; // 10 minutes par défaut

    public DeathNoteManager(DeathNote scenario) {
        this.scenario = scenario;
        loadConfiguration();
    }

    /**
     * Charge la configuration depuis le fichier YAML
     */
    private void loadConfiguration() {
        if (scenario.getConfig() != null) {
            deathTimer = scenario.getConfig().getInt("death_note.damage_timer", 40);
            cooldownTime = scenario.getConfig().getInt("death_note.cooldown", 600);
        }
    }

    /**
     * Utilise le Death Note sur un joueur cible
     *
     * @param kira       Le joueur Kira qui utilise le Death Note
     * @param targetName Le nom du joueur cible
     * @param episode    L'épisode actuel
     * @return true si l'utilisation a réussi, false sinon
     */
    public boolean useDeathNote(UHCPlayer kira, String targetName, int episode) {
        // Vérifier que le joueur peut utiliser le Death Note
        DeathNoteRole kiraRole = scenario.getRoleByUHCPlayer(kira);
        if (!(kiraRole instanceof KiraRole kiraRoleInstance)) {
            kira.getPlayer().sendMessage("§c[Death Note] §fVous n'êtes pas Kira !");
            return false;
        }

        // Vérifier le cooldown
        if (isOnCooldown(kira)) {
            long remainingTime = getRemainingCooldown(kira);
            kira.getPlayer().sendMessage("§c[Death Note] §fCooldown actif ! Temps restant : §e" +
                    (remainingTime / 60) + "m " + (remainingTime % 60) + "s");
            return false;
        }

        // Vérifier si déjà utilisé cet épisode
        if (kiraRoleInstance.hasUsedDeathNoteThisEpisode()) {
            kira.getPlayer().sendMessage("§c[Death Note] §fVous avez déjà utilisé votre Death Note cet épisode !");
            return false;
        }

        // Trouver le joueur cible
        UHCPlayer target = findPlayerByName(targetName);
        if (target == null) {
            kira.getPlayer().sendMessage("§c[Death Note] §fJoueur introuvable : §e" + targetName);
            return false;
        }

        // Vérifier que la cible est valide
        if (!isValidTarget(kira, target)) {
            return false;
        }

        // Calculer les dégâts selon l'épisode
        int damage = calculateDamage(episode);

        // Créer le timer de mort
        DeathNoteTimer timer = new DeathNoteTimer(kira, target, damage, deathTimer);
        activeTimers.put(target.getPlayer().getUniqueId(), timer);

        // Démarrer le timer
        timer.start();

        // Marquer comme utilisé
        kiraRoleInstance.useDamagePower(target, episode);

        // Appliquer le cooldown
        deathNoteCooldowns.put(kira.getPlayer().getUniqueId(), System.currentTimeMillis() + (cooldownTime * 1000L));

        // Messages
        kira.getPlayer().sendMessage("§c[Death Note] §fVous avez écrit le nom de §e" + target.getPlayer().getName() +
                " §fdans le Death Note !");
        kira.getPlayer().sendMessage("§c[Death Note] §fIl mourra dans §c" + deathTimer + " secondes §f!");

        // Notifier les Enquêteurs
        notifyInvestigators(target, deathTimer);

        return true;
    }

    /**
     * Vérifie si une cible est valide pour le Death Note
     */
    private boolean isValidTarget(UHCPlayer kira, UHCPlayer target) {
        // Ne peut pas se cibler soi-même
        if (kira.equals(target)) {
            kira.getPlayer().sendMessage("§c[Death Note] §fVous ne pouvez pas vous cibler vous-même !");
            return false;
        }

        // Vérifier si le joueur est vivant
        if (scenario.getDeadPlayers().contains(target)) {
            kira.getPlayer().sendMessage("§c[Death Note] §fCe joueur est déjà mort !");
            return false;
        }

        // Vérifier le rôle de la cible
        DeathNoteRole targetRole = scenario.getRoleByUHCPlayer(target);
        if (targetRole != null && !targetRole.canBeTargetedByDeathNote()) {
            kira.getPlayer().sendMessage("§c[Death Note] §fCe joueur est immunisé au Death Note !");
            return false;
        }

        // Vérifier si c'est un coéquipier (selon la configuration)
        if (scenario.getConfig().getBoolean("restrictions.no_teammates", true)) {
            if (kira.getTeam().isPresent() && target.getTeam().isPresent()) {
                if (kira.getTeam().get().equals(target.getTeam().get())) {
                    kira.getPlayer().sendMessage("§c[Death Note] §fVous ne pouvez pas cibler un coéquipier !");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Calcule les dégâts selon l'épisode
     */
    private int calculateDamage(int episode) {
        switch (episode) {
            case 2:
                return 5; // 5 cœurs
            case 3:
                return 4; // 4 cœurs
            default:
                return 3; // 3 cœurs (épisode 4+)
        }
    }

    /**
     * Notifie les Enquêteurs du timer Death Note
     */
    private void notifyInvestigators(UHCPlayer target, int seconds) {
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            DeathNoteRole role = scenario.getRoleByUHCPlayer(player);
            if (role instanceof EnqueteurRole enqueteur) {
                enqueteur.showDeathNoteTimer(player, target, seconds);
            }
        }
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
     * Vérifie si un joueur est en cooldown
     */
    public boolean isOnCooldown(UHCPlayer player) {
        Long cooldownEnd = deathNoteCooldowns.get(player.getPlayer().getUniqueId());
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }

    /**
     * Retourne le temps de cooldown restant en secondes
     */
    public long getRemainingCooldown(UHCPlayer player) {
        Long cooldownEnd = deathNoteCooldowns.get(player.getPlayer().getUniqueId());
        if (cooldownEnd == null) return 0;

        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * Gère les commandes Death Note
     */
    public void handleDeathNoteCommand(UHCPlayer player, String[] args) {
        if (args.length == 0) {
            player.getPlayer().sendMessage("§c[Death Note] §fCommandes disponibles :");
            player.getPlayer().sendMessage("§c[Death Note] §f/ff dnote claim §7- Récupérer les pouvoirs");
            player.getPlayer().sendMessage("§c[Death Note] §f/ff dnote use <nom> §7- Utiliser le Death Note");
            player.getPlayer().sendMessage("§c[Death Note] §f/ff dnote reveal §7- Révéler son identité (Kira)");
            return;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "claim":
                // TODO: Implémenter la récupération des pouvoirs
                player.getPlayer().sendMessage("§c[Death Note] §fRécupération des pouvoirs - À implémenter");
                break;

            case "use":
                if (args.length < 2) {
                    player.getPlayer().sendMessage("§c[Death Note] §fUsage : /ff dnote use <nom>");
                    return;
                }

                String targetName = args[1];
                int currentEpisode = scenario.getEpisodeManager().getCurrentEpisode();
                useDeathNote(player, targetName, currentEpisode);
                break;

            case "reveal":
                // TODO: Implémenter la révélation d'identité
                player.getPlayer().sendMessage("§c[Death Note] §fRévélation d'identité - À implémenter");
                break;

            default:
                player.getPlayer().sendMessage("§c[Death Note] §fCommande inconnue : " + subCommand);
                break;
        }
    }

    /**
     * Appelé chaque seconde pour mettre à jour les timers
     */
    public void onSecond() {
        // Les timers se gèrent eux-mêmes via BukkitRunnable
    }

    /**
     * Appelé quand un joueur meurt
     */
    public void onPlayerDeath(UHCPlayer deadPlayer, UHCPlayer killer) {
        // Annuler le timer Death Note si le joueur meurt avant
        UUID playerId = deadPlayer.getPlayer().getUniqueId();
        DeathNoteTimer timer = activeTimers.get(playerId);
        if (timer != null) {
            timer.cancel();
            activeTimers.remove(playerId);

            // Notifier que le Death Note a été annulé
            timer.getKira().getPlayer().sendMessage("§c[Death Note] §f" + deadPlayer.getPlayer().getName() +
                    " est mort avant l'effet du Death Note !");
        }
    }

    /**
     * Classe interne pour gérer les timers Death Note
     */
    private class DeathNoteTimer {
        private final UHCPlayer kira;
        private final UHCPlayer target;
        private final int damage;
        private int secondsLeft;
        private BukkitTask task;

        public DeathNoteTimer(UHCPlayer kira, UHCPlayer target, int damage, int seconds) {
            this.kira = kira;
            this.target = target;
            this.damage = damage;
            this.secondsLeft = seconds;
        }

        public void start() {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (secondsLeft <= 0) {
                        // Tuer le joueur
                        executeDeathNote();
                        cancel();
                        activeTimers.remove(target.getPlayer().getUniqueId());
                        return;
                    }

                    // Messages d'avertissement
                    if (secondsLeft == 30 || secondsLeft == 10 || secondsLeft <= 5) {
                        target.getPlayer().sendMessage("§c[Death Note] §fVous mourrez dans §c" + secondsLeft + " secondes §f!");

                        // Notifier les Enquêteurs
                        notifyInvestigators(target, secondsLeft);
                    }

                    secondsLeft--;
                }
            }.runTaskTimer(Main.get(), 0L, 20L); // Chaque seconde
        }

        private void executeDeathNote() {
            Player targetPlayer = target.getPlayer();

            // Appliquer les dégâts
            double newHealth = Math.max(0, targetPlayer.getHealth() - (damage * 2)); // damage * 2 car 1 cœur = 2 HP
            targetPlayer.setHealth(newHealth);

            // Message de mort
            targetPlayer.sendMessage("§c[Death Note] §fVous êtes mort à cause du Death Note de " + kira.getPlayer().getName() + " !");

            // TODO: Message de mort personnalisé dans le chat
        }

        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }

        public UHCPlayer getKira() {
            return kira;
        }
    }
}
