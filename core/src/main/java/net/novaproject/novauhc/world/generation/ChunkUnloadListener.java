package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.world.ArenaMirrorManager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.List;

public class ChunkUnloadListener implements Listener {

    public static List<Chunk> keepChunk = new ArrayList<>();

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        if (!keepChunk.contains(chunk)) return;

        World arena = Common.get() != null ? Common.get().getArena() : null;
        if (arena == null) return;
        int sizeChunks = ((int) arena.getWorldBorder().getSize() / 2) >> 4;

        World cw = chunk.getWorld();
        if (!ArenaMirrorManager.get().isArenaOrMirror(cw)) return;

        if (Math.abs(chunk.getX()) <= sizeChunks && Math.abs(chunk.getZ()) <= sizeChunks) {
            event.setCancelled(true);
        }
    }
}

