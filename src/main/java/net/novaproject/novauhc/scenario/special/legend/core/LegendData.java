package net.novaproject.novauhc.scenario.special.legend.core;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stocke toutes les données spécifiques à une légende pour un joueur donné.
 * Centralise les cooldowns, entités invoquées, marionnettes, etc.
 *
 * @author NovaProject
 * @version 2.0
 */
public class LegendData {

    private final UHCPlayer owner;
    private final LegendClass legendClass;
    private final long creationTime;

    // Système de cooldowns
    private final Map<String, Cooldown> cooldowns = new ConcurrentHashMap<>();

    // Données génériques (pour stockage flexible)
    private final Map<String, Object> customData = new ConcurrentHashMap<>();

    // Collections spécialisées pour les légendes complexes
    private final Set<UHCPlayer> puppets = ConcurrentHashMap.newKeySet();
    private final Set<Entity> summonedEntities = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> counters = new ConcurrentHashMap<>();

    /**
     * Constructeur des données de légende
     *
     * @param owner       Le propriétaire des données
     * @param legendClass La classe de légende
     */
    public LegendData(UHCPlayer owner, LegendClass legendClass) {
        this.owner = Objects.requireNonNull(owner, "Owner cannot be null");
        this.legendClass = Objects.requireNonNull(legendClass, "LegendClass cannot be null");
        this.creationTime = System.currentTimeMillis();
    }

    // === GETTERS DE BASE ===

    public UHCPlayer getOwner() {
        return owner;
    }

    public LegendClass getLegendClass() {
        return legendClass;
    }

    public long getCreationTime() {
        return creationTime;
    }

    // === GESTION DES COOLDOWNS ===

    /**
     * Récupère un cooldown par son nom
     *
     * @param name Le nom du cooldown
     * @return Le cooldown ou null s'il n'existe pas
     */
    public Cooldown getCooldown(String name) {
        return cooldowns.get(name);
    }

    /**
     * Crée ou met à jour un cooldown
     *
     * @param name       Le nom du cooldown
     * @param durationMs La durée en millisecondes
     * @return Le cooldown créé
     */
    public Cooldown setCooldown(String name, long durationMs) {
        Cooldown cooldown = new Cooldown(durationMs);
        cooldowns.put(name, cooldown);
        return cooldown;
    }

    /**
     * Vérifie si un cooldown est prêt (terminé ou inexistant)
     *
     * @param name Le nom du cooldown
     * @return true si le cooldown est prêt
     */
    public boolean isCooldownReady(String name) {
        Cooldown cooldown = cooldowns.get(name);
        return cooldown == null || cooldown.isReady();
    }

    /**
     * Récupère le temps restant d'un cooldown en secondes
     *
     * @param name Le nom du cooldown
     * @return Le temps restant en secondes (0 si prêt)
     */
    public long getCooldownRemaining(String name) {
        Cooldown cooldown = cooldowns.get(name);
        return cooldown != null ? cooldown.getRemaining() : 0;
    }

    /**
     * Supprime un cooldown
     *
     * @param name Le nom du cooldown
     */
    public void removeCooldown(String name) {
        cooldowns.remove(name);
    }

    // === DONNÉES PERSONNALISÉES ===

    /**
     * Stocke une donnée personnalisée
     *
     * @param key   La clé
     * @param value La valeur
     */
    public void setData(String key, Object value) {
        if (value == null) {
            customData.remove(key);
        } else {
            customData.put(key, value);
        }
    }

    /**
     * Récupère une donnée personnalisée
     *
     * @param key La clé
     * @return La valeur ou null
     */
    public Object getData(String key) {
        return customData.get(key);
    }

    /**
     * Récupère une donnée avec une valeur par défaut
     *
     * @param key          La clé
     * @param defaultValue La valeur par défaut
     * @return La valeur ou la valeur par défaut
     */
    @SuppressWarnings("unchecked")
    public <T> T getDataOrDefault(String key, T defaultValue) {
        Object value = customData.get(key);
        return value != null ? (T) value : defaultValue;
    }

    // === GESTION DES MARIONNETTES ===

    /**
     * Ajoute une marionnette
     *
     * @param puppet La marionnette à ajouter
     */
    public void addPuppet(UHCPlayer puppet) {
        puppets.add(puppet);
    }

    /**
     * Supprime une marionnette
     *
     * @param puppet La marionnette à supprimer
     */
    public void removePuppet(UHCPlayer puppet) {
        puppets.remove(puppet);
    }

    /**
     * Récupère toutes les marionnettes
     *
     * @return Un set non-modifiable des marionnettes
     */
    public Set<UHCPlayer> getPuppets() {
        return Collections.unmodifiableSet(puppets);
    }

    /**
     * Récupère le nombre de marionnettes
     *
     * @return Le nombre de marionnettes
     */
    public int getPuppetCount() {
        return puppets.size();
    }

    // === GESTION DES ENTITÉS INVOQUÉES ===

    /**
     * Ajoute une entité invoquée
     *
     * @param entity L'entité à ajouter
     */
    public void addSummonedEntity(Entity entity) {
        summonedEntities.add(entity);
    }

    /**
     * Supprime une entité invoquée
     *
     * @param entity L'entité à supprimer
     */
    public void removeSummonedEntity(Entity entity) {
        summonedEntities.remove(entity);
    }

    /**
     * Récupère toutes les entités invoquées
     *
     * @return Un set non-modifiable des entités
     */
    public Set<Entity> getSummonedEntities() {
        return Collections.unmodifiableSet(summonedEntities);
    }

    /**
     * Supprime toutes les entités invoquées
     */
    public void clearSummonedEntities() {
        summonedEntities.forEach(Entity::remove);
        summonedEntities.clear();
    }

    // === GESTION DES COMPTEURS ===

    /**
     * Incrémente un compteur
     *
     * @param name Le nom du compteur
     * @return La nouvelle valeur
     */
    public int incrementCounter(String name) {
        return counters.merge(name, 1, Integer::sum);
    }

    /**
     * Définit la valeur d'un compteur
     *
     * @param name  Le nom du compteur
     * @param value La valeur
     */
    public void setCounter(String name, int value) {
        counters.put(name, value);
    }

    /**
     * Récupère la valeur d'un compteur
     *
     * @param name Le nom du compteur
     * @return La valeur (0 si inexistant)
     */
    public int getCounter(String name) {
        return counters.getOrDefault(name, 0);
    }

    // === NETTOYAGE ===

    /**
     * Nettoie toutes les données (appelé à la déconnexion ou changement de légende)
     */
    public void cleanup() {
        cooldowns.clear();
        customData.clear();
        puppets.clear();
        clearSummonedEntities();
        counters.clear();
    }
}
