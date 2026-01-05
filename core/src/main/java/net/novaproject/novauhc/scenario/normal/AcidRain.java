package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.scenario.lang.lang.AcidRainLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
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

    @ScenarioVariable(name = "Temps entre les pluies", description = "Temps de base en secondes entre deux pluies",type = VariableType.TIME)
    private int nextRainInBase = 300;

    @ScenarioVariable(name = "Durée de la pluie", description = "Durée de base de la pluie en secondes", type = VariableType.TIME)
    private int rainDurationBase = 60;

    @ScenarioVariable(name = "Dégâts", description = "Dégâts par seconde sous la pluie", type = VariableType.DOUBLE)
    private double rainDamage = 1.0;

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
    public void toggleActive() {
        super.toggleActive();
        if (!isActive()) {
            stopAcidRainCycle();
        }
    }


    @Override
    public void onGameStart() {
        startAcidRainCycle();
    }

    private void startAcidRainCycle() {
        if (acidRainTask != null) {
            acidRainTask.cancel();
        }

        acidRainTask = new BukkitRunnable() {
            private int cycleTimer = 0;
            private int rainDuration = 0;
            private int nextRainIn = nextRainInBase + random.nextInt(nextRainInBase);

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                cycleTimer++;

                if (!isRaining) {
                    if (cycleTimer >= nextRainIn) {
                        startAcidRain();
                        cycleTimer = 0;
                        rainDuration = rainDurationBase + random.nextInt(rainDurationBase * 2);
                    } else {
                        int timeLeft = nextRainIn - cycleTimer;
                        if (timeLeft == 60) {
                            Bukkit.broadcastMessage("§c[AcidRain] §fPluie acide dans 1 minute ! Trouvez un abri !");
                        } else if (timeLeft == 10) {
                            Bukkit.broadcastMessage("§c[AcidRain] §fPluie acide dans 10 secondes !");
                        }
                    }
                } else {
                    damageExposedPlayers();
                    rainDuration--;

                    if (rainDuration <= 0) {
                        stopAcidRain();
                        cycleTimer = 0;
                        nextRainIn = nextRainInBase + random.nextInt(nextRainInBase);
                    } else {
                        if (rainDuration == 10) {
                            Bukkit.broadcastMessage("§c[AcidRain] §fLa pluie acide s'arrête dans 10 secondes !");
                        }
                    }
                }
            }
        };

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

        for (World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }

        ScenarioLangManager.sendAll(AcidRainLang.ACID_RAIN_START);

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.AMBIENCE_THUNDER, 1.0f, 1.0f);
        }
    }

    private void stopAcidRain() {
        isRaining = false;

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
                player.damage(rainDamage);
                player.sendMessage("§c[AcidRain] §fVous êtes brûlé par la pluie acide ! Trouvez un abri !");

                player.setFireTicks(20);
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

}
