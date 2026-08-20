package net.novaproject.novauhc.world.generation;

import net.minecraft.server.v1_8_R3.WorldGenMineshaft;
import java.util.Set;
import java.util.EnumSet;
import net.novaproject.novauhc.debug.DebugLog;
import net.minecraft.server.v1_8_R3.WorldGenVillage;
import net.minecraft.server.v1_8_R3.WorldGenStronghold;
import net.minecraft.server.v1_8_R3.WorldGenMonument;
import net.minecraft.server.v1_8_R3.WorldGenLargeFeature;
import net.minecraft.server.v1_8_R3.StructureGenerator;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.BiomeBase;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.WorldGenMinable;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class TerrainChunkGenerator extends ChunkGenerator {

    public static final int CHUNK = 16;
    public static final int WORLD_HEIGHT = 256;
    public static final int VANILLA_ORE_TOP = 128;

    private static final double MIN_SPAWN_DISTANCE_SQ = 50.0 * 50.0;

    private static final int AIR_ID = 0;
    private static final int BEDROCK_ID = Material.BEDROCK.getId();
    private static final int STONE_ID = Material.STONE.getId();
    private static final int DIRT_ID = Material.DIRT.getId();
    private static final int GRASS_ID = Material.GRASS.getId();
    private static final int SAND_ID = Material.SAND.getId();
    private static final int SANDSTONE_ID = Material.SANDSTONE.getId();
    private static final int GRAVEL_ID = Material.GRAVEL.getId();

    private final boolean[] solid = new boolean[WORLD_HEIGHT];

    protected abstract void solidColumn(int worldX, int worldZ, boolean[] solid);

    protected void beginChunk(World world, int chunkX, int chunkZ) {
    }

    protected int bedrockFloorY() {
        return 0;
    }

    protected int ceilingY() {
        return -1;
    }

    protected int ceilingId() {
        return BEDROCK_ID;
    }

    protected int fluidY() {
        return -1;
    }

    protected int fluidId() {
        return Material.STATIONARY_WATER.getId();
    }

    protected int stoneId() {
        return STONE_ID;
    }

    protected int fillerDepth() {
        return 3;
    }

    protected boolean capExposedTops() {
        return true;
    }

    public enum Structure {
        VILLAGE,
        TEMPLE,
        MINESHAFT,
        STRONGHOLD,
        MONUMENT,
    }

    public BiomeMapChunkManager.BiomeAt biomeMapping() {
        return null;
    }

    protected boolean decorate() {
        return true;
    }

    protected Set<Structure> structures() {
        return EnumSet.noneOf(Structure.class);
    }

    protected boolean injectOres() {
        return !decorate();
    }

    protected int oreTopY() {
        return VANILLA_ORE_TOP;
    }

    protected int surfaceId(Biome biome) {
        return switch (biome) {
            case DESERT, DESERT_HILLS, DESERT_MOUNTAINS, BEACH, COLD_BEACH,
                 MESA, MESA_BRYCE, MESA_PLATEAU, MESA_PLATEAU_FOREST -> SAND_ID;
            case STONE_BEACH -> GRAVEL_ID;
            default -> GRASS_ID;
        };
    }

    protected int fillerId(int surface) {
        if (surface == SAND_ID) return SANDSTONE_ID;
        if (surface == GRAVEL_ID) return stoneId();
        return DIRT_ID;
    }

    @Override
    public final ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biomes) {
        beginChunk(world, chunkX, chunkZ);
        ChunkData data = createChunkData(world);
        writeChunk(data, chunkX, chunkZ, biomes);
        return data;
    }

    final void writeChunk(ChunkData data, int chunkX, int chunkZ, BiomeGrid biomes) {
        int ceiling = ceilingY();
        int top = ceiling < 0 ? WORLD_HEIGHT : Math.min(ceiling, WORLD_HEIGHT);
        int fluid = Math.min(fluidY(), top - 1);
        int stone = stoneId();
        int depthMax = fillerDepth();
        boolean cap = capExposedTops();

        for (int x = 0; x < CHUNK; x++) {
            for (int z = 0; z < CHUNK; z++) {
                Arrays.fill(solid, false);
                solidColumn((chunkX << 4) | x, (chunkZ << 4) | z, solid);

                int surface = surfaceId(biomes.getBiome(x, z));
                int filler = fillerId(surface);
                int runTop = -1;

                for (int y = top - 1; y >= 1; y--) {
                    if (!solid[y]) {
                        runTop = -1;
                        if (y <= fluid) data.setBlock(x, y, z, fluidId());
                        continue;
                    }
                    if (runTop < 0) runTop = y;
                    if (!cap) {
                        data.setBlock(x, y, z, stone);
                        continue;
                    }
                    int depth = runTop - y;
                    if (depth == 0) data.setBlock(x, y, z, runTop <= fluid ? filler : surface);
                    else if (depth <= depthMax) data.setBlock(x, y, z, filler);
                    else data.setBlock(x, y, z, stone);
                }
            }
        }

        int floor = bedrockFloorY();
        if (floor >= 0) data.setRegion(0, 0, 0, CHUNK, floor + 1, CHUNK, BEDROCK_ID);
        if (ceiling >= 0 && ceiling < WORLD_HEIGHT) {
            data.setRegion(0, ceiling, 0, CHUNK, ceiling + 1, CHUNK, ceilingId());
            data.setRegion(0, ceiling + 1, 0, CHUNK, WORLD_HEIGHT, CHUNK, AIR_ID);
        }
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        List<BlockPopulator> populators = new ArrayList<>();
        Set<Structure> wanted = structures();
        if (!wanted.isEmpty()) populators.add(new StructurePopulator(wanted));
        if (decorate()) populators.add(new DecorationPopulator());
        if (injectOres()) populators.add(new OrePopulator(oreTopY()));
        return populators;
    }

    public static final class DecorationPopulator extends BlockPopulator {

        @Override
        public void populate(World world, Random random, Chunk chunk) {
            WorldServer handle = ((CraftWorld) world).getHandle();
            BlockPosition origine = new BlockPosition(chunk.getX() << 4, 0, chunk.getZ() << 4);
            BiomeBase biome = handle.getBiome(origine.a(16, 0, 16));
            try {
                biome.a(handle, random, origine);
            } catch (RuntimeException erreur) {
                DebugLog.warnOnce("terrain-decorate", "Decoration du chunk "
                        + chunk.getX() + "," + chunk.getZ() + " ignoree : " + erreur.getMessage());
            }
        }
    }

    public static final class StructurePopulator extends BlockPopulator {

        private final List<StructureGenerator> generateurs = new ArrayList<>();

        public StructurePopulator(Set<Structure> voulues) {
            for (Structure structure : voulues) {
                generateurs.add(creer(structure));
            }
        }

        private static StructureGenerator creer(Structure structure) {
            switch (structure) {
                case VILLAGE: return new WorldGenVillage();
                case TEMPLE: return new WorldGenLargeFeature();
                case MINESHAFT: return new WorldGenMineshaft();
                case STRONGHOLD: return new WorldGenStronghold();
                default: return new WorldGenMonument();
            }
        }

        @Override
        public void populate(World world, Random random, Chunk chunk) {
            WorldServer handle = ((CraftWorld) world).getHandle();
            ChunkCoordIntPair coords = new ChunkCoordIntPair(chunk.getX(), chunk.getZ());
            for (StructureGenerator generateur : generateurs) {
                generateur.a(null, handle, chunk.getX(), chunk.getZ(), null);
                generateur.a(handle, random, coords);
            }
        }
    }

    public static Location pocketSpawn(World world, Random random, int fromY, List<Location> taken) {
        double radius = Math.max(16.0, world.getWorldBorder().getSize() / 2.0 - 16.0);
        Location center = world.getWorldBorder().getCenter();
        int ceiling = Math.min(fromY, world.getMaxHeight() - 3);

        for (int attempt = 0; attempt < 64; attempt++) {
            int x = (int) Math.floor(center.getX() + (random.nextDouble() * 2 - 1) * radius);
            int z = (int) Math.floor(center.getZ() + (random.nextDouble() * 2 - 1) * radius);
            for (int y = ceiling; y > 1; y--) {
                if (world.getBlockAt(x, y, z).getType() != Material.AIR) continue;
                if (world.getBlockAt(x, y + 1, z).getType() != Material.AIR) continue;
                Material floor = world.getBlockAt(x, y - 1, z).getType();
                if (floor == Material.AIR || floor.name().contains("LAVA")) continue;
                Location candidate = new Location(world, x + 0.5, y, z + 0.5);
                if (tooClose(taken, candidate)) break;
                taken.add(candidate);
                return candidate;
            }
        }
        return null;
    }

    private static boolean tooClose(List<Location> taken, Location candidate) {
        for (Location placed : taken) {
            if (placed.getWorld() != candidate.getWorld()) continue;
            if (placed.distanceSquared(candidate) < MIN_SPAWN_DISTANCE_SQ) return true;
        }
        return false;
    }

    public static final class OrePopulator extends BlockPopulator {

        private final int topY;

        public OrePopulator() {
            this(VANILLA_ORE_TOP);
        }

        public OrePopulator(int topY) {
            this.topY = Math.max(16, topY);
        }

        @Override
        public void populate(World world, Random random, Chunk chunk) {
            WorldServer nms = ((CraftWorld) world).getHandle();
            int baseX = chunk.getX() << 4;
            int baseZ = chunk.getZ() << 4;

            vein(nms, random, baseX, baseZ, Blocks.COAL_ORE, 20, 17, 128);
            vein(nms, random, baseX, baseZ, Blocks.IRON_ORE,
                    UHCWorldSettings.ironCount(), UHCWorldSettings.ironSize(), 64);
            vein(nms, random, baseX, baseZ, Blocks.GOLD_ORE,
                    UHCWorldSettings.goldCount(), UHCWorldSettings.goldSize(), 32);
            vein(nms, random, baseX, baseZ, Blocks.LAPIS_ORE,
                    UHCWorldSettings.lapisCount(), UHCWorldSettings.lapisSize(), 32);
            vein(nms, random, baseX, baseZ, Blocks.DIAMOND_ORE,
                    UHCWorldSettings.diamondCount(), UHCWorldSettings.diamondSize(), 16);
            vein(nms, random, baseX, baseZ, Blocks.REDSTONE_ORE, 8, 8, 16);
        }

        private void vein(WorldServer nms, Random random, int baseX, int baseZ,
                          Block ore, int count, int size, int vanillaMaxY) {
            int maxY = Math.max(2, vanillaMaxY * topY / VANILLA_ORE_TOP);
            WorldGenMinable generator = new WorldGenMinable(ore.getBlockData(), size);
            for (int i = 0; i < count; i++) {
                generator.generate(nms, random, new BlockPosition(
                        baseX + random.nextInt(CHUNK),
                        1 + random.nextInt(maxY - 1),
                        baseZ + random.nextInt(CHUNK)));
            }
        }
    }
}
