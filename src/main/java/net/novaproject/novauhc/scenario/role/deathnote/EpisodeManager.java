package net.novaproject.novauhc.scenario.role.deathnote;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.role.deathnote.roles.DeathNoteRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestionnaire du système d'épisodes pour Death Note UHC
 * Gère la progression des épisodes, la régénération de vie et les annonces
 *
 * @author NovaProject
 * @version 1.0
 */
public class EpisodeManager {

    private final DeathNote scenario;
    // Gestion de la régénération de vie
    private final Map<UUID, Integer> playerDamageFromDeathNote = new HashMap<>();
    // Gestion des épisodes
    private int currentEpisode = 1;
    private int episodeDuration = 1200; // 20 minutes par défaut (en secondes)
    private int timeInCurrentEpisode = 0;
    private int heartRegenPerEpisode = 1; // 1 cœur par épisode par défaut

    // Task pour le système d'épisodes
    private BukkitTask episodeTask;

    public EpisodeManager(DeathNote scenario) {
        this.scenario = scenario;
        loadConfiguration();
    }

    /**
     * Charge la configuration depuis le fichier YAML
     */
    private void loadConfiguration() {
        if (scenario.getConfig() != null) {
            episodeDuration = scenario.getConfig().getInt("episodes.duration", 1200);
            heartRegenPerEpisode = scenario.getConfig().getInt("episodes.heart_regen", 1);
        }
    }

    /**
     * Démarre le système d'épisodes
     */
    public void startEpisodeSystem() {
        announceEpisode(currentEpisode);

        episodeTask = new BukkitRunnable() {
            @Override
            public void run() {
                timeInCurrentEpisode++;

                // Vérifier si l'épisode est terminé
                if (timeInCurrentEpisode >= episodeDuration) {
                    nextEpisode();
                }

                // Annonces de temps restant
                int timeLeft = episodeDuration - timeInCurrentEpisode;
                if (timeLeft == 300) { // 5 minutes restantes
                    announceTimeLeft(5);
                } else if (timeLeft == 60) { // 1 minute restante
                    announceTimeLeft(1);
                } else if (timeLeft == 10) { // 10 secondes restantes
                    announceTimeLeft(0, 10);
                }
            }
        }.runTaskTimer(Main.get(), 20L, 20L); // Chaque seconde
    }

    /**
     * Passe à l'épisode suivant
     */
    private void nextEpisode() {
        currentEpisode++;
        timeInCurrentEpisode = 0;

        // Régénération de vie pour les joueurs affectés par Death Note
        regenerateHealthForAffectedPlayers();

        // Notifier tous les rôles du nouvel épisode
        notifyRolesOfNewEpisode();

        // Annoncer le nouvel épisode
        announceEpisode(currentEpisode);
    }

    /**
     * Régénère la vie des joueurs affectés par Death Note
     */
    private void regenerateHealthForAffectedPlayers() {
        for (Map.Entry<UUID, Integer> entry : playerDamageFromDeathNote.entrySet()) {
            Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                int damageToHeal = Math.min(entry.getValue(), heartRegenPerEpisode * 2); // 1 cœur = 2 HP

                // Régénérer la vie
                double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + damageToHeal);
                player.setHealth(newHealth);

                // Mettre à jour les dégâts restants
                int remainingDamage = entry.getValue() - damageToHeal;
                if (remainingDamage <= 0) {
                    playerDamageFromDeathNote.remove(entry.getKey());
                } else {
                    playerDamageFromDeathNote.put(entry.getKey(), remainingDamage);
                }

                // Message au joueur
                player.sendMessage("§a[Épisode " + currentEpisode + "] §fVous récupérez §c" +
                        (damageToHeal / 2) + " cœur(s) §f!");
            }
        }
    }

    /**
     * Notifie tous les rôles du nouvel épisode
     */
    private void notifyRolesOfNewEpisode() {
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            DeathNoteRole role = scenario.getRoleByUHCPlayer(player);
            if (role != null) {
                role.onEpisodeStart(player, currentEpisode);
            }
        }
    }

    /**
     * Annonce le début d'un épisode
     */
    private void announceEpisode(int episode) {
        for (UHCPlayer player : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            Player p = player.getPlayer();
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            p.sendMessage("§3§l✦ ÉPISODE " + episode + " ✦");
            p.sendMessage("§8§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

            // Informations spécifiques à l'épisode
            if (episode == 1) {
                p.sendMessage("§fLe Death Note UHC commence !");
                p.sendMessage("§fDurée de l'épisode : §e" + (episodeDuration / 60) + " minutes");
            } else {
                p.sendMessage("§fNouvel épisode ! Les pouvoirs Death Note sont réinitialisés.");
                p.sendMessage("§fRégénération de vie pour les victimes de Death Note.");
            }
        }
    }

    /**
     * Annonce le temps restant dans l'épisode
     */
    private void announceTimeLeft(int minutes) {
        String message = "§e[Épisode " + currentEpisode + "] §fTemps restant : §c" + minutes + " minute(s)";

        for (UHCPlayer player : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            player.getPlayer().sendMessage(message);
        }
    }

    /**
     * Annonce le temps restant en secondes
     */
    private void announceTimeLeft(int minutes, int seconds) {
        String message = "§e[Épisode " + currentEpisode + "] §fTemps restant : §c" + seconds + " secondes";

        for (UHCPlayer player : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            player.getPlayer().sendMessage(message);
        }
    }

    /**
     * Enregistre les dégâts causés par Death Note pour la régénération
     */
    public void recordDeathNoteDamage(UHCPlayer player, int damage) {
        UUID playerId = player.getPlayer().getUniqueId();
        int currentDamage = playerDamageFromDeathNote.getOrDefault(playerId, 0);
        playerDamageFromDeathNote.put(playerId, currentDamage + damage);
    }

    /**
     * Appelé chaque seconde
     */
    public void onSecond() {
        // Le système d'épisodes se gère via sa propre BukkitRunnable
    }

    /**
     * Arrête le système d'épisodes
     */
    public void stopEpisodeSystem() {
        if (episodeTask != null) {
            episodeTask.cancel();
            episodeTask = null;
        }
    }

    /**
     * Force le passage à l'épisode suivant
     */
    public void forceNextEpisode() {
        nextEpisode();
    }

    /**
     * Retourne le temps restant dans l'épisode actuel en secondes
     */
    public int getTimeLeftInEpisode() {
        return episodeDuration - timeInCurrentEpisode;
    }

    /**
     * Retourne le temps écoulé dans l'épisode actuel en secondes
     */
    public int getTimeInCurrentEpisode() {
        return timeInCurrentEpisode;
    }

    /**
     * Retourne le pourcentage de progression dans l'épisode actuel
     */
    public double getEpisodeProgress() {
        return (double) timeInCurrentEpisode / episodeDuration * 100;
    }

    /**
     * Vérifie si c'est le premier épisode
     */
    public boolean isFirstEpisode() {
        return currentEpisode == 1;
    }

    /**
     * Retourne les informations sur l'épisode actuel
     */
    public String getEpisodeInfo() {
        int minutesLeft = getTimeLeftInEpisode() / 60;
        int secondsLeft = getTimeLeftInEpisode() % 60;

        return "§eÉpisode " + currentEpisode + " §7- §fTemps restant : §e" +
                minutesLeft + "m " + secondsLeft + "s";
    }

    // Getters
    public int getCurrentEpisode() {
        return currentEpisode;
    }

    /**
     * Définit l'épisode actuel (pour les tests ou la configuration)
     */
    public void setCurrentEpisode(int episode) {
        this.currentEpisode = episode;
        announceEpisode(episode);
    }

    public int getEpisodeDuration() {
        return episodeDuration;
    }

    public int getHeartRegenPerEpisode() {
        return heartRegenPerEpisode;
    }

    public Map<UUID, Integer> getPlayerDamageFromDeathNote() {
        return new HashMap<>(playerDamageFromDeathNote);
    }
}
