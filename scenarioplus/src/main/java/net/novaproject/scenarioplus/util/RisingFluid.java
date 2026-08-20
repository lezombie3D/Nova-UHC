package net.novaproject.scenarioplus.util;

import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class RisingFluid {

    private final Material fluide;
    private final Map<Long, Integer> niveauParChunk = new HashMap<>();
    private final Set<Long> file = new LinkedHashSet<>();
    private int niveau;
    private int depart;

    public RisingFluid(Material fluide) {
        this.fluide = fluide;
    }

    public void demarrer(int depart) {
        this.depart = depart;
        this.niveau = depart;
        niveauParChunk.clear();
        file.clear();
    }

    public int niveau() {
        return niveau;
    }

    public boolean monter(int pas, int plafond) {
        if (niveau >= plafond) return false;
        niveau = Math.min(plafond, niveau + pas);
        return true;
    }

    public void empiler(int chunkX, int chunkZ) {
        file.add(cle(chunkX, chunkZ));
    }

    public void empilerAutourDesJoueurs(World monde, int rayonBlocs) {
        int rayonChunks = Math.max(0, rayonBlocs >> 4);
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player joueur = uhcPlayer.getPlayer();
            if (joueur == null || !joueur.getWorld().equals(monde)) continue;
            int centreX = joueur.getLocation().getBlockX() >> 4;
            int centreZ = joueur.getLocation().getBlockZ() >> 4;
            for (int dx = -rayonChunks; dx <= rayonChunks; dx++) {
                for (int dz = -rayonChunks; dz <= rayonChunks; dz++) {
                    file.add(cle(centreX + dx, centreZ + dz));
                }
            }
        }
    }

    public void traiter(World monde, int budget) {
        Iterator<Long> parcours = file.iterator();
        for (int traites = 0; traites < budget && parcours.hasNext(); traites++) {
            long cle = parcours.next();
            parcours.remove();
            noyer(monde, cle);
        }
    }

    public void vider() {
        niveauParChunk.clear();
        file.clear();
        niveau = 0;
    }

    private void noyer(World monde, long cle) {
        int chunkX = (int) (cle >> 32);
        int chunkZ = (int) cle;
        if (!monde.isChunkLoaded(chunkX, chunkZ)) return;

        int deja = niveauParChunk.getOrDefault(cle, depart);
        if (deja >= niveau) return;

        Chunk chunk = monde.getChunkAt(chunkX, chunkZ);
        int id = fluide.getId();
        int plafond = Math.min(niveau, monde.getMaxHeight() - 1);
        for (int y = deja + 1; y <= plafond; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block bloc = chunk.getBlock(x, y, z);
                    if (bloc.getType() != Material.AIR) continue;
                    bloc.setTypeIdAndData(id, (byte) 0, false);
                }
            }
        }
        niveauParChunk.put(cle, plafond);
    }

    private static long cle(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
    }
}
