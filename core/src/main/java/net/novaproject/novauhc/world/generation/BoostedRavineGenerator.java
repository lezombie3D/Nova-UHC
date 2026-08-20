package net.novaproject.novauhc.world.generation;

import net.minecraft.server.v1_8_R3.ChunkSnapshot;
import net.minecraft.server.v1_8_R3.IChunkProvider;
import net.minecraft.server.v1_8_R3.World;
import net.minecraft.server.v1_8_R3.WorldGenCanyon;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BoostedRavineGenerator extends WorldGenCanyon {

    public static final AtomicInteger CHUNKS_GENERATED = new AtomicInteger(0);
    public static final AtomicLong TOTAL_PASSES = new AtomicLong(0);

    private static final long PASS_SALT = 0x9E3779B97F4A7C15L;
    private final int passes;

    public BoostedRavineGenerator(int passes) {
        this.passes = Math.max(1, passes);
    }

    @Override
    public void a(IChunkProvider provider, World world, int chunkX, int chunkZ, ChunkSnapshot snapshot) {
        int firstCall = CHUNKS_GENERATED.incrementAndGet();
        TOTAL_PASSES.addAndGet(passes);
        if (firstCall == 1) {
            Bukkit.getLogger().info("[CaveBooster] BoostedRavineGenerator first call (passes=" + passes + ") on " + world.getWorld().getName());
        }
        long baseSeed = world.getSeed();
        int radius = this.a;
        for (int pass = 0; pass < passes; pass++) {
            long passSeed = baseSeed + pass * PASS_SALT;
            this.c = world;
            this.b.setSeed(passSeed);
            long s1 = this.b.nextLong() / 2L * 2L + 1L;
            long s2 = this.b.nextLong() / 2L * 2L + 1L;
            for (int i = chunkX - radius; i <= chunkX + radius; i++) {
                for (int j = chunkZ - radius; j <= chunkZ + radius; j++) {
                    this.b.setSeed((long) i * s1 + (long) j * s2 ^ passSeed);
                    this.a(world, i, j, chunkX, chunkZ, snapshot);
                }
            }
        }
    }
}

