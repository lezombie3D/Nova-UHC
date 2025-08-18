package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.Legend;
import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.core.LegendData;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendEffects;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendItems;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Légende du Succube
 */
public class Succube extends LegendClass {

    private static final String POWER_COOLDOWN = "succube_power";
    private static final long POWER_COOLDOWN_MS = 6 * 60 * 1000L; // 6 minutes

    public Succube() {
        super(8, "Succube", "Donne Absorption III à l'équipe", Material.APPLE);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Succube] §aVous êtes maintenant un Succube !");
        player.sendMessage("§7Pouvoir : Absorption III pour l'équipe dans un rayon de 11 blocs");
        player.getInventory().addItem(LegendItems.createPowerItem("Succube"));
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        if (!data.isCooldownReady(POWER_COOLDOWN)) {
            long remaining = data.getCooldownRemaining(POWER_COOLDOWN);
            LegendEffects.sendCooldownMessage(player, formatTime(remaining));
            return false;
        }

        // Donner Absorption III à l'équipe
        PotionEffect absorption = new PotionEffect(PotionEffectType.ABSORPTION, 20 * 60, 2);
        LegendEffects.applyTeamEffect(player, uhcPlayer, absorption, 11.0, false);

        data.setCooldown(POWER_COOLDOWN, POWER_COOLDOWN_MS);
        LegendEffects.sendPowerActivatedMessage(player, "Succube");
        player.sendMessage("§6[Succube] §aAbsorption III donnée à votre équipe !");

        return true;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        // Pas d'effets passifs
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m" + (seconds % 60 > 0 ? (seconds % 60) + "s" : "");
    }
}
