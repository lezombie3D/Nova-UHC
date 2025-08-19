package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BloodLust extends Scenario {

    private final Map<UUID, BukkitRunnable> activeEffects = new HashMap<>();

    @Override
    public String getName() {
        return "BloodLust";
    }

    @Override
    public String getDescription() {
        return "Chaque kill donne Speed II et Strength I pendant 30 secondes.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BLAZE_POWDER);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            Player killerPlayer = killer.getPlayer();

            // Cancel any existing blood lust effect
            cancelBloodLustEffect(killerPlayer.getUniqueId());

            // Apply blood lust effects
            applyBloodLustEffect(killerPlayer);

            // Send message to killer
            killerPlayer.sendMessage("§c[BloodLust] §fVous ressentez la soif de sang ! Speed II et Strength I pendant 30 secondes !");

            // Broadcast to all players
            Bukkit.broadcastMessage("§c[BloodLust] §f" + killerPlayer.getName() + " §fest en état de soif de sang !");
        }
    }

    private void applyBloodLustEffect(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Apply potion effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 600, 1)); // Speed II for 30 seconds
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 600, 0)); // Strength I for 30 seconds

        // Create countdown task
        BukkitRunnable countdownTask = new BukkitRunnable() {
            private int timeLeft = 30;

            @Override
            public void run() {
                if (!player.isOnline() || !isActive()) {
                    cancel();
                    activeEffects.remove(playerUuid);
                    return;
                }

                timeLeft--;

                // Send countdown messages at specific intervals
                if (timeLeft == 10) {
                    player.sendMessage("§c[BloodLust] §fSoif de sang se termine dans 10 secondes !");
                } else if (timeLeft == 5) {
                    player.sendMessage("§c[BloodLust] §fSoif de sang se termine dans 5 secondes !");
                } else if (timeLeft == 0) {
                    player.sendMessage("§c[BloodLust] §fVotre soif de sang s'est calmée.");

                    // Remove effects manually to ensure they're gone
                    player.removePotionEffect(PotionEffectType.SPEED);
                    player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);

                    activeEffects.remove(playerUuid);
                    cancel();
                }
            }
        };

        // Store the task and start it
        activeEffects.put(playerUuid, countdownTask);
        countdownTask.runTaskTimer(Main.get(), 0, 20); // Run every second
    }

    private void cancelBloodLustEffect(UUID playerUuid) {
        BukkitRunnable existingTask = activeEffects.get(playerUuid);
        if (existingTask != null) {
            existingTask.cancel();
            activeEffects.remove(playerUuid);

            // Remove effects from player
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                player.removePotionEffect(PotionEffectType.SPEED);
                player.removePotionEffect(PotionEffectType.INCREASE_DAMAGE);
            }
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        if (!isActive()) {
            // Cancel all active effects when scenario is disabled
            for (UUID playerUuid : activeEffects.keySet()) {
                cancelBloodLustEffect(playerUuid);
            }
            activeEffects.clear();
        }
    }

    // Clean up when players disconnect
    public void onPlayerDisconnect(Player player) {
        cancelBloodLustEffect(player.getUniqueId());
    }

    // Check if a player has blood lust active
    public boolean hasBloodLust(Player player) {
        return activeEffects.containsKey(player.getUniqueId());
    }

    // Get remaining blood lust time for a player
    public int getBloodLustTimeLeft(Player player) {
        BukkitRunnable task = activeEffects.get(player.getUniqueId());
        if (task != null) {
            // This is a simplified version - in a real implementation you'd track the time more precisely
            return player.hasPotionEffect(PotionEffectType.SPEED) ?
                    player.getPotionEffect(PotionEffectType.SPEED).getDuration() / 20 : 0;
        }
        return 0;
    }
}
