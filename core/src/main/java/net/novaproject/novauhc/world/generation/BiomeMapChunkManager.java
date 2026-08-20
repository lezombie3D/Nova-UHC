package net.novaproject.novauhc.world.generation;

import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldChunkManager;
import net.minecraft.server.v1_8_R3.WorldProvider;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftBlock;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

public final class BiomeMapChunkManager extends WorldChunkManager {

    public interface BiomeAt {
        Biome at(int blockX, int blockZ);
    }

    private static final Map<Biome, BiomeBase> CACHE = new EnumMap<>(Biome.class);

    private final BiomeAt mapping;

    private BiomeMapChunkManager(World world, BiomeAt mapping) {
        super(world);
        this.mapping = mapping;
    }

    public static boolean install(org.bukkit.World bukkitWorld, BiomeAt mapping) {
        if (mapping == null) return false;
        try {
            World nms = ((CraftWorld) bukkitWorld).getHandle();
            Field field = WorldProvider.class.getDeclaredField("c");
            field.setAccessible(true);
            field.set(nms.worldProvider, new BiomeMapChunkManager(nms, mapping));
            return true;
        } catch (Throwable erreur) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[BiomeMap] installation impossible sur " + bukkitWorld.getName(), erreur);
            return false;
        }
    }

    private BiomeBase resolve(int blockX, int blockZ, BiomeBase defaut) {
        Biome voulu = mapping.at(blockX, blockZ);
        if (voulu == null) return defaut;
        BiomeBase connu = CACHE.get(voulu);
        if (connu == null) {
            connu = CraftBlock.biomeToBiomeBase(voulu);
            if (connu == null) return defaut;
            CACHE.put(voulu, connu);
        }
        return connu;
    }

    @Override
    public BiomeBase[] getBiomes(BiomeBase[] target, int cellX, int cellZ, int width, int height) {
        BiomeBase[] result = super.getBiomes(target, cellX, cellZ, width, height);
        if (result == null) return null;
        for (int i = 0; i < width * height && i < result.length; i++) {
            result[i] = resolve((cellX + i % width) << 2, (cellZ + i / width) << 2, result[i]);
        }
        return result;
    }

    @Override
    public BiomeBase[] a(BiomeBase[] target, int blockX, int blockZ, int width, int height, boolean useCache) {
        BiomeBase[] result = super.a(target, blockX, blockZ, width, height, useCache);
        if (result == null) return null;
        for (int i = 0; i < width * height && i < result.length; i++) {
            result[i] = resolve(blockX + i % width, blockZ + i / width, result[i]);
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
        int originX = (blockX - radius) >> 2;
        int originZ = (blockZ - radius) >> 2;

        BiomeBase[] found = getBiomes(null, originX, originZ, cells, cells);
        if (found == null) return null;

        BlockPosition chosen = null;
        int matches = 0;
        for (int i = 0; i < found.length; i++) {
            if (!allowed.contains(found[i])) continue;
            if (chosen == null || random.nextInt(matches + 1) == 0) {
                chosen = new BlockPosition((originX + i % cells) << 2, 0, (originZ + i / cells) << 2);
            }
            matches++;
        }
        return chosen;
    }
}
