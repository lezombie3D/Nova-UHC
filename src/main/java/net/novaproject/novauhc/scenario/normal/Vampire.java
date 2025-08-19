package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class Vampire extends Scenario {

    private BukkitRunnable sunDamageTask;

    @Override
    public String getName() {
        return "Vampire";
    }

    @Override
    public String getDescription() {
        return "Récupérez 1 cœur en tuant un joueur, mais brûlez au soleil sans armure.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.REDSTONE);
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startSunDamageTask();
        } else {
            stopSunDamageTask();
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            Player killerPlayer = killer.getPlayer();

            double currentHealth = killerPlayer.getHealth();
            double maxHealth = killerPlayer.getMaxHealth();
            double newHealth = Math.min(maxHealth, currentHealth + 2.0);

            killerPlayer.setHealth(newHealth);

            killerPlayer.sendMessage("§c[Vampire] §fVous avez récupéré 1 cœur en tuant " + uhcPlayer.getPlayer().getName() + " !");

            // Give night vision effect for 30 seconds
            killerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
        }
    }

    private void startSunDamageTask() {
        if (sunDamageTask != null) {
            sunDamageTask.cancel();
        }

        sunDamageTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    Player player = uhcPlayer.getPlayer();
                    World world = player.getWorld();

                    // Check if it's day time (time between 0 and 12000)
                    long time = world.getTime();
                    boolean isDay = time >= 0 && time < 12000;

                    if (isDay) {
                        // Check if player is under sunlight
                        if (world.getHighestBlockYAt(player.getLocation()) <= player.getLocation().getBlockY()) {
                            // Check if player has no helmet
                            if (player.getInventory().getHelmet() == null ||
                                    player.getInventory().getHelmet().getType() == Material.AIR) {

                                // Damage player
                                player.damage(1.0);
                                player.sendMessage("§c[Vampire] §fVous brûlez au soleil ! Équipez un casque ou trouvez de l'ombre !");

                                // Add fire effect for visual
                                player.setFireTicks(40);
                            }
                        }
                    }
                }
            }
        };

        // Run every 2 seconds (40 ticks)
        sunDamageTask.runTaskTimerAsynchronously(Main.get(), 0, 40);
    }

    private void stopSunDamageTask() {
        if (sunDamageTask != null) {
            sunDamageTask.cancel();
            sunDamageTask = null;
        }
    }

}
