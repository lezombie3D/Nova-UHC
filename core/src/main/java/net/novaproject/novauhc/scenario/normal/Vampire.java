package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.VampireLang;
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

import java.util.HashMap;
import java.util.Map;

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
    public String getPath() {
        return "vampire";
    }

    @Override
    public ScenarioLang[] getLang() {
        return VampireLang.values();
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

            double healAmount = getConfig().getDouble("heal_amount", 2.0);
            double currentHealth = killerPlayer.getHealth();
            double maxHealth = killerPlayer.getMaxHealth();
            double newHealth = Math.min(maxHealth, currentHealth + healAmount);

            killerPlayer.setHealth(newHealth);

            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%victim%", uhcPlayer.getPlayer().getName());
            placeholders.put("%heal_hearts%", String.valueOf(healAmount / 2));
            ScenarioLangManager.send(killerPlayer, VampireLang.KILL_HEAL, placeholders);

            int nightVisionDuration = getConfig().getInt("night_vision_duration", 600);
            killerPlayer.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, nightVisionDuration, 0));
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
                                double sunDamage = getConfig().getDouble("sun_damage_amount", 1.0);
                                player.damage(sunDamage);
                                Map<String, Object> sunPlaceholders = new HashMap<>();
                                sunPlaceholders.put("%sun_damage%", String.valueOf(sunDamage));
                                ScenarioLangManager.send(uhcPlayer.getPlayer(), VampireLang.SUN_DAMAGE, sunPlaceholders);

                                // Add fire effect for visual
                                player.setFireTicks(40);
                            }
                        }
                    }
                }
            }
        };

        // Run based on config interval
        int interval = getConfig().getInt("sun_damage_interval", 40);
        sunDamageTask.runTaskTimer(Main.get(), 0, interval);
    }

    private void stopSunDamageTask() {
        if (sunDamageTask != null) {
            sunDamageTask.cancel();
            sunDamageTask = null;
        }
    }

}
