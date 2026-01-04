package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.ultimate.legend.core.LegendClass;
import net.novaproject.ultimate.legend.utils.LegendEffects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Légende du Prisonnier
 */
public class Prisonnier extends LegendClass {

    public Prisonnier() {
        super(15, "Prisonnier", "Vitesse permanente mais moins de vie", Material.DIAMOND_AXE);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Prisonnier] §aVous êtes maintenant un Prisonnier !");
        player.sendMessage("§7Vitesse I permanente mais -2 cœurs");

        // Réduire la vie de 2 cœurs
        player.setMaxHealth(16.0);
        player.setHealth(16.0);
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Prisonnier] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Vitesse permanente
        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false);
        LegendEffects.applyEffect(player, speed);
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial
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
        return false;
    }
}
