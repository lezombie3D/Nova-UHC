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
    private final Plugin plugin;

    public WaterFixer(Plugin plugin) {
        this.plugin = plugin;
    }

    public void fixLiquids(World world) {
        new BukkitRunnable() {
            @Override
            public void run() {
                startLiquidFixing(world);
            }
        }.runTask(plugin);
    }

    private void startLiquidFixing(World world) {
        log("Démarrage du nettoyage des liquides et des blocs instables...");
        Location center = new Location(world, 0, 0, 0);
        Set<String> checkedLocations = new HashSet<>();
        Set<Location> currentLiquidArea = new HashSet<>();
        int fixedAreas = 0;

        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {
                if (x * x + z * z > RADIUS * RADIUS) continue;
                Location loc = center.clone().add(x, 0, z);
                String key = x + ":" + z;
                if (checkedLocations.contains(key)) continue;
                currentLiquidArea.clear();
                if (processLiquidArea(world, loc, checkedLocations, currentLiquidArea)) {
                    fillLiquidArea(world, currentLiquidArea);
                    fixedAreas++;
                }
            }
        }
        log("Zones nettoyées: " + fixedAreas);
    }

    private boolean processLiquidArea(World world, Location start, Set<String> checked, Set<Location> liquidBlocks) {
        Queue<Location> toCheck = new LinkedList<>();
        toCheck.add(start);

        while (!toCheck.isEmpty()) {
            Location current = toCheck.poll();
            String key = current.getBlockX() + ":" + current.getBlockZ();

            if (checked.contains(key)) continue;
            checked.add(key);

            if (hasLiquidOrUnstableBlockAboveDepth(world, current)) {
                liquidBlocks.add(current.clone());
                addAdjacentLocations(current, toCheck);
            }
        }

        return liquidBlocks.size() >= MIN_LIQUID_SIZE;
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