package net.novaproject.scenarioplus.monde;

import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.IChunkProvider;
import net.minecraft.server.v1_8_R3.StructureBoundingBox;
import net.minecraft.server.v1_8_R3.StructurePiece;
import net.minecraft.server.v1_8_R3.StructureStart;
import net.minecraft.server.v1_8_R3.WorldGenMineshaft;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class VoidMineshaftWorld {

    private static final int PLANCHE = Material.WOOD.getId();
    private static final int RAYON_STARTS = 8;
    private static final double ECART_SPAWN_SQ = 50.0 * 50.0;
    private static final int ESSAIS_SPAWN = 16;

    private final DenseMineshaft mines;
    private final List<Location> spawns = new ArrayList<>();
    private final Random alea = new Random();

    public VoidMineshaftWorld(double densite) {
        this.mines = new DenseMineshaft(densite);
    }

    public ChunkGenerator generator() {
        return new ChunkGenerator() {
            @Override
            public ChunkData generateChunkData(World monde, Random random, int x, int z, BiomeGrid biomes) {
                return createChunkData(monde);
            }
        };
    }

    public BlockPopulator populator() {
        return new BlockPopulator() {
            @Override
            public void populate(World monde, Random random, Chunk chunk) {
                mines.render((CraftWorld) monde, random, chunk);
            }
        };
    }

    public Location spawn(World monde) {
        double rayon = monde.getWorldBorder().getSize() / 2.0 - 16.0;
        Location centre = monde.getWorldBorder().getCenter();
        Location dernier = null;
        for (int essai = 0; essai < ESSAIS_SPAWN; essai++) {
            Location trouve = mines.randomFloor(monde, alea);
            if (trouve == null) return dernier;
            dernier = trouve;
            if (Math.abs(trouve.getX() - centre.getX()) > rayon) continue;
            if (Math.abs(trouve.getZ() - centre.getZ()) > rayon) continue;
            if (tropProche(trouve)) continue;
            spawns.add(trouve);
            return trouve;
        }
        if (dernier != null) spawns.add(dernier);
        return dernier;
    }

    private boolean tropProche(Location candidat) {
        for (Location pose : spawns) {
            if (pose.getWorld() != candidat.getWorld()) continue;
            if (pose.distanceSquared(candidat) < ECART_SPAWN_SQ) return true;
        }
        return false;
    }

    private static final class DenseMineshaft extends WorldGenMineshaft {

        private final double densite;

        private DenseMineshaft(double densite) {
            this.densite = densite;
        }

        @Override
        protected boolean a(int chunkX, int chunkZ) {
            return this.b.nextDouble() < densite;
        }

        private void render(CraftWorld monde, Random random, Chunk chunk) {
            int cx = chunk.getX();
            int cz = chunk.getZ();
            a((IChunkProvider) null, monde.getHandle(), cx, cz, null);
            a(monde.getHandle(), random, new ChunkCoordIntPair(cx, cz));

            int minX = cx << 4;
            int minZ = cz << 4;
            for (int dx = -RAYON_STARTS; dx <= RAYON_STARTS; dx++) {
                for (int dz = -RAYON_STARTS; dz <= RAYON_STARTS; dz++) {
                    StructureStart start = this.e.get(ChunkCoordIntPair.a(cx + dx, cz + dz));
                    if (start == null) continue;
                    if (!start.a().a(minX, minZ, minX + 15, minZ + 15)) continue;
                    for (StructurePiece piece : start.b()) {
                        poserSol(chunk, piece.c(), minX, minZ);
                    }
                }
            }
        }

        private void poserSol(Chunk chunk, StructureBoundingBox boite, int minX, int minZ) {
            int y = boite.b - 1;
            if (y < 1 || y > 254) return;
            int deX = Math.max(boite.a, minX);
            int aX = Math.min(boite.d, minX + 15);
            int deZ = Math.max(boite.c, minZ);
            int aZ = Math.min(boite.f, minZ + 15);
            for (int x = deX; x <= aX; x++) {
                for (int z = deZ; z <= aZ; z++) {
                    Block bloc = chunk.getBlock(x - minX, y, z - minZ);
                    if (bloc.getType() == Material.AIR) bloc.setTypeIdAndData(PLANCHE, (byte) 0, false);
                }
            }
        }

        private Location randomFloor(World monde, Random alea) {
            if (this.e.isEmpty()) return null;
            List<StructureStart> starts = new ArrayList<>(this.e.values());
            List<StructurePiece> pieces = starts.get(alea.nextInt(starts.size())).b();
            if (pieces.isEmpty()) return null;
            StructureBoundingBox boite = pieces.get(alea.nextInt(pieces.size())).c();
            return new Location(monde,
                    (boite.a + boite.d) / 2.0 + 0.5, boite.b, (boite.c + boite.f) / 2.0 + 0.5);
        }
    }
}
