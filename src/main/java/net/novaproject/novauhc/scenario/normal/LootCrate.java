package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.LootCrateLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LootCrate extends Scenario {

    private final List<Location> activeCrates = new ArrayList<>();
    private final Random random = new Random();
    private BukkitRunnable lootCrateTask;

    @Override
    public String getName() {
        return "LootCrate";
    }

    @Override
    public String getDescription() {
        return "Coffres de loot distribués toutes les 10 minutes avec des objets précieux !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CHEST);
    }

    @Override
    public String getPath() {
        return "lootcrate";
    }

    @Override
    public ScenarioLang[] getLang() {
        return LootCrateLang.values();
    }


    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            startLootCrateTask();
        } else {
            stopLootCrateTask();
            removeAllCrates();
        }
    }

    private void startLootCrateTask() {
        if (lootCrateTask != null) {
            lootCrateTask.cancel();
        }

        lootCrateTask = new BukkitRunnable() {
            private int timer = 0;

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                timer++;

                int spawnInterval = getConfig().getInt("spawn_interval", 600);
                if (timer >= spawnInterval) {
                    spawnLootCrates();
                    timer = 0;
                }

                // Send warnings
                int timeUntilNext = spawnInterval - timer;
                if (timeUntilNext == 60) { // 1 minute before
                    ScenarioLangManager.sendAll(LootCrateLang.CRATES_WARNING_1MIN);
                } else if (timeUntilNext == 10) { // 10 seconds before
                    ScenarioLangManager.sendAll(LootCrateLang.CRATES_WARNING_10SEC);
                }
            }
        };

        // Run every second
        lootCrateTask.runTaskTimer(Main.get(), 0, 20);
    }

    private void stopLootCrateTask() {
        if (lootCrateTask != null) {
            lootCrateTask.cancel();
            lootCrateTask = null;
        }
    }

    private void spawnLootCrates() {
        // Remove old crates first
        removeAllCrates();

        List<UHCPlayer> playingPlayers = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        if (playingPlayers.isEmpty()) return;

        // Spawn crates based on config
        int minCrates = getConfig().getInt("min_crates", 3);
        int maxCrates = getConfig().getInt("max_crates", 5);
        int crateCount = minCrates + random.nextInt(maxCrates - minCrates + 1);

        for (int i = 0; i < crateCount; i++) {
            Location crateLocation = findSuitableCrateLocation();
            if (crateLocation != null) {
                spawnLootCrate(crateLocation);
            }
        }

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%count%", String.valueOf(activeCrates.size()));
        ScenarioLangManager.sendAll(LootCrateLang.CRATES_SPAWNED, placeholders);
        ScenarioLangManager.sendAll(LootCrateLang.CRATES_ANNOUNCEMENT);

        // Play sound to all players
        for (UHCPlayer uhcPlayer : playingPlayers) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENDERDRAGON_GROWL, 1.0f, 1.5f);
        }
    }

    private Location findSuitableCrateLocation() {
        World world = Bukkit.getWorld("world");
        if (world == null) return null;

        // Try to find a suitable location
        for (int attempts = 0; attempts < 20; attempts++) {
            int x = random.nextInt(2000) - 1000; // -1000 to 1000
            int z = random.nextInt(2000) - 1000; // -1000 to 1000
            int y = world.getHighestBlockYAt(x, z) + 1;

            Location testLoc = new Location(world, x, y, z);

            // Check if location is suitable
            if (testLoc.getBlock().getType() == Material.AIR &&
                    testLoc.clone().subtract(0, 1, 0).getBlock().getType().isSolid() &&
                    !testLoc.clone().subtract(0, 1, 0).getBlock().getType().equals(Material.LAVA) &&
                    !testLoc.clone().subtract(0, 1, 0).getBlock().getType().equals(Material.WATER)) {
                return testLoc;
            }
        }

        return null;
    }

    private void spawnLootCrate(Location location) {
        // Place chest
        Block chestBlock = location.getBlock();
        chestBlock.setType(Material.CHEST);

        // Fill chest with loot
        Chest chest = (Chest) chestBlock.getState();
        Inventory inventory = chest.getInventory();

        fillChestWithLoot(inventory);

        // Add to active crates list
        activeCrates.add(location);

        // Create beacon beam effect (if possible)
        createBeaconEffect(location);

        // Announce location to nearby players
        announceNearbyPlayers(location);
    }

    private void fillChestWithLoot(Inventory inventory) {
        List<ItemStack> possibleLoot = Arrays.asList(
                // Weapons and Tools
                createEnchantedItem(Material.DIAMOND_SWORD, Enchantment.DAMAGE_ALL, 2),
                createEnchantedItem(Material.DIAMOND_PICKAXE, Enchantment.DIG_SPEED, 3),
                createEnchantedItem(Material.BOW, Enchantment.ARROW_DAMAGE, 3),
                createEnchantedItem(Material.DIAMOND_AXE, Enchantment.DIG_SPEED, 2),

                // Armor
                createEnchantedItem(Material.DIAMOND_HELMET, Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                createEnchantedItem(Material.DIAMOND_CHESTPLATE, Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                createEnchantedItem(Material.DIAMOND_LEGGINGS, Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                createEnchantedItem(Material.DIAMOND_BOOTS, Enchantment.PROTECTION_ENVIRONMENTAL, 2),

                // Resources
                new ItemStack(Material.DIAMOND, 3 + random.nextInt(5)),
                new ItemStack(Material.GOLD_INGOT, 5 + random.nextInt(8)),
                new ItemStack(Material.IRON_INGOT, 8 + random.nextInt(12)),
                new ItemStack(Material.EMERALD, 2 + random.nextInt(4)),

                // Food and Potions
                new ItemStack(Material.GOLDEN_APPLE, 2 + random.nextInt(3)),
                new ItemStack(Material.GOLDEN_APPLE, 1, (short) 1), // Notch apple
                new ItemStack(Material.COOKED_BEEF, 8 + random.nextInt(8)),
                createPotion(Material.POTION, 8201), // Regeneration II
                createPotion(Material.POTION, 8233), // Regeneration II Extended
                createPotion(Material.POTION, 8226), // Fire Resistance Extended

                // Utility
                new ItemStack(Material.ENDER_PEARL, 4 + random.nextInt(4)),
                new ItemStack(Material.ARROW, 32 + random.nextInt(32)),
                new ItemStack(Material.EXP_BOTTLE, 8 + random.nextInt(8)),
                new ItemStack(Material.ENCHANTED_BOOK, 1),
                new ItemStack(Material.ANVIL, 1),

                // Rare items
                new ItemStack(Material.NETHER_STAR, 1),
                new ItemStack(Material.BEACON, 1),
                createEnchantedItem(Material.FISHING_ROD, Enchantment.LUCK, 3)
        );

        // Fill chest with 5-9 random items
        int itemCount = 5 + random.nextInt(5);
        Set<Integer> usedSlots = new HashSet<>();

        for (int i = 0; i < itemCount; i++) {
            ItemStack loot = possibleLoot.get(random.nextInt(possibleLoot.size())).clone();

            // Find empty slot
            int slot;
            do {
                slot = random.nextInt(27);
            } while (usedSlots.contains(slot));

            usedSlots.add(slot);
            inventory.setItem(slot, loot);
        }
    }

    private ItemStack createEnchantedItem(Material material, Enchantment enchantment, int level) {
        ItemStack item = new ItemStack(material);
        item.addUnsafeEnchantment(enchantment, level);
        return item;
    }

    private ItemStack createPotion(Material material, int data) {
        ItemStack potion = new ItemStack(material, 1);
        potion.setDurability((short) data);
        return potion;
    }

    private void createBeaconEffect(Location location) {
        Location beaconLoc = location.clone().subtract(0, 1, 0);
        beaconLoc.getBlock().setType(Material.IRON_BLOCK);

        Location beaconTop = location.clone().add(0, 1, 0);
        beaconTop.getBlock().setType(Material.BEACON);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (beaconTop.getBlock().getType() == Material.BEACON) {
                    beaconTop.getBlock().setType(Material.AIR);
                }
                if (beaconLoc.getBlock().getType() == Material.IRON_BLOCK) {
                    beaconLoc.getBlock().setType(Material.STONE);
                }
            }
        }.runTaskLater(Main.get(), 600); // 30 seconds
    }

    private void announceNearbyPlayers(Location crateLocation) {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            double distance = player.getLocation().distance(crateLocation);

            if (distance <= 100) {
                player.sendMessage("§d[LootCrate] §fUn coffre de loot est apparu près de vous ! (" +
                        (int) distance + " blocs)");
            }
        }
    }

    private void removeAllCrates() {
        for (Location crateLocation : activeCrates) {
            Block block = crateLocation.getBlock();
            if (block.getType() == Material.CHEST) {
                block.setType(Material.AIR);
            }

            // Also remove beacon effects
            Block beaconBlock = crateLocation.clone().add(0, 1, 0).getBlock();
            if (beaconBlock.getType() == Material.BEACON) {
                beaconBlock.setType(Material.AIR);
            }

            Block ironBlock = crateLocation.clone().subtract(0, 1, 0).getBlock();
            if (ironBlock.getType() == Material.IRON_BLOCK) {
                ironBlock.setType(Material.STONE);
            }
        }

        activeCrates.clear();
    }

    // Get active crate locations
    public List<Location> getActiveCrates() {
        return new ArrayList<>(activeCrates);
    }

    // Force spawn crates (admin command)
    public void forceSpawnCrates() {
        if (isActive()) {
            spawnLootCrates();
            Bukkit.broadcastMessage("§d[LootCrate] §fCoffres forcés par un administrateur !");
        }
    }

    // Get time until next crate spawn

    // Check if a location has an active crate
    public boolean hasCrateAt(Location location) {
        for (Location crateLocation : activeCrates) {
            if (crateLocation.distance(location) < 1.0) {
                return true;
            }
        }
        return false;
    }

    // Remove a specific crate (when looted)
    public void removeCrate(Location location) {
        activeCrates.removeIf(crateLocation -> crateLocation.distance(location) < 1.0);
    }
}
