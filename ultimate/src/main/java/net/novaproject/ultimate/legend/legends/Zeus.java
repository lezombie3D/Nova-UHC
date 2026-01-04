package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
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
import java.util.Collections;
import java.util.List;

/**
 * Légende de Zeus
 */
public class Zeus extends LegendClass {

    private static final String POWER_COOLDOWN = "zeus_power";
    private static final long POWER_COOLDOWN_MS = 10 * 60 * 1000L; // 10 minutes

    public Zeus() {
        super(6, "Zeus", "Maître des éclairs avec effets aléatoires", Material.ARROW);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Zeus] §aVous êtes maintenant Zeus !");
        player.sendMessage("§7Pouvoir : 2 effets aléatoires pendant 2 minutes");
        player.getInventory().addItem(LegendItems.createPowerItem("Zeus"));
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        if (!data.isCooldownReady(POWER_COOLDOWN)) {
            long remaining = data.getCooldownRemaining(POWER_COOLDOWN);
            LegendEffects.sendCooldownMessage(player, formatTime(remaining));
            return false;
        }

        // Donner 2 effets aléatoires
        List<PotionEffect> effects = Arrays.asList(
                new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 2, 0),
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 60 * 2, 0),
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * 60 * 2, 0)
        );
        Collections.shuffle(effects);

        for (int i = 0; i < 2; i++) {
            LegendEffects.applyEffect(player, effects.get(i));
        }

        data.setCooldown(POWER_COOLDOWN, POWER_COOLDOWN_MS);
        LegendEffects.sendPowerActivatedMessage(player, "Zeus");
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
