package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.PotentialPermanentLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
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

public class PotentialPermanent extends Scenario {

    private final Map<UUID, Double> playerPermanentHealth = new HashMap<>();
    private final Map<UUID, Double> playerAbsorptionHealth = new HashMap<>();
    private final Map<UUID, BukkitRunnable> absorptionTasks = new HashMap<>();



    @Override
    public String getName() {
        return "PotentialPermanent";
    }

    @Override
    public String getDescription() {
        return "Commencez avec 10 cœurs + 10 d'absorption qui peuvent devenir permanents !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }

    @Override
    public String getPath() {
        return "potentialpermanent";
    }

    @Override
    public ScenarioLang[] getLang() {
        return PotentialPermanentLang.values();
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        UUID playerUuid = player.getUniqueId();

        // Initialize player health values based on config
        double startingPermanent = getConfig().getDouble("starting_permanent_health", 20.0);
        double startingAbsorption = getConfig().getDouble("starting_absorption_health", 20.0);

        playerPermanentHealth.put(playerUuid, startingPermanent);
        playerAbsorptionHealth.put(playerUuid, startingAbsorption);

        // Set player's max health and current health
        player.setMaxHealth(startingPermanent);
        player.setHealth(startingPermanent);

        // Give absorption hearts
        int absorptionLevel = (int) Math.ceil(startingAbsorption / 4.0) - 1;
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, absorptionLevel));

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%permanent_hearts%", String.valueOf(startingPermanent / 2));
        placeholders.put("%absorption_hearts%", String.valueOf(startingAbsorption / 2));
        ScenarioLangManager.send(uhcPlayer, PotentialPermanentLang.STARTING_HEALTH, placeholders);
        ScenarioLangManager.send(uhcPlayer, PotentialPermanentLang.CONVERSION_INFO);

        startAbsorptionMonitoring(player);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;

        UUID deadPlayerUuid = uhcPlayer.getPlayer().getUniqueId();

        // Clean up dead player's data
        cleanupPlayer(deadPlayerUuid);

        if (killer != null) {
            Player killerPlayer = killer.getPlayer();
            UUID killerUuid = killerPlayer.getUniqueId();

            // Reward killer with permanent health
            double currentPermanent = playerPermanentHealth.getOrDefault(killerUuid, getConfig().getDouble("starting_permanent_health", 20.0));
            double currentAbsorption = playerAbsorptionHealth.getOrDefault(killerUuid, 0.0);

            // Convert some absorption to permanent health
            double absorptionToConvert = Math.min(getConfig().getDouble("kill_reward", 4.0), currentAbsorption);
            double newPermanent = currentPermanent + absorptionToConvert;
            double newAbsorption = currentAbsorption - absorptionToConvert;

            // Update values
            playerPermanentHealth.put(killerUuid, newPermanent);
            playerAbsorptionHealth.put(killerUuid, newAbsorption);

            // Apply to player
            killerPlayer.setMaxHealth(newPermanent);
            killerPlayer.setHealth(Math.min(killerPlayer.getHealth(), newPermanent));

            // Update absorption effect
            updateAbsorptionEffect(killerPlayer, newAbsorption);

            // Send messages
            killerPlayer.sendMessage("§e[PotentialPermanent] §fKill ! " + (absorptionToConvert / 2) +
                    " cœur(s) d'absorption convertis en vie permanente !");
            killerPlayer.sendMessage("§e[PotentialPermanent] §fVie permanente : " + (newPermanent / 2) +
                    " cœurs | Absorption : " + (newAbsorption / 2) + " cœurs");

            // Broadcast
            Bukkit.broadcastMessage("§e[PotentialPermanent] §f" + killerPlayer.getName() +
                    " a maintenant " + (newPermanent / 2) + " cœurs permanents !");
        }
    }

    private void startAbsorptionMonitoring(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Cancel existing task if any
        BukkitRunnable existingTask = absorptionTasks.get(playerUuid);
        if (existingTask != null) {
            existingTask.cancel();
        }

        BukkitRunnable monitorTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isActive()) {
                    cancel();
                    absorptionTasks.remove(playerUuid);
                    return;
                }

                // Monitor absorption hearts and update our tracking
                double expectedAbsorption = playerAbsorptionHealth.getOrDefault(playerUuid, 0.0);

                // Check if player lost absorption (took damage)
                if (!player.hasPotionEffect(PotionEffectType.ABSORPTION) && expectedAbsorption > 0) {
                    // Player lost all absorption
                    playerAbsorptionHealth.put(playerUuid, 0.0);
                    player.sendMessage("§e[PotentialPermanent] §cVous avez perdu toute votre absorption !");
                }
            }
        };

        absorptionTasks.put(playerUuid, monitorTask);
        monitorTask.runTaskTimer(Main.get(), 0, 20); // Check every second
    }

    private void updateAbsorptionEffect(Player player, double absorptionHealth) {
        // Remove existing absorption effect
        player.removePotionEffect(PotionEffectType.ABSORPTION);

        if (absorptionHealth > 0) {
            // Calculate absorption level (each level gives 2 hearts)
            int absorptionLevel = (int) Math.ceil(absorptionHealth / 4.0) - 1; // -1 because level 0 = 2 hearts
            absorptionLevel = Math.max(0, Math.min(absorptionLevel, 19)); // Max level 19 (40 hearts)

            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, absorptionLevel));
        }
    }

    private void cleanupPlayer(UUID playerUuid) {
        playerPermanentHealth.remove(playerUuid);
        playerAbsorptionHealth.remove(playerUuid);

        BukkitRunnable task = absorptionTasks.remove(playerUuid);
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    public void toggleActive() {
        super.toggleActive();

        if (!isActive()) {
            // Clean up all tasks and reset players
            for (BukkitRunnable task : absorptionTasks.values()) {
                task.cancel();
            }
            absorptionTasks.clear();

            // Reset all players to normal health
            for (UUID playerUuid : playerPermanentHealth.keySet()) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null && player.isOnline()) {
                    player.setMaxHealth(20.0);
                    player.setHealth(Math.min(player.getHealth(), 20.0));
                    player.removePotionEffect(PotionEffectType.ABSORPTION);
                }
            }

            playerPermanentHealth.clear();
            playerAbsorptionHealth.clear();
        }
    }

    // Get player's permanent health
    public double getPlayerPermanentHealth(Player player) {
        return playerPermanentHealth.getOrDefault(player.getUniqueId(), getConfig().getDouble("starting_permanent_health", 20.0));
    }

    // Get player's absorption health
    public double getPlayerAbsorptionHealth(Player player) {
        return playerAbsorptionHealth.getOrDefault(player.getUniqueId(), 0.0);
    }

    // Get player's total potential health
    public double getPlayerTotalHealth(Player player) {
        return getPlayerPermanentHealth(player) + getPlayerAbsorptionHealth(player);
    }

    // Admin command to set player's permanent health
    public void setPlayerPermanentHealth(Player player, double health) {
        if (!isActive()) return;

        UUID playerUuid = player.getUniqueId();
        health = Math.max(2.0, Math.min(health, 60.0)); // Between 1 and 30 hearts

        playerPermanentHealth.put(playerUuid, health);
        player.setMaxHealth(health);
        player.setHealth(Math.min(player.getHealth(), health));

        player.sendMessage("§e[PotentialPermanent] §fVie permanente définie à " + (health / 2) + " cœurs !");
    }

    // Admin command to set player's absorption health
    public void setPlayerAbsorptionHealth(Player player, double absorption) {
        if (!isActive()) return;

        UUID playerUuid = player.getUniqueId();
        absorption = Math.max(0.0, Math.min(absorption, 40.0)); // Between 0 and 20 hearts

        playerAbsorptionHealth.put(playerUuid, absorption);
        updateAbsorptionEffect(player, absorption);

        player.sendMessage("§e[PotentialPermanent] §fAbsorption définie à " + (absorption / 2) + " cœurs !");
    }

    // Convert absorption to permanent health (admin command)
    public void convertAbsorptionToPermanent(Player player, double amount) {
        if (!isActive()) return;

        UUID playerUuid = player.getUniqueId();
        double currentPermanent = playerPermanentHealth.getOrDefault(playerUuid, getConfig().getDouble("starting_permanent_health", 20.0));
        double currentAbsorption = playerAbsorptionHealth.getOrDefault(playerUuid, 0.0);

        double toConvert = Math.min(amount, currentAbsorption);
        if (toConvert <= 0) {
            player.sendMessage("§e[PotentialPermanent] §cPas assez d'absorption à convertir !");
            return;
        }

        double newPermanent = Math.min(60.0, currentPermanent + toConvert); // Max 30 hearts
        double actualConverted = newPermanent - currentPermanent;
        double newAbsorption = currentAbsorption - actualConverted;

        playerPermanentHealth.put(playerUuid, newPermanent);
        playerAbsorptionHealth.put(playerUuid, newAbsorption);

        player.setMaxHealth(newPermanent);
        player.setHealth(Math.min(player.getHealth(), newPermanent));
        updateAbsorptionEffect(player, newAbsorption);

        player.sendMessage("§e[PotentialPermanent] §f" + (actualConverted / 2) +
                " cœur(s) d'absorption convertis en vie permanente !");
    }

    // Get health status string for a player
    public String getHealthStatus(Player player) {
        double permanent = getPlayerPermanentHealth(player) / 2;
        double absorption = getPlayerAbsorptionHealth(player) / 2;
        double total = permanent + absorption;

        return "§e[PotentialPermanent] §f" + player.getName() + " : " +
                "§c" + permanent + "♥ §epermanents §f+ §6" + absorption + "♥ §eabsorption §f= §a" + total + "♥ §etotal";
    }
}
