package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendEffects;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Légende du Médecin
 */
public class Medecin extends LegendClass {

    public Medecin() {
        super(14, "Médecin", "Zone de soin pour les alliés proches", Material.GOLDEN_APPLE);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Médecin] §aVous êtes maintenant un Médecin !");
        player.sendMessage("§7Régénération I pour les alliés dans un rayon de 6 blocs");
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§c[Médecin] Vous n'avez pas de pouvoir activable !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Zone de soin pour les alliés
        PotionEffect regeneration = new PotionEffect(PotionEffectType.REGENERATION, 80, 0, false, false);
        LegendEffects.applyTeamEffect(player, uhcPlayer, regeneration, 6.0, false);
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial
    }

    @Override
    public boolean hasPower() {
        return false;
    }
}
