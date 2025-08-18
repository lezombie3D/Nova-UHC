package net.novaproject.novauhc.scenario.special.legend.core;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

/**
 * Classe abstraite de base pour toutes les légendes UHC.
 * Définit la structure commune et les méthodes que chaque légende doit implémenter.
 *
 * @author NovaProject
 * @version 2.0
 */
public abstract class LegendClass {

    protected final int id;
    protected final String name;
    protected final String description;
    protected final Material iconMaterial;

    /**
     * Constructeur de base pour une légende
     *
     * @param id           ID unique de la légende
     * @param name         Nom de la légende
     * @param description  Description des capacités
     * @param iconMaterial Matériau pour l'icône dans l'interface
     */
    protected LegendClass(int id, String name, String description, Material iconMaterial) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconMaterial = iconMaterial;
    }

    // === GETTERS ===

    public final int getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final Material getIconMaterial() {
        return iconMaterial;
    }

    /**
     * Crée l'item d'icône pour l'interface de sélection
     */
    public ItemStack getIcon() {
        return new ItemCreator(iconMaterial)
                .setName("§6" + name)
                .setLores(Collections.singletonList("§7" + description))
                .getItemstack();
    }

    // === MÉTHODES ABSTRAITES (À IMPLÉMENTER) ===

    /**
     * Appelée quand un joueur choisit cette légende
     *
     * @param player    Le joueur Bukkit
     * @param uhcPlayer Le joueur UHC
     */
    public abstract void onChoose(Player player, UHCPlayer uhcPlayer);

    /**
     * Appelée quand le joueur active son pouvoir
     *
     * @param player    Le joueur Bukkit
     * @param uhcPlayer Le joueur UHC
     * @return true si le pouvoir a été activé avec succès
     */
    public abstract boolean onPower(Player player, UHCPlayer uhcPlayer);

    /**
     * Appelée chaque seconde pour les effets passifs
     *
     * @param player    Le joueur Bukkit
     * @param uhcPlayer Le joueur UHC
     */
    public abstract void onTick(Player player, UHCPlayer uhcPlayer);

    /**
     * Appelée quand le joueur meurt
     *
     * @param player    Le joueur Bukkit qui meurt
     * @param uhcPlayer Le joueur UHC qui meurt
     * @param killer    Le tueur (peut être null)
     */
    public abstract void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer);

    // === MÉTHODES AVEC IMPLÉMENTATION PAR DÉFAUT ===

    /**
     * Appelée quand le joueur frappe un autre joueur
     * Implémentation par défaut : ne fait rien
     */
    public void onHit(Player attacker, Player victim, UHCPlayer uhcAttacker, UHCPlayer uhcVictim) {
        // Implémentation par défaut vide
    }

    /**
     * Appelée quand le joueur subit des dégâts
     * Implémentation par défaut : ne fait rien
     */
    public void onDamage(Player player, UHCPlayer uhcPlayer, double damage) {
        // Implémentation par défaut vide
    }

    /**
     * Appelée quand le joueur mange un item
     * Implémentation par défaut : ne fait rien
     */
    public void onConsume(Player player, UHCPlayer uhcPlayer, ItemStack item) {
    }


    public boolean hasPower() {
        return true;
    }

    public boolean canBeChosen(UHCPlayer player) {
        return true;
    }


    public void cleanup(Player player, UHCPlayer uhcPlayer) {
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        LegendClass that = (LegendClass) obj;
        return id == that.id;
    }

    @Override
    public final int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public final String toString() {
        return String.format("LegendClass{id=%d, name='%s'}", id, name);
    }

    public void onKill(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {

    }
}
