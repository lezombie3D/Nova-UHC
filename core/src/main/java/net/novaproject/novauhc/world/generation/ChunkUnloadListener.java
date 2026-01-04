package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.Common;
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
        World arena = Common.get().getArena();
        if (arena == null) return;
        Chunk chunk = event.getChunk();
        int size = (int) arena.getWorldBorder().getSize() / 2;

        if (keepChunk.contains(chunk) && (chunk.getX() > size || chunk.getZ() > size)) {
            event.setCancelled(true);
        }
    }
}
