package net.novaproject.novauhc.task;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.utils.ProgressBar;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.world.generation.ChunkUnloadListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class LoadingChunkTask extends BukkitRunnable {
    private static LoadingChunkTask instance;

    private final World overworld;
    private final World nether;
    private final int radius;
    private final int chunkStep = 16;

    private final int chunkPerAxis;
    private final double totalChunksToLoad;
    private double loadedChunks = 0;
    private double percent = 0;

    private int idx = 0;

    private boolean finished = false;
    private boolean canceled = false;
    private LoadingTaskState state = LoadingTaskState.READY;

    public LoadingChunkTask(World overworld, World nether, int radius) {
        if (overworld == null || nether == null) {
            throw new IllegalArgumentException("Worlds cannot be null");
        }

        this.overworld = overworld;
        this.nether = nether;
        this.radius = radius;

        this.chunkPerAxis = ((radius * 2) / chunkStep) + 1;
        this.totalChunksToLoad = chunkPerAxis * chunkPerAxis * 2;

        instance = this;
    }

    public static LoadingChunkTask create(World overworld, World nether, int radius) {
        if (instance != null && instance.getState() == LoadingTaskState.RUNNING) {
            stopPregen();
        }

        LoadingChunkTask task = new LoadingChunkTask(overworld, nether, radius);
        task.runTaskTimer(Main.get(), 0L, 5L);
        task.state = LoadingTaskState.RUNNING;

        Bukkit.broadcastMessage(CommonString.PREGEN_STARTED.getMessage());
        return task;
    }

    public static void stopPregen() {
        if (instance != null && instance.getState() == LoadingTaskState.RUNNING) {
            instance.setCanceled(true);
            instance.cancel();
            ChunkUnloadListener.keepChunk.clear();
            Bukkit.broadcastMessage(CommonString.PREGEN_FINISHED.getMessage());
        }
    }

    public static LoadingChunkTask get() {
        return instance;
    }

    @Override
    public void run() {
        if (state != LoadingTaskState.RUNNING || canceled) {
            if (canceled) state = LoadingTaskState.CANCELED;
            cancel();
            return;
        }

        for (int i = 0; i < 30 && !finished; i++) {
            loadNextChunk();
        }

        updateProgress();

        if (loadedChunks >= totalChunksToLoad && !finished) {
            finished = true;
        }

        if (finished) {
            state = LoadingTaskState.FINISHED;
            Bukkit.broadcastMessage(CommonString.PREGEN_FINISHED.getMessage());
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

        loadChunk(nether, x / 8, z / 8);
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

    private void updateProgress() {
        percent = Math.min(loadedChunks / totalChunksToLoad * 100.0, 100.0);

        if (loadedChunks >= totalChunksToLoad) percent = 100.0;

        String formatted = String.format("%.1f", percent);
        String progressBar = ProgressBar.getProgressBar((int) percent, 100, 40, "|", ChatColor.GREEN, ChatColor.GRAY);

        for (Player player : Bukkit.getOnlinePlayers()) {
            new Titles().sendActionText(player, ChatColor.GRAY + "Prégénération: " + ChatColor.GREEN + formatted + "% §8[§r" + progressBar + "§8]");
        }
    }

    public boolean isFinished() {
        return state == LoadingTaskState.FINISHED;
    }

    public boolean isCanceled() {
        return state == LoadingTaskState.CANCELED;
    }

    public void setCanceled(boolean cancel) {
        this.canceled = cancel;
        if (cancel) {
            state = LoadingTaskState.CANCELED;
        }
    }

    public LoadingTaskState getState() {
        return state;
    }

    public enum LoadingTaskState {
        READY,
        RUNNING,
        FINISHED,
        CANCELED
    }
}