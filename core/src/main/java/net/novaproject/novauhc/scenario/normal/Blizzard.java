package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class Blizzard extends Scenario {

    private final Random random = new Random();
    private final Map<UUID, Integer> playerWarmth = new HashMap<>();
    private BukkitRunnable blizzardTask;
    private boolean isBlizzardActive = false;

    @Override
    public String getName() {
        return "Blizzard";
    }

    @Override
    public String getDescription() {
        return "Tempêtes de neige qui ralentissent et aveuglent. Restez près du feu !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SNOW_BALL);
    }


    @Override
    public void toggleActive() {
        super.toggleActive();
        if (!isActive()) {
            stopBlizzardCycle();
            clearPlayerEffects();
        } else {

        }
    }

    @Override
    public void onGameStart() {
        startBlizzardCycle();
        initializePlayerWarmth();
    }

    private void startBlizzardCycle() {
        if (blizzardTask != null) {
            blizzardTask.cancel();
        }

        blizzardTask = new BukkitRunnable() {
            private int cycleTimer = 0;
            private int blizzardDuration = 0;
            private int nextBlizzardIn = 240 + random.nextInt(240);

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                cycleTimer++;

                if (!isBlizzardActive) {
                    if (cycleTimer >= nextBlizzardIn) {
                        startBlizzard();
                        cycleTimer = 0;
                        blizzardDuration = 90 + random.nextInt(90);
                    } else {
                        int timeLeft = nextBlizzardIn - cycleTimer;
                        if (timeLeft == 60) {
                            Bukkit.broadcastMessage("§b[Blizzard] §fTempête de neige dans 1 minute ! Préparez-vous !");
                        } else if (timeLeft == 10) {
                            Bukkit.broadcastMessage("§b[Blizzard] §fTempête de neige dans 10 secondes !");
                        }
                    }
                } else {
                    // Currently in blizzard
                    applyBlizzardEffects();
                    blizzardDuration--;

                    if (blizzardDuration <= 0) {
                        stopBlizzard();
                        cycleTimer = 0;
                        nextBlizzardIn = 240 + random.nextInt(240);
                    } else {
                        if (blizzardDuration == 10) {
                            Bukkit.broadcastMessage("§b[Blizzard] §fLa tempête se calme dans 10 secondes !");
                        }
                    }
                }

                updatePlayerWarmth();
            }
        };

        blizzardTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void stopBlizzardCycle() {
        if (blizzardTask != null) {
            blizzardTask.cancel();
            blizzardTask = null;
        }

        if (isBlizzardActive) {
            stopBlizzard();
        }
    }

    private void startBlizzard() {
        isBlizzardActive = true;

        for (World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }

        Bukkit.broadcastMessage("§b§l[Blizzard] §fUne tempête de neige commence ! Trouvez de la chaleur !");

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.AMBIENCE_CAVE, 0.5f, 0.8f);
        }
    }

    private void stopBlizzard() {
        isBlizzardActive = false;

        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setWeatherDuration(0);
        }

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }

        Bukkit.broadcastMessage("§b[Blizzard] §fLa tempête de neige s'est calmée !");
    }

    private void applyBlizzardEffects() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            UUID playerUuid = player.getUniqueId();

            if (isPlayerExposedToBlizzard(player)) {
                int currentWarmth = playerWarmth.getOrDefault(playerUuid, 100);
                playerWarmth.put(playerUuid, Math.max(0, currentWarmth - 2));

                int warmth = playerWarmth.get(playerUuid);

                if (warmth < 20) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    player.damage(0.5);
                    player.sendMessage("§b[Blizzard] §fVous gelez ! Trouvez de la chaleur rapidement !");
                } else if (warmth < 50) {
                    // Cold - moderate effects
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    player.sendMessage("§b[Blizzard] §fVous avez très froid !");
                } else if (warmth < 80) {
                    // Chilly - light effects
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 0));
                    if (random.nextInt(3) == 0) {
                        player.sendMessage("§b[Blizzard] §fVous commencez à avoir froid...");
                    }
                }
            } else {
                int currentWarmth = playerWarmth.getOrDefault(playerUuid, 100);
                if (isPlayerNearHeatSource(player)) {
                    playerWarmth.put(playerUuid, Math.min(100, currentWarmth + 5));
                } else {
                    playerWarmth.put(playerUuid, Math.min(100, currentWarmth + 1));
                }
            }
        }
    }

    private void updatePlayerWarmth() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            UUID playerUuid = player.getUniqueId();

            if (!isBlizzardActive) {
                int currentWarmth = playerWarmth.getOrDefault(playerUuid, 100);
                if (isPlayerNearHeatSource(player)) {
                    playerWarmth.put(playerUuid, Math.min(100, currentWarmth + 3));
                } else {
                    playerWarmth.put(playerUuid, Math.min(100, currentWarmth + 1));
                }
            }
        }
    }

    private boolean isPlayerExposedToBlizzard(Player player) {
        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();

        if (!world.hasStorm()) {
            return false;
        }

        if (playerLoc.getY() < 50) {
            return false;
        }

        Location checkLoc = playerLoc.clone();

        for (int y = 1; y <= 5; y++) {
            checkLoc.add(0, 1, 0);
            Material blockType = checkLoc.getBlock().getType();

            if (blockType != Material.AIR &&
                    blockType != Material.WATER &&
                    blockType != Material.LAVA &&
                    !blockType.name().contains("LEAVES")) {
                return false;
            }
        }

        return true;
    }

    private boolean isPlayerNearHeatSource(Player player) {
        Location playerLoc = player.getLocation();

        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location checkLoc = playerLoc.clone().add(x, y, z);
                    Material blockType = checkLoc.getBlock().getType();

                    if (blockType == Material.FIRE ||
                            blockType == Material.LAVA ||
                            blockType == Material.BURNING_FURNACE ||
                            blockType == Material.TORCH ||
                            blockType == Material.GLOWSTONE) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void initializePlayerWarmth() {
        playerWarmth.clear();
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            playerWarmth.put(uhcPlayer.getPlayer().getUniqueId(), 100);
        }
    }

    private void clearPlayerEffects() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
        playerWarmth.clear();
    }

}
