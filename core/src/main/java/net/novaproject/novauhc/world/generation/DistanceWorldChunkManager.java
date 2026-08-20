package net.novaproject.novauhc.world.generation;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldChunkManager;
import net.minecraft.server.v1_8_R3.WorldProvider;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class DistanceWorldChunkManager extends WorldChunkManager {

    private final BiomeZones zones;

    public DistanceWorldChunkManager(World world, BiomeZones zones) {
        super(world);
        this.zones = zones;
    }

    public static boolean install(org.bukkit.World bukkitWorld) {
        BiomeZones zones = BiomeZones.snapshot();
        if (!zones.isActive()) return false;

        try {
            World nms = ((CraftWorld) bukkitWorld).getHandle();
            WorldProvider provider = nms.worldProvider;

            Field field = WorldProvider.class.getDeclaredField("c");
            field.setAccessible(true);
            field.set(provider, new DistanceWorldChunkManager(nms, zones));
            return true;
        } catch (Throwable t) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[BiomeZones] installation impossible sur " + bukkitWorld.getName(), t);
            return false;
        }
    }

    @Override
    public BiomeBase[] getBiomes(BiomeBase[] target, int cellX, int cellZ, int width, int height) {
        BiomeBase[] result = super.getBiomes(target, cellX, cellZ, width, height);
        if (result == null) return null;

        for (int i = 0; i < width * height && i < result.length; i++) {
            int blockX = (cellX + i % width) << 2;
            int blockZ = (cellZ + i / width) << 2;
            result[i] = zones.resolve(blockX, blockZ, result[i]);
        }
        return result;
    }

    @Override
    public BiomeBase[] a(BiomeBase[] target, int blockX, int blockZ, int width, int height, boolean useCache) {
        BiomeBase[] result = super.a(target, blockX, blockZ, width, height, useCache);
        if (result == null) return null;

        for (int i = 0; i < width * height && i < result.length; i++) {
            result[i] = zones.resolve(blockX + i % width, blockZ + i / width, result[i]);
        }
        return result;
    }

    @Override
    public boolean a(int blockX, int blockZ, int radius, List<BiomeBase> allowed) {
        int cells = (radius << 1) / 4 + 1;
        BiomeBase[] found = getBiomes(null, (blockX - radius) >> 2, (blockZ - radius) >> 2, cells, cells);
        if (found == null) return false;

        for (BiomeBase biome : found) {
            if (!allowed.contains(biome)) return false;
        }
        return true;
    }

    @Override
    public BlockPosition a(int blockX, int blockZ, int radius, List<BiomeBase> allowed, Random random) {
        int cells = (radius << 1) / 4 + 1;
        int originCell = (blockX - radius) >> 2;
        int originCellZ = (blockZ - radius) >> 2;

        BiomeBase[] found = getBiomes(null, originCell, originCellZ, cells, cells);
        if (found == null) return null;

        BlockPosition chosen = null;
        int matches = 0;

        for (int i = 0; i < found.length; i++) {
            int x = (originCell + i % cells) << 2;
            int z = (originCellZ + i / cells) << 2;
            if (!allowed.contains(found[i])) continue;
            if (chosen == null || random.nextInt(matches + 1) == 0) {
                chosen = new BlockPosition(x, 0, z);
            }
            matches++;
        }
        return chosen;
    }
}
