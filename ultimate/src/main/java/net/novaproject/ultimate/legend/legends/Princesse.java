package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.ultimate.legend.core.LegendClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Légende de la Princesse
 */
public class Princesse extends LegendClass {

    public Princesse() {
        super(10, "Princesse", "Immunité aux dégâts de chute et +2 cœurs", Material.YELLOW_FLOWER);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Princesse] §aVous êtes maintenant une Princesse !");
        player.sendMessage("§7Immunité aux dégâts de chute et +2 cœurs");

        // Donner +2 cœurs
        player.setMaxHealth(24.0);
        player.setHealth(24.0);
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Princesse] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Pas d'effets passifs particuliers
    }

    @Override
    public void onDamage(Player player, UHCPlayer uhcPlayer, double damage) {
        // L'immunité aux dégâts de chute est gérée dans Legend.java
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
