package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.VampireLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Vampire extends Scenario {

    private BukkitRunnable sunDamageTask;
    @ScenarioVariable(name = "Vie",description = "Permet de definir la vie gagnez par kill", type = VariableType.DOUBLE)
    private double healAmount = 2.0; // Default heal amount (1 heart)
    @ScenarioVariable(name = "Degat du Soleil",description = "Permet de definir les dégats infligés par le soleil", type = VariableType.DOUBLE)
    double sunDamage = 1.0;
    @ScenarioVariable(name = "Inverval",description = "Definis l'interval entre les degat du soleil",type = VariableType.INTEGER)
    private int interval = 40;
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
    public void onGameStart() {
        Common.get().getArena().setTime(12000);
        startSunDamageTask();
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        if (killer != null) {
            Player killerPlayer = killer.getPlayer();

            double currentHealth = killerPlayer.getHealth();
            double maxHealth = killerPlayer.getMaxHealth();
            double newHealth = Math.min(maxHealth, currentHealth + healAmount);

            killerPlayer.setHealth(newHealth);

            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%victim%", uhcPlayer.getPlayer().getName());
            placeholders.put("%heal_hearts%", String.valueOf(healAmount / 2));
            ScenarioLangManager.send(killerPlayer, VampireLang.KILL_HEAL, placeholders);

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

                    long time = world.getTime();
                    boolean isDay = time >= 0 && time < 12000;

                    if (isDay) {
                        if (world.getHighestBlockYAt(player.getLocation()) <= player.getLocation().getBlockY()) {
                            if (player.getInventory().getHelmet() == null ||
                                    player.getInventory().getHelmet().getType() == Material.AIR) {

                                player.damage(sunDamage);
                                Map<String, Object> sunPlaceholders = new HashMap<>();
                                sunPlaceholders.put("%sun_damage%", String.valueOf(sunDamage));
                                ScenarioLangManager.send(uhcPlayer.getPlayer(), VampireLang.SUN_DAMAGE, sunPlaceholders);

                                player.setFireTicks(40);
                            }
                        }
                    }
                }
            }
        };

        sunDamageTask.runTaskTimer(Main.get(), 0, interval);
    }


}
