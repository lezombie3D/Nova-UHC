package net.novaproject.novauhc.task;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.world.generation.ChunkUnloadListener;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class ShadowLoadingChunkTask extends BukkitRunnable {


    private final World overworld;
    private final int radius;
    private final int chunkStep = 16;

    private final int chunkPerAxis;
    private final double totalChunksToLoad;
    private double loadedChunks = 0;

    private int idx = 0;

    private boolean finished = false;
    private final boolean canceled = false;

    public ShadowLoadingChunkTask(World overworld, int radius) {
        if (overworld == null) {
            throw new IllegalArgumentException("World cannot be null");
        }

        this.overworld = overworld;
        this.radius = radius;

        this.chunkPerAxis = ((radius * 2) / chunkStep) + 1;
        this.totalChunksToLoad = chunkPerAxis * chunkPerAxis; // Seulement l'overworld
        runTaskTimer(Main.get(), 0L, 5L);
    }


    @Override
    public void run() {
        if (canceled) {
            cancel();
            return;
        }

        for (int i = 0; i < 30 && !finished; i++) {
            loadNextChunk();
        }

        // Pas de mise à jour de progression

        if (loadedChunks >= totalChunksToLoad && !finished) {
            finished = true;
        }

        if (finished) {
            cancel();
        }
    }

    private void loadNextChunk() {
        if (idx >= chunkPerAxis * chunkPerAxis) return;

        int xStep = idx % chunkPerAxis;
        int zStep = idx / chunkPerAxis;

        int x = -radius + (xStep * chunkStep);
        int z = -radius + (zStep * chunkStep);

        loadChunk(overworld, x, z);
        loadedChunks++;

        idx++;

    }

    private void loadChunk(World world, int x, int z) {
        Location loc = new Location(world, x, 0, z);
        if (!loc.getChunk().isLoaded()) {
            world.loadChunk(loc.getChunk().getX(), loc.getChunk().getZ(), true);
            ChunkUnloadListener.keepChunk.add(loc.getChunk());
        }
    }


}