package net.novaproject.novauhc.scenario.special.legend.legends;

import net.novaproject.novauhc.scenario.special.legend.Legend;
import net.novaproject.novauhc.scenario.special.legend.core.LegendClass;
import net.novaproject.novauhc.scenario.special.legend.core.LegendData;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendEffects;
import net.novaproject.novauhc.scenario.special.legend.utils.LegendItems;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

/**
 * Légende du Cavalier
 */
public class Cavalier extends LegendClass {

    private static final String HORSE_COOLDOWN = "horse";
    private static final long HORSE_COOLDOWN_MS = 5 * 60 * 1000L; // 5 minutes

    public Cavalier() {
        super(11, "Cavalier", "Invoque un cheval pour se déplacer", Material.SADDLE);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Cavalier] §aVous êtes maintenant un Cavalier !");
        player.sendMessage("§7Pouvoir : Invoque un cheval (Cooldown 5 minutes)");
        player.getInventory().addItem(LegendItems.createPowerItem("Cavalier"));
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        if (!data.isCooldownReady(HORSE_COOLDOWN)) {
            long remaining = data.getCooldownRemaining(HORSE_COOLDOWN);
            LegendEffects.sendCooldownMessage(player, formatTime(remaining));
            return false;
        }

        // Invoquer un cheval
        Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        horse.setOwner(player);
        horse.setTamed(true);
        horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(Material.SADDLE));

        data.setCooldown(HORSE_COOLDOWN, HORSE_COOLDOWN_MS);
        LegendEffects.sendPowerActivatedMessage(player, "Cavalier");
        player.sendMessage("§6[Cavalier] §aCheval invoqué !");

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
