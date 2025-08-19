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
    public void enable() {
        super.enable();
        if (isActive()) {
            startBlizzardCycle();
            initializePlayerWarmth();
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startBlizzardCycle();
            initializePlayerWarmth();
        } else {
            stopBlizzardCycle();
            clearPlayerEffects();
        }
    }

    private void startBlizzardCycle() {
        if (blizzardTask != null) {
            blizzardTask.cancel();
        }

        blizzardTask = new BukkitRunnable() {
            private int cycleTimer = 0;
            private int blizzardDuration = 0;
            private int nextBlizzardIn = 240 + random.nextInt(240); // 4-8 minutes until first blizzard

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                cycleTimer++;

                if (!isBlizzardActive) {
                    // Waiting for next blizzard
                    if (cycleTimer >= nextBlizzardIn) {
                        startBlizzard();
                        cycleTimer = 0;
                        blizzardDuration = 90 + random.nextInt(90); // Blizzard for 1.5-3 minutes
                    } else {
                        // Warning messages
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
                        nextBlizzardIn = 240 + random.nextInt(240); // Next blizzard in 4-8 minutes
                    } else {
                        // Warning messages during blizzard
                        if (blizzardDuration == 10) {
                            Bukkit.broadcastMessage("§b[Blizzard] §fLa tempête se calme dans 10 secondes !");
                        }
                    }
                }

                // Update player warmth regardless of blizzard state
                updatePlayerWarmth();
            }
        };

        // Run every second
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

        // Set weather to snow in all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(true);
            world.setWeatherDuration(Integer.MAX_VALUE);
        }

        Bukkit.broadcastMessage("§b§l[Blizzard] §fUne tempête de neige commence ! Trouvez de la chaleur !");

        // Play wind sound for atmosphere
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.AMBIENCE_CAVE, 0.5f, 0.8f);
        }
    }

    private void stopBlizzard() {
        isBlizzardActive = false;

        // Stop snow in all worlds
        for (World world : Bukkit.getWorlds()) {
            world.setStorm(false);
            world.setWeatherDuration(0);
        }

        // Remove blizzard effects from all players
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
                // Reduce warmth
                int currentWarmth = playerWarmth.getOrDefault(playerUuid, 100);
                playerWarmth.put(playerUuid, Math.max(0, currentWarmth - 2));

                // Apply effects based on warmth level
                int warmth = playerWarmth.get(playerUuid);

                if (warmth < 20) {
                    // Very cold - severe effects
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
                // Player is sheltered, slowly regain warmth
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
                // Slowly regain warmth when no blizzard
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

        // Check if it's snowing in this world
        if (!world.hasStorm()) {
            return false;
        }

        // Check if player is underground (below Y=50)
        if (playerLoc.getY() < 50) {
            return false;
        }

        // Check if player has blocks above them (shelter)
        Location checkLoc = playerLoc.clone();

        // Check up to 5 blocks above the player
        for (int y = 1; y <= 5; y++) {
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

        // Player is exposed to blizzard
        return true;
    }

    private boolean isPlayerNearHeatSource(Player player) {
        Location playerLoc = player.getLocation();

        // Check for heat sources in a 3x3x3 area around the player
        for (int x = -3; x <= 3; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -3; z <= 3; z++) {
                    Location checkLoc = playerLoc.clone().add(x, y, z);
                    Material blockType = checkLoc.getBlock().getType();

                    // Heat sources
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

    // Get player warmth level
    public int getPlayerWarmth(Player player) {
        return playerWarmth.getOrDefault(player.getUniqueId(), 100);
    }

    // Check if blizzard is currently active
    public boolean isBlizzardActive() {
        return isBlizzardActive;
    }

    // Force start blizzard (admin command)
    public void forceStartBlizzard() {
        if (isActive() && !isBlizzardActive) {
            startBlizzard();
            Bukkit.broadcastMessage("§b[Blizzard] §fTempête forcée par un administrateur !");
        }
    }

    // Force stop blizzard (admin command)
    public void forceStopBlizzard() {
        if (isActive() && isBlizzardActive) {
            stopBlizzard();
            Bukkit.broadcastMessage("§b[Blizzard] §fTempête arrêtée par un administrateur !");
        }
    }
}
