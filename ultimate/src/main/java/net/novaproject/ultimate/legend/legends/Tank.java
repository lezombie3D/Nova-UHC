package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.ultimate.legend.core.LegendClass;
import net.novaproject.ultimate.legend.utils.LegendEffects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Légende du Tank
 * <p>
 * Capacités :
 * - +2 cœurs permanents (24 HP au lieu de 20)
 * - Passif : Résistance I quand la vie est en dessous de 11 cœurs
 * - Pas de pouvoir activable
 *
 * @author NovaProject
 * @version 3.0
 */
public class Tank extends LegendClass {

    public Tank() {
        super(4, "Tank", "Guerrier robuste avec plus de vie et de résistance", Material.DIAMOND_CHESTPLATE);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Tank] §aVous êtes maintenant un Tank !");
        player.sendMessage("§7Vous avez +2 cœurs permanents");
        player.sendMessage("§7Résistance I quand votre vie est faible");

        // Donner +2 cœurs
        player.setMaxHealth(24.0);
        player.setHealth(24.0);
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        // Le Tank n'a pas de pouvoir activable
        player.sendMessage("§c[Tank] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Effet passif : Résistance quand la vie est faible
        if (player.getHealth() < 11.0) {
            PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false);
            LegendEffects.applyEffect(player, resistance);
        }
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial à la mort
    }

    @Override
    public void cleanup(Player player, UHCPlayer uhcPlayer) {
        // Remettre la vie normale
        player.setMaxHealth(20.0);
        if (player.getHealth() > 20.0) {
            player.setHealth(20.0);
        }
    }

    @Override
    public boolean hasPower() {
        return false; // Le Tank n'a pas de pouvoir activable
    }
}
