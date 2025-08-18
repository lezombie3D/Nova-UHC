package net.novaproject.novauhc.scenario.special.legend.core;

/**
 * Classe utilitaire générique pour gérer les cooldowns.
 * Fournit des méthodes pour vérifier l'état, définir la durée et obtenir le temps restant.
 *
 * @author NovaProject
 * @version 2.0
 */
public class Cooldown {

    private final long startTime;
    private final long durationMs;
    private final long endTime;

    /**
     * Crée un nouveau cooldown avec la durée spécifiée
     *
     * @param durationMs La durée du cooldown en millisecondes
     */
    public Cooldown(long durationMs) {
        if (durationMs < 0) {
            throw new IllegalArgumentException("La durée du cooldown ne peut pas être négative");
        }

        this.startTime = System.currentTimeMillis();
        this.durationMs = durationMs;
        this.endTime = startTime + durationMs;
    }

    /**
     * Crée un cooldown avec une durée en secondes
     *
     * @param durationSeconds La durée en secondes
     * @return Un nouveau cooldown
     */
    public static Cooldown ofSeconds(long durationSeconds) {
        return new Cooldown(durationSeconds * 1000L);
    }

    /**
     * Crée un cooldown avec une durée en minutes
     *
     * @param durationMinutes La durée en minutes
     * @return Un nouveau cooldown
     */
    public static Cooldown ofMinutes(long durationMinutes) {
        return new Cooldown(durationMinutes * 60L * 1000L);
    }

    /**
     * Crée un cooldown déjà expiré (prêt immédiatement)
     *
     * @return Un cooldown expiré
     */
    public static Cooldown expired() {
        return new Cooldown(0);
    }

    /**
     * Vérifie si le cooldown est terminé (prêt)
     *
     * @return true si le cooldown est terminé
     */
    public boolean isReady() {
        return System.currentTimeMillis() >= endTime;
    }

    /**
     * Vérifie si le cooldown est encore actif
     *
     * @return true si le cooldown est actif
     */
    public boolean isActive() {
        return !isReady();
    }

    /**
     * Récupère le temps restant en millisecondes
     *
     * @return Le temps restant (0 si terminé)
     */
    public long getRemainingMs() {
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Récupère le temps restant en secondes
     *
     * @return Le temps restant en secondes (0 si terminé)
     */
    public long getRemaining() {
        return getRemainingMs() / 1000L;
    }

    /**
     * Récupère la durée totale du cooldown en millisecondes
     *
     * @return La durée totale
     */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Récupère la durée totale du cooldown en secondes
     *
     * @return La durée totale en secondes
     */
    public long getDurationSeconds() {
        return durationMs / 1000L;
    }

    /**
     * Formate le temps restant en format lisible (mm:ss ou ss)
     *
     * @return Le temps formaté
     */
    public String getFormattedRemaining() {
        long seconds = getRemaining();

        if (seconds <= 0) {
            return "Prêt";
        }

        if (seconds < 60) {
            return seconds + "s";
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        if (remainingSeconds == 0) {
            return minutes + "m";
        }

        return String.format("%dm%02ds", minutes, remainingSeconds);
    }

    @Override
    public String toString() {
        return String.format("Cooldown{remaining=%s}", getFormattedRemaining());
    }
}
