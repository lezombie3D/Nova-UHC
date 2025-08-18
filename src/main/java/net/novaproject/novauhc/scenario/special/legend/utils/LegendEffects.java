package net.novaproject.novauhc.scenario.special.legend.utils;

import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.special.legend.LegendLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Classe utilitaire pour gérer tous les effets visuels et de gameplay des légendes.
 * Sépare la logique métier de la logique visuelle.
 *
 * @author NovaProject
 * @version 2.0
 */
public class LegendEffects {

    // === EFFETS DE POTION ===

    /**
     * Applique un effet de potion en retirant d'abord l'ancien
     *
     * @param player Le joueur
     * @param effect L'effet à appliquer
     */
    public static void applyEffect(Player player, PotionEffect effect) {
        player.removePotionEffect(effect.getType());
        player.addPotionEffect(effect);
    }

    /**
     * Applique plusieurs effets de potion
     *
     * @param player  Le joueur
     * @param effects Les effets à appliquer
     */
    public static void applyEffects(Player player, PotionEffect... effects) {
        for (PotionEffect effect : effects) {
            applyEffect(player, effect);
        }
    }

    /**
     * Applique plusieurs effets de potion
     *
     * @param player  Le joueur
     * @param effects Les effets à appliquer
     */
    public static void applyEffects(Player player, Collection<PotionEffect> effects) {
        for (PotionEffect effect : effects) {
            applyEffect(player, effect);
        }
    }

    /**
     * Retire un type d'effet de potion
     *
     * @param player     Le joueur
     * @param effectType Le type d'effet à retirer
     */
    public static void removeEffect(Player player, PotionEffectType effectType) {
        player.removePotionEffect(effectType);
    }

    /**
     * Vérifie si un joueur a un effet spécifique
     *
     * @param player     Le joueur
     * @param effectType Le type d'effet
     * @return true si le joueur a l'effet
     */
    public static boolean hasEffect(Player player, PotionEffectType effectType) {
        return player.hasPotionEffect(effectType);
    }

    // === EFFETS DE ZONE (ÉQUIPE) ===

    /**
     * Applique un effet à tous les membres d'une équipe dans un rayon donné
     *
     * @param center      Le joueur au centre
     * @param uhcPlayer   Le joueur UHC
     * @param effect      L'effet à appliquer
     * @param radius      Le rayon d'effet
     * @param includeSelf Inclure le joueur central
     */
    public static void applyTeamEffect(Player center, UHCPlayer uhcPlayer, PotionEffect effect, double radius, boolean includeSelf) {
        Optional<UHCTeam> teamOpt = uhcPlayer.getTeam();
        if (!teamOpt.isPresent()) return;

        UHCTeam team = teamOpt.get();
        Location centerLoc = center.getLocation();

        for (UHCPlayer teammate : team.getPlayers()) {
            if (!includeSelf && teammate.equals(uhcPlayer)) continue;

            Player teammatePlayer = teammate.getPlayer();
            if (teammatePlayer == null || !teammatePlayer.isOnline()) continue;

            if (teammatePlayer.getLocation().distance(centerLoc) <= radius) {
                applyEffect(teammatePlayer, effect);
            }
        }
    }

    /**
     * Applique un effet à tous les membres d'une équipe dans un rayon donné (incluant le joueur central)
     *
     * @param center    Le joueur au centre
     * @param uhcPlayer Le joueur UHC
     * @param effect    L'effet à appliquer
     * @param radius    Le rayon d'effet
     */
    public static void applyTeamEffect(Player center, UHCPlayer uhcPlayer, PotionEffect effect, double radius) {
        applyTeamEffect(center, uhcPlayer, effect, radius, true);
    }

    // === EFFETS VISUELS ===

    /**
     * Fait apparaître des particules en cercle autour d'un joueur
     *
     * @param center        Le centre du cercle
     * @param location      La location
     * @param effect        L'effet de particule
     * @param radius        Le rayon du cercle
     * @param particleCount Le nombre de particules
     */
    public static void spawnParticleCircle(Player center, Location location, Effect effect, double radius, int particleCount) {
        World world = location.getWorld();
        if (world == null) return;

        double angleStep = 2 * Math.PI / particleCount;

        for (int i = 0; i < particleCount; i++) {
            double angle = i * angleStep;
            double x = location.getX() + radius * Math.cos(angle);
            double z = location.getZ() + radius * Math.sin(angle);

            Location particleLoc = new Location(world, x, location.getY(), z);
            world.playEffect(particleLoc, effect, 0);
        }
    }

    /**
     * Joue un son à un joueur
     *
     * @param player Le joueur
     * @param sound  Le son à jouer
     * @param volume Le volume (0.0 à 1.0)
     * @param pitch  La hauteur (0.5 à 2.0)
     */
    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Fait apparaître un éclair sur une location
     *
     * @param location La location
     * @param damage   Infliger des dégâts
     */
    public static void strikeLightning(Location location, boolean damage) {
        World world = location.getWorld();
        if (world == null) return;

        if (damage) {
            world.strikeLightning(location);
        } else {
            world.strikeLightningEffect(location);
        }
    }

    /**
     * Crée une explosion visuelle (sans dégâts)
     *
     * @param location La location
     * @param power    La puissance de l'explosion
     */
    public static void createExplosion(Location location, float power) {
        World world = location.getWorld();
        if (world == null) return;

        world.createExplosion(location, power, false);
    }

    // === MESSAGES ET NOTIFICATIONS ===

    /**
     * Envoie un message de pouvoir activé
     *
     * @param player     Le joueur
     * @param legendName Le nom de la légende
     */
    public static void sendPowerActivatedMessage(Player player, String legendName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%legend_name%", legendName);
        ScenarioLangManager.send(player, LegendLang.POWER_ACTIVATED, placeholders);
    }

    /**
     * Envoie un message de cooldown
     *
     * @param player        Le joueur
     * @param timeRemaining Le temps restant formaté
     */
    public static void sendCooldownMessage(Player player, String timeRemaining) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%time%", timeRemaining);
        ScenarioLangManager.send(player, LegendLang.POWER_COOLDOWN_REMAINING, placeholders);
    }

    /**
     * Envoie un message d'effet reçu
     *
     * @param player     Le joueur
     * @param effectName Le nom de l'effet
     */
    public static void sendEffectReceivedMessage(Player player, String effectName) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%effect%", effectName);
        ScenarioLangManager.send(player, LegendLang.EFFECT_RECEIVED, placeholders);
    }
}
