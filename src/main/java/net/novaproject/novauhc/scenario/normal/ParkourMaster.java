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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ParkourMaster extends Scenario {

    private final Map<UUID, ParkourChallenge> activeChallenges = new HashMap<>();
    private final List<ParkourReward> rewards = new ArrayList<>();
    private final Random random = new Random();
    private BukkitRunnable parkourTask;

    public ParkourMaster() {
        initializeRewards();
    }

    @Override
    public String getName() {
        return "ParkourMaster";
    }

    @Override
    public String getDescription() {
        return "Des parcours apparaissent aléatoirement. Les compléter donne des récompenses !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FEATHER);
    }


    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startParkourTask();
        } else {
            stopParkourTask();
            clearAllChallenges();
        }
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (!isActive()) return;

        UUID playerUuid = player.getUniqueId();
        ParkourChallenge challenge = activeChallenges.get(playerUuid);

        if (challenge != null) {
            Location playerLoc = player.getLocation();

            if (challenge.isAtCheckpoint(playerLoc)) {
                challenge.nextCheckpoint();

                if (challenge.isCompleted()) {
                    completeParkour(player, challenge);
                } else {
                    player.sendMessage("§a[ParkourMaster] §fCheckpoint " + challenge.getCurrentCheckpoint() +
                            "/" + challenge.getTotalCheckpoints() + " atteint !");
                    player.getWorld().playSound(playerLoc, org.bukkit.Sound.ORB_PICKUP, 1.0f, 1.5f);
                }
            }

            // Check if player fell or went too far
            if (challenge.isPlayerOutOfBounds(playerLoc)) {
                failParkour(player, challenge);
            }
        }
    }

    private void startParkourTask() {
        if (parkourTask != null) {
            parkourTask.cancel();
        }

        parkourTask = new BukkitRunnable() {
            private int timer = 0;

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                timer++;

                // Spawn new parkour every 3-5 minutes
                if (timer >= 180 + random.nextInt(120)) {
                    spawnRandomParkour();
                    timer = 0;
                }

                // Update existing challenges
                updateChallenges();
            }
        };

        // Run every second
        parkourTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void stopParkourTask() {
        if (parkourTask != null) {
            parkourTask.cancel();
            parkourTask = null;
        }
    }

    private void spawnRandomParkour() {
        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        if (playingPlayers.isEmpty()) return;

        UHCPlayer targetPlayer = playingPlayers.get(random.nextInt(playingPlayers.size()));
        Player player = targetPlayer.getPlayer();

        // Don't spawn if player already has a challenge
        if (activeChallenges.containsKey(player.getUniqueId())) {
            return;
        }

        Location spawnLocation = findSuitableSpawnLocation(player.getLocation());
        if (spawnLocation != null) {
            ParkourChallenge challenge = createParkourChallenge(spawnLocation);
            activeChallenges.put(player.getUniqueId(), challenge);

            player.sendMessage("§a[ParkourMaster] §fUn parcours est apparu près de vous ! Complétez-le pour une récompense !");
            Bukkit.broadcastMessage("§a[ParkourMaster] §fUn parcours est apparu près de " + player.getName() + " !");

            // Build the parkour
            buildParkour(challenge);
        }
    }

    private Location findSuitableSpawnLocation(Location playerLoc) {
        World world = playerLoc.getWorld();

        // Try to find a suitable location within 50 blocks
        for (int attempts = 0; attempts < 10; attempts++) {
            int x = playerLoc.getBlockX() + random.nextInt(100) - 50;
            int z = playerLoc.getBlockZ() + random.nextInt(100) - 50;
            int y = world.getHighestBlockYAt(x, z) + 1;

            Location testLoc = new Location(world, x, y, z);

            // Check if location is suitable (not in water, lava, etc.)
            if (testLoc.getBlock().getType() == Material.AIR &&
                    testLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid()) {
                return testLoc;
            }
        }

        return null;
    }

    private ParkourChallenge createParkourChallenge(Location startLoc) {
        List<Location> checkpoints = new ArrayList<>();
        checkpoints.add(startLoc.clone());

        // Generate 3-5 checkpoints
        int numCheckpoints = 3 + random.nextInt(3);
        Location currentLoc = startLoc.clone();

        for (int i = 0; i < numCheckpoints; i++) {
            // Next checkpoint 5-10 blocks away
            int distance = 5 + random.nextInt(6);
            double angle = random.nextDouble() * 2 * Math.PI;

            int deltaX = (int) (Math.cos(angle) * distance);
            int deltaZ = (int) (Math.sin(angle) * distance);
            int deltaY = random.nextInt(5) - 2; // Can go up or down

            currentLoc = currentLoc.clone().add(deltaX, deltaY, deltaZ);
            checkpoints.add(currentLoc.clone());
        }

        return new ParkourChallenge(checkpoints, System.currentTimeMillis() + 300000); // 5 minute timeout
    }

    private void buildParkour(ParkourChallenge challenge) {
        List<Location> checkpoints = challenge.getCheckpoints();

        for (int i = 0; i < checkpoints.size(); i++) {
            Location checkpoint = checkpoints.get(i);

            // Place checkpoint block
            if (i == 0) {
                // Start block (green wool)
                checkpoint.getBlock().setType(Material.WOOL);
                checkpoint.getBlock().setData((byte) 5); // Green wool
            } else if (i == checkpoints.size() - 1) {
                // End block (red wool)
                checkpoint.getBlock().setType(Material.WOOL);
                checkpoint.getBlock().setData((byte) 14); // Red wool
            } else {
                // Checkpoint block (yellow wool)
                checkpoint.getBlock().setType(Material.WOOL);
                checkpoint.getBlock().setData((byte) 4); // Yellow wool
            }

            // Add some platform blocks around checkpoint
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && z == 0) continue; // Skip center block

                    Location platformLoc = checkpoint.clone().add(x, -1, z);
                    if (platformLoc.getBlock().getType() == Material.AIR) {
                        platformLoc.getBlock().setType(Material.STONE);
                    }
                }
            }
        }
    }

    private void completeParkour(Player player, ParkourChallenge challenge) {
        activeChallenges.remove(player.getUniqueId());

        // Give reward
        ParkourReward reward = getRandomReward();
        giveReward(player, reward);

        // Clean up parkour
        cleanupParkour(challenge);

        // Announce completion
        player.sendMessage("§a§l[ParkourMaster] §fParcours complété ! Récompense : " + reward.getDescription());
        Bukkit.broadcastMessage("§a[ParkourMaster] §f" + player.getName() + " a complété un parcours !");

        // Effects
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.LEVEL_UP, 1.0f, 1.0f);
    }

    private void failParkour(Player player, ParkourChallenge challenge) {
        activeChallenges.remove(player.getUniqueId());

        player.sendMessage("§c[ParkourMaster] §fVous avez échoué au parcours ! Réessayez la prochaine fois.");

        // Clean up parkour after a delay
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupParkour(challenge);
            }
        }.runTaskLater(Main.get(), 100); // 5 second delay
    }

    private void cleanupParkour(ParkourChallenge challenge) {
        for (Location checkpoint : challenge.getCheckpoints()) {
            // Remove checkpoint blocks
            checkpoint.getBlock().setType(Material.AIR);

            // Remove platform blocks
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    Location platformLoc = checkpoint.clone().add(x, -1, z);
                    if (platformLoc.getBlock().getType() == Material.STONE) {
                        platformLoc.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
    }

    private void updateChallenges() {
        Iterator<Map.Entry<UUID, ParkourChallenge>> iterator = activeChallenges.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<UUID, ParkourChallenge> entry = iterator.next();
            ParkourChallenge challenge = entry.getValue();

            if (challenge.isExpired()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    player.sendMessage("§c[ParkourMaster] §fLe parcours a expiré !");
                }

                cleanupParkour(challenge);
                iterator.remove();
            }
        }
    }

    private void clearAllChallenges() {
        for (ParkourChallenge challenge : activeChallenges.values()) {
            cleanupParkour(challenge);
        }
        activeChallenges.clear();
    }

    private void initializeRewards() {
        rewards.add(new ParkourReward("2 Pommes d'Or", new ItemStack(Material.GOLDEN_APPLE, 2)));
        rewards.add(new ParkourReward("16 Flèches", new ItemStack(Material.ARROW, 16)));
        rewards.add(new ParkourReward("4 Perles d'Ender", new ItemStack(Material.ENDER_PEARL, 4)));
        rewards.add(new ParkourReward("8 Lingots de Fer", new ItemStack(Material.IRON_INGOT, 8)));
        rewards.add(new ParkourReward("2 Diamants", new ItemStack(Material.DIAMOND, 2)));
        rewards.add(new ParkourReward("1 Livre d'Enchantement", new ItemStack(Material.ENCHANTED_BOOK, 1)));
    }

    private ParkourReward getRandomReward() {
        return rewards.get(random.nextInt(rewards.size()));
    }

    private void giveReward(Player player, ParkourReward reward) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(reward.getItem());
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), reward.getItem());
            player.sendMessage("§a[ParkourMaster] §fInventaire plein ! Récompense jetée au sol.");
        }
    }

    // Inner classes
    private static class ParkourChallenge {
        private final List<Location> checkpoints;
        private final long expirationTime;
        private int currentCheckpoint = 0;

        public ParkourChallenge(List<Location> checkpoints, long expirationTime) {
            this.checkpoints = checkpoints;
            this.expirationTime = expirationTime;
        }

        public List<Location> getCheckpoints() {
            return checkpoints;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }

        public int getCurrentCheckpoint() {
            return currentCheckpoint + 1;
        }

        public int getTotalCheckpoints() {
            return checkpoints.size();
        }

        public boolean isCompleted() {
            return currentCheckpoint >= checkpoints.size();
        }

        public boolean isAtCheckpoint(Location playerLoc) {
            if (currentCheckpoint >= checkpoints.size()) return false;

            Location checkpoint = checkpoints.get(currentCheckpoint);
            return playerLoc.distance(checkpoint) <= 2.0;
        }

        public void nextCheckpoint() {
            currentCheckpoint++;
        }

        public boolean isPlayerOutOfBounds(Location playerLoc) {
            if (currentCheckpoint >= checkpoints.size()) return false;

            Location checkpoint = checkpoints.get(currentCheckpoint);
            return playerLoc.distance(checkpoint) > 50.0 || playerLoc.getY() < checkpoint.getY() - 10;
        }
    }

    private static class ParkourReward {
        private final String description;
        private final ItemStack item;

        public ParkourReward(String description, ItemStack item) {
            this.description = description;
            this.item = item;
        }

        public String getDescription() {
            return description;
        }

        public ItemStack getItem() {
            return item.clone();
        }
    }
}
