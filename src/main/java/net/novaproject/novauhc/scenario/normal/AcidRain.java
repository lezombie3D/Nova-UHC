package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.AcidRainLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class AcidRain extends Scenario {

    private final Random random = new Random();
    private BukkitRunnable acidRainTask;
    private boolean isRaining = false;

    @Override
    public String getName() {
        return "AcidRain";
    }

    @Override
    public String getDescription() {
        return "Pluie acide qui fait des dégâts. Protégez-vous sous des blocs !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WATER_BUCKET);
    }

    @Override
    public String getPath() {
        return "acidrain";
    }

    @Override
    public ScenarioLang[] getLang() {
        return AcidRainLang.values();
    }

    @Override
    public void enable() {
        super.enable();
        if (isActive()) {
            startAcidRainCycle();
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startAcidRainCycle();
        } else {
            stopAcidRainCycle();
        }
    }

    private void startAcidRainCycle() {
        if (acidRainTask != null) {
            acidRainTask.cancel();
        }

        acidRainTask = new BukkitRunnable() {
            private int cycleTimer = 0;
            private int rainDuration = 0;
            private int nextRainIn = 300 + random.nextInt(300); // 5-10 minutes until first rain

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                cycleTimer++;

                if (!isRaining) {
                    // Waiting for next rain
                    if (cycleTimer >= nextRainIn) {
                        startAcidRain();
                        cycleTimer = 0;
                        rainDuration = 60 + random.nextInt(120); // Rain for 1-3 minutes
                    } else {
                        // Warning messages
                        int timeLeft = nextRainIn - cycleTimer;
                        if (timeLeft == 60) {
                            Bukkit.broadcastMessage("§c[AcidRain] §fPluie acide dans 1 minute ! Trouvez un abri !");
                        } else if (timeLeft == 10) {
                            Bukkit.broadcastMessage("§c[AcidRain] §fPluie acide dans 10 secondes !");
                        }
                    }
                } else {
                    // Currently raining acid
                    damageExposedPlayers();
                    rainDuration--;

                    if (rainDuration <= 0) {
                        stopAcidRain();
                        cycleTimer = 0;
                        nextRainIn = 300 + random.nextInt(300); // Next rain in 5-10 minutes
                    } else {
                        // Warning messages during rain
                        if (rainDuration == 10) {
                            Bukkit.broadcastMessage("§c[AcidRain] §fLa pluie acide s'arrête dans 10 secondes !");
                        }
                    }
                }
            }
        };

        // Run every second
        acidRainTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void stopAcidRainCycle() {
        if (acidRainTask != null) {
            acidRainTask.cancel();
            acidRainTask = null;
        }

        if (isRaining) {
            stopAcidRain();
        }
    }

    private void startAcidRain() {
        isRaining = true;

        // Set weather to rain in all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }

        ScenarioLangManager.sendAll(AcidRainLang.ACID_RAIN_START);

        // Play thunder sound for dramatic effect
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.AMBIENCE_THUNDER, 1.0f, 1.0f);
        }
    }

    private void stopAcidRain() {
        isRaining = false;

        // Stop rain in all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setWeatherDuration(0);
        }

        Bukkit.broadcastMessage("§c[AcidRain] §fLa pluie acide s'est arrêtée. Vous pouvez sortir en sécurité !");
    }

    private void damageExposedPlayers() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();

            if (isPlayerExposedToRain(player)) {
                // Damage the player
                player.damage(1.0);
                player.sendMessage("§c[AcidRain] §fVous êtes brûlé par la pluie acide ! Trouvez un abri !");

                // Visual effect
                player.setFireTicks(20); // Set on fire for 1 second for visual effect
            }
        }
    }

    private boolean isPlayerExposedToRain(Player player) {
        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();

        // Check if it's raining in this world
        if (!world.hasStorm()) {
            return false;
        }

        // Check if player is underground (below Y=60)
        if (playerLoc.getY() < 60) {
            return false;
        }

        // Check if player has blocks above them (shelter)
        Location checkLoc = playerLoc.clone();

        // Check up to 10 blocks above the player
        for (int y = 1; y <= 10; y++) {
            checkLoc.add(0, 1, 0);
            Material blockType = checkLoc.getBlock().getType();

            // If there's a solid block above, player is protected
            if (blockType != Material.AIR &&
                    blockType != Material.WATER &&
                    blockType != Material.LAVA &&
                    !blockType.name().contains("LEAVES")) {
                return false;
            }
        }

        // Player is exposed to rain
        return true;
    }

    // Check if acid rain is currently active
    public boolean isAcidRaining() {
        return isRaining;
    }

    // Force start acid rain (admin command)
    public void forceStartAcidRain() {
        if (isActive() && !isRaining) {
            startAcidRain();
            Bukkit.broadcastMessage("§c[AcidRain] §fPluie acide forcée par un administrateur !");
        }
    }

    // Force stop acid rain (admin command)
    public void forceStopAcidRain() {
        if (isActive() && isRaining) {
            stopAcidRain();
            Bukkit.broadcastMessage("§c[AcidRain] §fPluie acide arrêtée par un administrateur !");
        }
    }

    // Get shelter status for a player
    public boolean isPlayerSheltered(Player player) {
        return !isPlayerExposedToRain(player);
    }

    @Override
    public void onSec(Player p) {
        // This method is called every second for each player
        // We use the BukkitRunnable instead for better performance
    }
}
