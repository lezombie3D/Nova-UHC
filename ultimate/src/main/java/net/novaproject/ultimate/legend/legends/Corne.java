package net.novaproject.ultimate.legend.legends;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.ultimate.legend.Legend;
import net.novaproject.ultimate.legend.core.LegendClass;
import net.novaproject.ultimate.legend.core.LegendData;
import net.novaproject.ultimate.legend.utils.LegendEffects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

/**
 * Légende de la Corne
 */
public class Corne extends LegendClass {

    private static final String FIRE_COOLDOWN = "melodie_feu";
    private static final String HEAL_COOLDOWN = "melodie_heal";
    private static final String METAL_COOLDOWN = "melodie_metal";
    private static final String AIR_COOLDOWN = "melodie_air";

    private static final long FIRE_COOLDOWN_MS = 60 * 1000L; // 1 minute
    private static final long HEAL_COOLDOWN_MS = 10 * 60 * 1000L; // 10 minutes
    private static final long METAL_COOLDOWN_MS = 60 * 1000L; // 1 minute
    private static final long AIR_COOLDOWN_MS = 3 * 60 * 1000L; // 3 minutes

    public Corne() {
        super(16, "Corne", "Mélodies magiques pour aider l'équipe", Material.JUKEBOX);
    }

    @Override
    public void onChoose(Player player, UHCPlayer uhcPlayer) {
        player.sendMessage("§6[Corne] §aVous êtes maintenant une Corne !");
        player.sendMessage("§7Vous avez 4 mélodies différentes");

        giveMelodies(player);
    }

    @Override
    public boolean onPower(Player player, UHCPlayer uhcPlayer) {
        // Les mélodies sont gérées individuellement
        player.sendMessage("§6[Corne] §aUtilisez vos mélodies individuellement !");
        return false;
    }

    @Override
    public void onTick(Player player, UHCPlayer uhcPlayer) {
        UHCUtils.applyInfiniteEffects(new PotionEffect[]{
                new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false)
        }, player);
    }

    @Override
    public void onDeath(Player player, UHCPlayer uhcPlayer, UHCPlayer killer) {
        // Pas d'effet spécial
    }

    /**
     * Active une mélodie spécifique
     */
    public boolean activateMelody(Player player, UHCPlayer uhcPlayer, String melodyName) {
        LegendData data = Legend.get().getPlayerData(uhcPlayer);

        switch (melodyName) {
            case "§6Melodie : Feu":
                if (!data.isCooldownReady(FIRE_COOLDOWN)) {
                    LegendEffects.sendCooldownMessage(player, formatTime(data.getCooldownRemaining(FIRE_COOLDOWN)));
                    return false;
                }

                // Fire Resistance II pendant 12 secondes
                PotionEffect fireRes = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20 * 12, 1);
                LegendEffects.applyTeamEffect(player, uhcPlayer, fireRes, 31.0);
                data.setCooldown(FIRE_COOLDOWN, FIRE_COOLDOWN_MS);
                player.sendMessage("§6[Corne] §aMélodie de Feu activée !");
                return true;

            case "§6Melodie : Heal":
                if (!data.isCooldownReady(HEAL_COOLDOWN)) {
                    LegendEffects.sendCooldownMessage(player, formatTime(data.getCooldownRemaining(HEAL_COOLDOWN)));
                    return false;
                }

                // Soigner complètement l'équipe
                if (uhcPlayer.getTeam().isPresent()) {
                    uhcPlayer.getTeam().get().getPlayers().forEach(teammate -> {
                        Player teammatePlayer = teammate.getPlayer();
                        if (teammatePlayer != null && teammatePlayer.getLocation().distance(player.getLocation()) < 31) {
                            teammatePlayer.setHealth(teammatePlayer.getMaxHealth());
                            teammatePlayer.sendMessage("§6[Corne] §aVous avez été soigné par la mélodie !");
                        }
                    });
                }
                data.setCooldown(HEAL_COOLDOWN, HEAL_COOLDOWN_MS);
                player.sendMessage("§6[Corne] §aMélodie de Soin activée !");
                return true;

            case "§6Melodie : Metal":
                if (!data.isCooldownReady(METAL_COOLDOWN)) {
                    LegendEffects.sendCooldownMessage(player, formatTime(data.getCooldownRemaining(METAL_COOLDOWN)));
                    return false;
                }

                // Resistance II pendant 5 secondes
                PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20 * 5, 1);
                LegendEffects.applyTeamEffect(player, uhcPlayer, resistance, 31.0);
                data.setCooldown(METAL_COOLDOWN, METAL_COOLDOWN_MS);
                player.sendMessage("§6[Corne] §aMélodie de Métal activée !");
                return true;

            case "§6Melodie : Air":
                if (!data.isCooldownReady(AIR_COOLDOWN)) {
                    LegendEffects.sendCooldownMessage(player, formatTime(data.getCooldownRemaining(AIR_COOLDOWN)));
                    return false;
                }

                // Speed II pendant 8 secondes
                PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, 20 * 8, 1);
                LegendEffects.applyTeamEffect(player, uhcPlayer, speed, 31.0);
                data.setCooldown(AIR_COOLDOWN, AIR_COOLDOWN_MS);
                player.sendMessage("§6[Corne] §aMélodie d'Air activée !");
                return true;
        }

        return false;
    }

    private void giveMelodies(Player player) {
        player.getInventory().addItem(new ItemCreator(Material.NETHER_STAR)
                .setName("§6Melodie : Feu")
                .setLores(Arrays.asList("§7Fire Resistance II - 12s", "§7Cooldown: 1 minute"))
                .getItemstack());

        player.getInventory().addItem(new ItemCreator(Material.NETHER_STAR)
                .setName("§6Melodie : Heal")
                .setLores(Arrays.asList("§7Soigne complètement l'équipe", "§7Cooldown: 10 minutes"))
                .getItemstack());

        player.getInventory().addItem(new ItemCreator(Material.NETHER_STAR)
                .setName("§6Melodie : Metal")
                .setLores(Arrays.asList("§7Resistance II - 5s", "§7Cooldown: 1 minute"))
                .getItemstack());

        player.getInventory().addItem(new ItemCreator(Material.NETHER_STAR)
                .setName("§6Melodie : Air")
                .setLores(Arrays.asList("§7Speed II - 8s", "§7Cooldown: 3 minutes"))
                .getItemstack());
    }

    private String formatTime(long seconds) {
        if (seconds < 60) return seconds + "s";
        return (seconds / 60) + "m" + (seconds % 60 > 0 ? (seconds % 60) + "s" : "");
    }
}
