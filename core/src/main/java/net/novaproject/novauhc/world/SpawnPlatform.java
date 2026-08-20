package net.novaproject.novauhc.world;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class SpawnPlatform {

    private final Location center;
    private final int size;
    private final byte glassColor;
    private final List<Block> placedBlocks = new ArrayList<>();

    public SpawnPlatform(Location center, int size, int glassColor) {
        this.center = center;
        this.size = Math.max(1, size + (size + 1) % 2);
        this.glassColor = (byte) Math.max(0, Math.min(15, glassColor));
    }

    public Location getSpawnLocation() {
        return new Location(center.getWorld(),
                center.getBlockX() + 0.5, center.getBlockY() + 1, center.getBlockZ() + 0.5);
    }

    @SuppressWarnings("deprecation")
    public void build() {
        World world = center.getWorld();
        if (world == null) return;
        int half = size / 2;
        int x = center.getBlockX();
        int y = center.getBlockY();
        int z = center.getBlockZ();

        for (int dx = -half; dx <= half; dx++) {
            for (int dz = -half; dz <= half; dz++) {
                Block block = world.getBlockAt(x + dx, y, z + dz);
                if (block.getType() != Material.AIR) continue;
                block.setType(Material.STAINED_GLASS);
                block.setData(glassColor);
                placedBlocks.add(block);
            }
        }

        int wallHalf = half + 1;
        for (int wy = 1; wy <= 3; wy++) {
            for (int dx = -wallHalf; dx <= wallHalf; dx++) {
                for (int dz = -wallHalf; dz <= wallHalf; dz++) {
                    if (Math.abs(dx) != wallHalf && Math.abs(dz) != wallHalf) continue;
                    Block block = world.getBlockAt(x + dx, y + wy, z + dz);
                    if (block.getType() != Material.AIR) continue;
                    block.setType(Material.BARRIER);
                    placedBlocks.add(block);
                }
            }
        }
    }

    public void destroy() {
        for (Block block : placedBlocks) {
            block.setType(Material.AIR);
        }
        placedBlocks.clear();
    }
}

