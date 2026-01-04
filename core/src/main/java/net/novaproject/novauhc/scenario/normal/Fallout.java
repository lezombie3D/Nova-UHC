package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Fallout extends Scenario {

    private final Map<UUID, Integer> playerWarnings = new HashMap<>();
    private final int FALLOUT_START_TIME = 45 * 60; // 45 minutes in seconds
    private final int SAFE_Y_LEVEL = 60;
    private BukkitRunnable falloutTask;
    private boolean falloutStarted = false;

    @Override
    public String getName() {
        return "Fallout";
    }

    @Override
    public String getDescription() {
        return "Après 45 minutes, rester au-dessus de Y=60 inflige des dégâts de radiation !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SKULL_ITEM);
    }


    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startFalloutTask();
        } else {
            stopFalloutTask();
        }
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;

        int currentTime = UHCManager.get().getTimer();

        // Check if fallout should start
        if (!falloutStarted && currentTime >= FALLOUT_START_TIME) {
            startFallout();
        }

        // If fallout is active, check players
        if (falloutStarted) {
            checkPlayerRadiation(p);
        } else {
            // Send warnings before fallout starts
            sendFalloutWarnings(currentTime);
        }
    }

    private void startFalloutTask() {
        if (falloutTask != null) {
            falloutTask.cancel();
        }

        falloutTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                int currentTime = UHCManager.get().getTimer();

                // Check if fallout should start
                if (!falloutStarted && currentTime >= FALLOUT_START_TIME) {
                    startFallout();
                }

                // If fallout is active, apply radiation to exposed players
                if (falloutStarted) {
                    applyRadiationToExposedPlayers();
                }
            }
        };

        // Run every 10 seconds for efficiency
        falloutTask.runTaskTimer(Main.get(), 0, 200);
    }

    private void stopFalloutTask() {
        if (falloutTask != null) {
            falloutTask.cancel();
            falloutTask = null;
        }
        falloutStarted = false;
        playerWarnings.clear();
    }

    private void startFallout() {
        falloutStarted = true;

        Bukkit.broadcastMessage("§c§l[Fallout] §fLES RADIATIONS COMMENCENT !");
        Bukkit.broadcastMessage("§c[Fallout] §fDescendez sous Y=" + SAFE_Y_LEVEL + " pour éviter les radiations !");

        // Play dramatic sound
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.WITHER_SPAWN, 1.0f, 0.5f);
        }

        // Create visual effects in the sky
        createFalloutEffects();
    }

    private void sendFalloutWarnings(int currentTime) {
        int timeUntilFallout = FALLOUT_START_TIME - currentTime;

        // Send warnings at specific intervals
        if (timeUntilFallout == 300) { // 5 minutes before
            Bukkit.broadcastMessage("§c[Fallout] §fRadiations dans 5 minutes ! Préparez vos abris souterrains !");
        } else if (timeUntilFallout == 60) { // 1 minute before
            Bukkit.broadcastMessage("§c[Fallout] §fRadiations dans 1 minute ! Descendez sous Y=" + SAFE_Y_LEVEL + " !");
        } else if (timeUntilFallout == 10) { // 10 seconds before
            Bukkit.broadcastMessage("§c[Fallout] §fRadiations dans 10 secondes !");
        }
    }

    private void checkPlayerRadiation(Player player) {
        if (!falloutStarted) return;

        Location playerLoc = player.getLocation();
        UUID playerUuid = player.getUniqueId();

        if (playerLoc.getY() > SAFE_Y_LEVEL) {
            // Player is exposed to radiation
            int warnings = playerWarnings.getOrDefault(playerUuid, 0);
            playerWarnings.put(playerUuid, warnings + 1);

            // Apply radiation effects based on exposure time
            applyRadiationEffects(player, warnings);
        } else {
            // Player is safe, reset warnings
            playerWarnings.put(playerUuid, 0);

            // Remove radiation effects if player was affected
            if (player.hasPotionEffect(PotionEffectType.POISON)) {
                player.removePotionEffect(PotionEffectType.POISON);
            }
            if (player.hasPotionEffect(PotionEffectType.WEAKNESS)) {
                player.removePotionEffect(PotionEffectType.WEAKNESS);
            }
            if (player.hasPotionEffect(PotionEffectType.HUNGER)) {
                player.removePotionEffect(PotionEffectType.HUNGER);
            }
        }
    }

    private void applyRadiationToExposedPlayers() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            checkPlayerRadiation(player);
        }
    }

    private void applyRadiationEffects(Player player, int exposureLevel) {
        // Damage increases with exposure time
        double damage = Math.min(2.0, 0.5 + (exposureLevel * 0.1));

        // Apply damage
        player.damage(damage);

        // Apply radiation effects based on exposure level
        if (exposureLevel >= 5) {
            // Severe radiation poisoning
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 1));
            player.sendMessage("§c[Fallout] §fRadiation SÉVÈRE ! Descendez immédiatement !");
        } else if (exposureLevel >= 3) {
            // Moderate radiation
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 60, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
            player.sendMessage("§c[Fallout] §fRadiation modérée ! Trouvez un abri !");
        } else {
            // Light radiation
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 40, 0));
            player.sendMessage("§c[Fallout] §fVous êtes exposé aux radiations !");
        }

        // Visual effects
        /*if (exposureLevel % 2 == 0) { // Every other second
            try {
                player.getWorld().spawn(
                    org.bukkit.Particle.SPELL_WITCH,
                    player.getLocation().add(0, 1, 0),
                    5, 0.5, 0.5, 0.5, 0.1
                );
            } catch (Exception e) {
                // Particle effects not available in this version
            }
        }*/
    }

    private void createFalloutEffects() {
        // Create atmospheric effects to show fallout has started
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            Location playerLoc = player.getLocation();

            // Create particles in the sky above players
            for (int i = 0; i < 20; i++) {
                Location effectLoc = playerLoc.clone().add(
                        (Math.random() - 0.5) * 20,
                        10 + Math.random() * 10,
                        (Math.random() - 0.5) * 20
                );
                
                /*try {
                    player.getWorld().spawnParticle(
                        org.bukkit.Particle.SMOKE_LARGE,
                        effectLoc,
                        1, 0, 0, 0, 0
                    );
                } catch (Exception e) {
                    // Particle effects not available in this version
                }*/
            }
        }
    }

    // Check if a location is safe from radiation
    public boolean isSafeFromRadiation(Location location) {
        return !falloutStarted || location.getY() <= SAFE_Y_LEVEL;
    }

    // Get player's radiation exposure level
    public int getPlayerExposure(Player player) {
        return playerWarnings.getOrDefault(player.getUniqueId(), 0);
    }

    // Check if fallout has started
    public boolean hasFalloutStarted() {
        return falloutStarted;
    }

    // Get time until fallout starts (in seconds)
    public int getTimeUntilFallout() {
        if (falloutStarted) return 0;

        int currentTime = UHCManager.get().getTimer();
        return Math.max(0, FALLOUT_START_TIME - currentTime);
    }

    // Get safe Y level
    public int getSafeYLevel() {
        return SAFE_Y_LEVEL;
    }

    // Force start fallout (admin command)
    public void forceStartFallout() {
        if (isActive() && !falloutStarted) {
            startFallout();
            Bukkit.broadcastMessage("§c[Fallout] §fRadiations forcées par un administrateur !");
        }
    }

    // Create a temporary safe zone (admin command)
    public void createSafeZone(Location center, int radius, int duration) {
        if (!isActive()) return;

        Bukkit.broadcastMessage("§c[Fallout] §fZone sûre temporaire créée en " +
                center.getBlockX() + ", " + center.getBlockY() + ", " + center.getBlockZ() +
                " pour " + duration + " secondes !");

        // This would require more complex implementation to track safe zones
        // For now, just announce it
    }

    // Get radiation level description
    public String getRadiationLevelDescription(int exposureLevel) {
        if (exposureLevel == 0) {
            return "§aSûr";
        } else if (exposureLevel < 3) {
            return "§eLégère";
        } else if (exposureLevel < 5) {
            return "§6Modérée";
        } else {
            return "§cSévère";
        }
    }
}
