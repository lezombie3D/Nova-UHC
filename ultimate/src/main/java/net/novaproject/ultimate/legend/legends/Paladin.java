package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.ultimate.legend.Legend;
import net.novaproject.ultimate.legend.core.LegendClass;
import net.novaproject.ultimate.legend.core.LegendData;
import net.novaproject.ultimate.legend.utils.LegendEffects;
import net.novaproject.ultimate.legend.utils.LegendItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

/**
 * Exemple de nouvelle légende : Le Paladin
 * <p>
 * Capacités :
 * - Pouvoir : Bénédiction (Régénération II + Résistance I pendant 30 secondes) - Cooldown 8 minutes
 * - Passif : Résistance I permanente quand la vie est en dessous de 50%
 * - Spécial : Bonus de force quand il combat près d'alliés
 *
 * @author NovaProject
 * @version 2.0
 */
public class Paladin extends LegendClass {

    // Constantes de la légende
    private static final String POWER_COOLDOWN = "blessing";
    private static final long POWER_COOLDOWN_MS = 8 * 60 * 1000L; // 8 minutes
    private static final int POWER_DURATION = 30; // 30 secondes

    private static final double LOW_HEALTH_THRESHOLD = 10.0; // 50% de vie (10 cœurs)
    private static final double ALLY_BONUS_RADIUS = 8.0; // Rayon pour le bonus d'allié

    public Paladin() {
        super(
                18, // ID unique (après les 17 légendes existantes)
                "Paladin",
                "Guerrier saint avec bénédictions et résistance",
                Material.DIAMOND_SWORD
        );
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        // Récupérer les données du joueur
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        // Donner l'équipement de base du Paladin
        giveStartingEquipment(player);

        // Message de bienvenue
        player.sendMessage("§6[Paladin] §aVous êtes maintenant un Paladin !");
        player.sendMessage("§7Pouvoir : §eBénédiction §7(Régénération + Résistance)");
        player.sendMessage("§7Passif : §eRésistance à faible vie et bonus près des alliés");

        // Donner l'item de pouvoir
        player.getInventory().addItem(LegendItems.createPowerItem("Paladin"));
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        // Récupérer les données du joueur
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        // Vérifier le cooldown
        if (!data.isCooldownReady(POWER_COOLDOWN)) {
            long remaining = data.getCooldownRemaining(POWER_COOLDOWN);
            LegendEffects.sendCooldownMessage(player, formatTime(remaining));
            return false;
        }

        // Activer le pouvoir
        activateBlessing(player, uhcPlayer, data);

        // Démarrer le cooldown
        data.setCooldown(POWER_COOLDOWN, POWER_COOLDOWN_MS);

        return true;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Effet passif : Résistance à faible vie
        applyLowHealthResistance(player);

        // Effet passif : Bonus de force près des alliés
        applyAllyBonus(player, uhcPlayer);
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Effet spécial à la mort : explosion de lumière
        LegendEffects.createExplosion(player.getLocation(), 2.0f);
        LegendEffects.spawnParticleCircle(player, player.getLocation(),
                org.bukkit.Effect.ENDER_SIGNAL, 5.0, 30);

        // Message
        if (killer != null) {
            killer.getPlayer().sendMessage("§6Le Paladin " + player.getName() + " §6a été vaincu !");
        }
    }

    @Override
    public void onHit(Player attacker, Player victim, UHCPlayer uhcAttacker, UHCPlayer uhcVictim) {
        // 15% de chance d'infliger Weakness I pendant 5 secondes à la victime
        if (Math.random() < 0.15) {
            PotionEffect weakness = new PotionEffect(PotionEffectType.WEAKNESS, 20 * 5, 0);
            LegendEffects.applyEffect(victim, weakness);

            attacker.sendMessage("§6[Paladin] §eVotre attaque affaiblit votre ennemi !");
            victim.sendMessage("§c[Paladin] §eVous êtes affaibli par l'attaque du Paladin !");
        }
    }

    @Override
    public void cleanup(Player player, UHCPlayer uhcPlayer) {
        // Retirer tous les effets du Paladin
        LegendEffects.removeEffect(player, PotionEffectType.REGENERATION);
        LegendEffects.removeEffect(player, PotionEffectType.DAMAGE_RESISTANCE);
        LegendEffects.removeEffect(player, PotionEffectType.INCREASE_DAMAGE);
    }

    // === MÉTHODES PRIVÉES ===

    /**
     * Active la bénédiction du Paladin
     */
    private void activateBlessing(Player player, UHCPlayer uhcPlayer, LegendData data) {
        // Effets de la bénédiction
        PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 20 * POWER_DURATION, 1);
        PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * POWER_DURATION, 0);

        LegendEffects.applyEffects(player, regeneration, resistance);

        // Effets visuels et sonores
        LegendEffects.spawnParticleCircle(player, player.getLocation(),
                org.bukkit.Effect.HEART, 3.0, 50);
        LegendEffects.playSound(player, org.bukkit.Sound.LEVEL_UP, 1.0f, 1.5f);

        // Messages
        LegendEffects.sendPowerActivatedMessage(player, "Paladin");
        player.sendMessage("§6[Paladin] §aBénédiction activée ! Régénération et résistance pendant " + POWER_DURATION + " secondes.");

        // Effet de zone pour les alliés (bonus mineur)
        PotionEffect allyRegeneration = new PotionEffect(PotionEffectType.REGENERATION, 20 * 10, 0);
        LegendEffects.applyTeamEffect(player, uhcPlayer, allyRegeneration, 6.0, false);

        // Notifier les alliés
        if (uhcPlayer.getTeam().isPresent()) {
            uhcPlayer.getTeam().get().getPlayers().forEach(teammate -> {
                if (!teammate.equals(uhcPlayer)) {
                    Player teammatePlayer = teammate.getPlayer();
                    if (teammatePlayer != null && teammatePlayer.getLocation().distance(player.getLocation()) <= 6.0) {
                        teammatePlayer.sendMessage("§6[Paladin] §aVous bénéficiez de la bénédiction de " + player.getName() + " !");
                    }
                }
            });
        }
    }

    /**
     * Applique la résistance à faible vie
     */
    private void applyLowHealthResistance(Player player) {
        if (player.getHealth() <= LOW_HEALTH_THRESHOLD) {
            PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false);
            LegendEffects.applyEffect(player, resistance);
        }
    }

    /**
     * Applique le bonus de force près des alliés
     */
    private void applyAllyBonus(Player player, UHCPlayer uhcPlayer) {
        if (!uhcPlayer.getTeam().isPresent()) return;

        // Compter les alliés proches
        long nearbyAllies = uhcPlayer.getTeam().get().getPlayers().stream()
                .filter(teammate -> !teammate.equals(uhcPlayer))
                .map(UHCPlayer::getPlayer)
                .filter(teammatePlayer -> teammatePlayer != null && teammatePlayer.isOnline())
                .filter(teammatePlayer -> teammatePlayer.getLocation().distance(player.getLocation()) <= ALLY_BONUS_RADIUS)
                .count();

        // Appliquer le bonus si au moins un allié est proche
        if (nearbyAllies > 0) {
            int strengthLevel = (int) Math.min(nearbyAllies - 1, 1); // Maximum Strength I
            PotionEffect strength = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, strengthLevel, false, false);
            LegendEffects.applyEffect(player, strength);
        }
    }

    /**
     * Donne l'équipement de départ du Paladin
     */
    private void giveStartingEquipment(Player player) {
        // Épée en fer avec Smite
        player.getInventory().addItem(
                new ItemCreator(Material.IRON_SWORD)
                        .addEnchantment(org.bukkit.enchantments.Enchantment.DAMAGE_UNDEAD, 2)
                        .setName("§6Épée Sacrée")
                        .setLores(Arrays.asList("§7Smite II", "§7Efficace contre les morts-vivants"))
                        .setUnbreakable(true)
                        .getItemstack()
        );

        // Bouclier (représenté par un item spécial)
        player.getInventory().addItem(
                new ItemCreator(Material.IRON_INGOT)
                        .setName("§7Bouclier Sacré")
                        .addLore("§7Symbole de votre foi")
                        .getItemstack()
        );
    }

    /**
     * Formate un temps en secondes
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + "m" + (remainingSeconds > 0 ? remainingSeconds + "s" : "");
    }
}
