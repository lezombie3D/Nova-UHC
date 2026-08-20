package net.novaproject.novauhc.world.generation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class WaterFixer {
    private static final int RADIUS = 400;
    private static final int MAX_DEPTH = 57;
    private static final int MIN_LIQUID_SIZE = 3;
    private static final int COLUMNS_PER_TICK = 1024;
    private static final int MAX_AREA_SIZE = 4096;
    private final Plugin plugin;

    public WaterFixer(Plugin plugin) {
        this.plugin = plugin;
    }

    public void fixLiquids(World world) {
        log("Démarrage du nettoyage des liquides et des blocs instables...");
        final Set<String> checkedLocations = new HashSet<>();

        new BukkitRunnable() {
            int x = -RADIUS;
            int z = -RADIUS;
            int fixedAreas = 0;
            final long start = System.currentTimeMillis();
            final Set<Location> currentLiquidArea = new HashSet<>();

            @Override
            public void run() {
                int budget = COLUMNS_PER_TICK;

                while (budget > 0) {
                    if (x > RADIUS) {
                        log("Zones nettoyées: " + fixedAreas + " en " + (System.currentTimeMillis() - start) + "ms");
                        cancel();
                        return;
                    }

                    if (x * x + z * z <= RADIUS * RADIUS && !checkedLocations.contains(x + ":" + z)) {
                        currentLiquidArea.clear();
                        Location loc = new Location(world, x, 0, z);
                        budget -= processLiquidArea(world, loc, checkedLocations, currentLiquidArea);
                        if (currentLiquidArea.size() >= MIN_LIQUID_SIZE) {
                            fillLiquidArea(world, currentLiquidArea);
                            fixedAreas++;
                        }
                    }

                    budget--;
                    z++;
                    if (z > RADIUS) {
                        z = -RADIUS;
                        x++;
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private int processLiquidArea(World world, Location start, Set<String> checked, Set<Location> liquidBlocks) {
        Queue<Location> toCheck = new LinkedList<>();
        toCheck.add(start);
        int processed = 0;

        while (!toCheck.isEmpty() && liquidBlocks.size() < MAX_AREA_SIZE) {
            Location current = toCheck.poll();
            String key = current.getBlockX() + ":" + current.getBlockZ();

            if (checked.contains(key)) continue;
            checked.add(key);
            processed++;

            if (hasLiquidOrUnstableBlockAboveDepth(world, current)) {
                liquidBlocks.add(current.clone());
                addAdjacentLocations(current, toCheck);
            }
        }

        return processed;
    }

    private boolean hasLiquidOrUnstableBlockAboveDepth(World world, Location loc) {
        int maxY = world.getHighestBlockYAt(loc);
        for (int y = maxY; y >= 0 && y > MAX_DEPTH; y--) {
            Block block = world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ());
            if (shouldReplace(block.getType())) {
                return true;
            } else if (block.getType().isSolid() && !shouldReplace(block.getType())) {
                break;
            }
        }
        return false;
    }

    private void fillLiquidArea(World world, Set<Location> liquidBlocks) {
        for (Location loc : liquidBlocks) {
            int maxY = world.getHighestBlockYAt(loc);
            boolean topPlaced = false;
            for (int y = maxY; y >= 0 && y > MAX_DEPTH; y--) {
                Block block = world.getBlockAt(loc.getBlockX(), y, loc.getBlockZ());

                if (shouldReplace(block.getType())) {
                    if (!topPlaced) {
                        block.setType(Material.GRASS);
                        topPlaced = true;
                    } else {
                        block.setType(Material.DIRT);
                    }
                } else if (block.getType().isSolid() && !shouldReplace(block.getType())) {
                    break;
                }
            }
        }
    }

    private boolean shouldReplace(Material material) {
        return material == Material.WATER ||
                material == Material.STATIONARY_WATER ||
                material == Material.LAVA ||
                material == Material.STATIONARY_LAVA ||
                material == Material.SAND ||
                material == Material.GRAVEL;
    }

    private void addAdjacentLocations(Location loc, Queue<Location> toCheck) {
        int[] dx = {-1, 1, 0, 0};
        int[] dz = {0, 0, -1, 1};

        for (int i = 0; i < 4; i++) {
            Location newLoc = loc.clone().add(dx[i], 0, dz[i]);
            if (isInRadius(newLoc)) {
                toCheck.add(newLoc);
            }
        }
    }

    private boolean isInRadius(Location loc) {
        return (loc.getBlockX() * loc.getBlockX() + loc.getBlockZ() * loc.getBlockZ()) <= RADIUS * RADIUS;
    }

    private void log(String message) {
        Bukkit.getConsoleSender().sendMessage("§8[§bWaterFixer§8] §7" + message);
    }
}

