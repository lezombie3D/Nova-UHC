package net.novaproject.novauhc.utils;

import net.novaproject.novauhc.CommonString;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils {

    /**
     * Envoie un message d'erreur pour un nombre invalide
     */
    public static void sendInvalidNumber(Player player) {
        CommonString.ERROR_INVALID_NUMBER.send(player);
    }

    /**
     * Envoie un message d'erreur pour un nombre trop bas
     */
    public static void sendNumberTooLow(Player player, int min) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%min%", min);
        CommonString.ERROR_NUMBER_TOO_LOW.send(player, placeholders);
    }

    /**
     * Envoie un message d'erreur pour un nombre trop élevé
     */
    public static void sendNumberTooHigh(Player player, int max) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%max%", max);
        CommonString.ERROR_NUMBER_TOO_HIGH.send(player, placeholders);
    }

    /**
     * Envoie un message d'erreur pour un argument invalide
     */
    public static void sendInvalidArgument(Player player, String argument) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%argument%", argument);
        CommonString.ERROR_INVALID_ARGUMENT.send(player, placeholders);
    }

    /**
     * Envoie un message d'erreur pour un joueur hors ligne
     */
    public static void sendPlayerOffline(Player player, String playerName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%player%", playerName);
        CommonString.ERROR_PLAYER_OFFLINE.send(player, placeholders);
    }

    /**
     * Envoie un message d'erreur pour un joueur pas en jeu
     */
    public static void sendPlayerNotInGame(Player player, String playerName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%player%", playerName);
        CommonString.ERROR_PLAYER_NOT_IN_GAME.send(player, placeholders);
    }

    /**
     * Envoie un message d'erreur pour un monde introuvable
     */
    public static void sendWorldNotFound(Player player, String worldName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%world%", worldName);
        CommonString.ERROR_WORLD_NOT_FOUND.send(player, placeholders);
    }

    /**
     * Envoie un message d'erreur pour un fichier introuvable
     */
    public static void sendFileNotFound(Player player, String fileName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%file%", fileName);
        CommonString.ERROR_FILE_NOT_FOUND.send(player, placeholders);
    }

    /**
     * Envoie un message d'erreur pour un cooldown actif
     */
    public static void sendCooldownActive(Player player, String time) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%time%", time);
        CommonString.ERROR_COOLDOWN_ACTIVE.send(player, placeholders);
    }

    /**
     * Envoie un message de succès pour un joueur ajouté
     */
    public static void sendPlayerAdded(Player player, String playerName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%player%", playerName);
        CommonString.SUCCESS_PLAYER_ADDED.send(player, placeholders);
    }

    /**
     * Envoie un message de succès pour un joueur retiré
     */
    public static void sendPlayerRemoved(Player player, String playerName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%player%", playerName);
        CommonString.SUCCESS_PLAYER_REMOVED.send(player, placeholders);
    }

    /**
     * Envoie un message de succès pour un monde chargé
     */
    public static void sendWorldLoaded(Player player, String worldName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%world%", worldName);
        CommonString.SUCCESS_WORLD_LOADED.send(player, placeholders);
    }

    /**
     * Envoie un message d'information de chargement
     */
    public static void sendLoading(Player player) {
        CommonString.INFO_LOADING.send(player);
    }

    /**
     * Envoie un message d'information de traitement
     */
    public static void sendProcessing(Player player) {
        CommonString.INFO_PROCESSING.send(player);
    }

    /**
     * Envoie un message d'information pour patienter
     */
    public static void sendPleaseWait(Player player) {
        CommonString.INFO_PLEASE_WAIT.send(player);
    }

    /**
     * Envoie un message d'information d'opération annulée
     */
    public static void sendOperationCancelled(Player player) {
        CommonString.INFO_OPERATION_CANCELLED.send(player);
    }

    /**
     * Envoie un message d'information d'aucune modification
     */
    public static void sendNoChangesMade(Player player) {
        CommonString.INFO_NO_CHANGES_MADE.send(player);
    }

    /**
     * Envoie un message de succès générique
     */
    public static void sendOperationCompleted(Player player) {
        CommonString.SUCCESS_OPERATION_COMPLETED.send(player);
    }

    /**
     * Envoie un message d'erreur de permission refusée
     */
    public static void sendPermissionDenied(Player player) {
        CommonString.ERROR_PERMISSION_DENIED.send(player);
    }

    /**
     * Envoie un message d'erreur d'argument manquant
     */
    public static void sendMissingArgument(Player player) {
        CommonString.ERROR_MISSING_ARGUMENT.send(player);
    }

    /**
     * Envoie un message d'erreur de commande désactivée
     */
    public static void sendCommandDisabled(Player player) {
        CommonString.ERROR_COMMAND_DISABLED.send(player);
    }

    /**
     * Envoie un message d'erreur de position invalide
     */
    public static void sendLocationInvalid(Player player) {
        CommonString.ERROR_LOCATION_INVALID.send(player);
    }

    /**
     * Envoie un message d'erreur d'opération déjà en cours
     */
    public static void sendAlreadyInProgress(Player player) {
        CommonString.ERROR_ALREADY_IN_PROGRESS.send(player);
    }

    /**
     * Envoie un message d'erreur de partie non démarrée
     */
    public static void sendNotStarted(Player player) {
        CommonString.ERROR_NOT_STARTED.send(player);
    }

    /**
     * Envoie un message de succès de paramètres sauvegardés
     */
    public static void sendSettingsSaved(Player player) {
        CommonString.SUCCESS_SETTINGS_SAVED.send(player);
    }

    /**
     * Envoie un message de succès de téléportation
     */
    public static void sendTeleportCompleted(Player player) {
        CommonString.SUCCESS_TELEPORT_COMPLETED.send(player);
    }
}
