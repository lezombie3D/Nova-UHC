package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.ultimate.legend.Legend;
import net.novaproject.ultimate.legend.core.LegendClass;
import net.novaproject.ultimate.legend.core.LegendData;
import net.novaproject.ultimate.legend.utils.LegendItems;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Légende du Mage
 * <p>
 * Capacités :
 * - Passif : Reçoit 3 potions (Force, Résistance, Vitesse) toutes les 10 minutes
 * - Pas de pouvoir activable
 *
 * @author NovaProject
 * @version 3.0
 */
public class Mage extends LegendClass {

    private static final String POTION_COOLDOWN = "potions";
    private static final long POTION_COOLDOWN_MS = 10 * 60 * 1000L; // 10 minutes

    public Mage() {
        super(1, "Mage", "Reçoit des potions magiques toutes les 10 minutes", Material.POTION);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Mage] §aVous êtes maintenant un Mage !");
        player.sendMessage("§7Vous recevrez des potions toutes les 10 minutes");

        // Donner les premières potions immédiatement
        LegendItems.dropMagePotions(player);

        // Démarrer le cooldown
        LegendData data = Legend.get().getPlayerData(uhcPlayer);
        data.setCooldown(POTION_COOLDOWN, POTION_COOLDOWN_MS);
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        // Le Mage n'a pas de pouvoir activable
        player.sendMessage("§c[Mage] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        // Vérifier si les potions sont prêtes
        if (data.isCooldownReady(POTION_COOLDOWN)) {
            // Donner les potions
            LegendItems.dropMagePotions(player);
            player.sendMessage("§6[Mage] §aVous avez reçu de nouvelles potions !");

            // Redémarrer le cooldown
            data.setCooldown(POTION_COOLDOWN, POTION_COOLDOWN_MS);
        }
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial à la mort
    }

    @Override
    public boolean hasPower() {
        return false; // Le Mage n'a pas de pouvoir activable
    }
}
