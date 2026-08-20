package net.novaproject.novauhc.world;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.world.generation.ChunkUnloadListener;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.WorldBorder;

public class ArenaMirrorManager {

    private static final ArenaMirrorManager INSTANCE = new ArenaMirrorManager();
    public static ArenaMirrorManager get() { return INSTANCE; }

    private final Set<String> mirrorWorldNames = new HashSet<>();
    private int syncTaskId = -1;


    public void registerMirror(World world) {
        if (world == null) return;
        mirrorWorldNames.add(world.getName());
        if (syncTaskId == -1) startBorderSync();

        syncBorderToMirror(world);
    }

    public void unregisterMirror(World world) {
        if (world == null) return;
        mirrorWorldNames.remove(world.getName());
    }

    public void unregisterMirror(String worldName) {
        if (worldName != null) mirrorWorldNames.remove(worldName);
    }

    public boolean isMirror(World world) {
        return world != null && mirrorWorldNames.contains(world.getName());
    }

    public boolean isArenaOrMirror(World world) {
        if (world == null) return false;
        World arena = Common.get() != null ? Common.get().getArena() : null;
        if (world.equals(arena)) return true;
        return isMirror(world);
    }

    public Set<String> getMirrorWorldNames() {
        return Collections.unmodifiableSet(mirrorWorldNames);
    }

    public void pregenAndKeep(World world, int radius) {
        if (world == null) return;
        Bukkit.getScheduler().runTask(Main.get(), () -> {
            int chunkRadius = (radius / 16) + 1;
            int loaded = 0;
            for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
                for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                    Chunk chunk = world.getChunkAt(cx, cz);
                    if (!chunk.isLoaded()) chunk.load(false);
                    if (!ChunkUnloadListener.keepChunk.contains(chunk)) {
                        ChunkUnloadListener.keepChunk.add(chunk);
                    }
                    loaded++;
                }
            }
            Bukkit.getLogger().info("[ArenaMirror] " + loaded + " chunks chargés et protégés pour miroir " + world.getName());
        });
    }

    private void startBorderSync() {
        if (syncTaskId != -1) Bukkit.getScheduler().cancelTask(syncTaskId);
        syncTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.get(), () -> {
            if (mirrorWorldNames.isEmpty()) return;
            for (String name : mirrorWorldNames) {
                World mirror = Bukkit.getWorld(name);
                if (mirror != null) syncBorderToMirror(mirror);
            }
        }, 20L, 20L);
    }

    public void propagatePVP(boolean pvp) {
        World arena = Common.get() != null ? Common.get().getArena() : null;
        if (arena != null) arena.setPVP(pvp);
        for (String name : mirrorWorldNames) {
            World m = Bukkit.getWorld(name);
            if (m != null) m.setPVP(pvp);
        }
    }

    public void propagateTime(long time) {
        World arena = Common.get() != null ? Common.get().getArena() : null;
        if (arena != null) arena.setTime(time);
        for (String name : mirrorWorldNames) {
            World m = Bukkit.getWorld(name);
            if (m != null) m.setTime(time);
        }
    }

    public void propagateGameRule(String rule, String value) {
        World arena = Common.get() != null ? Common.get().getArena() : null;
        if (arena != null) arena.setGameRuleValue(rule, value);
        for (String name : mirrorWorldNames) {
            World m = Bukkit.getWorld(name);
            if (m != null) m.setGameRuleValue(rule, value);
        }
    }

    private void syncBorderToMirror(World mirror) {
        World arena = Common.get() != null ? Common.get().getArena() : null;
        if (arena == null || mirror == null) return;
        WorldBorder s = arena.getWorldBorder();
        WorldBorder d = mirror.getWorldBorder();
        d.setCenter(s.getCenter().getX(), s.getCenter().getZ());
        d.setSize(s.getSize());
        d.setDamageAmount(s.getDamageAmount());
        d.setDamageBuffer(s.getDamageBuffer());
        d.setWarningDistance(s.getWarningDistance());
        d.setWarningTime(s.getWarningTime());
    }
}

