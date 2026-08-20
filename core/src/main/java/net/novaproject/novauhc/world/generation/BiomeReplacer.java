package net.novaproject.novauhc.world.generation;

import java.util.logging.Level;
import net.minecraft.server.v1_8_R3.*;
import net.novaproject.novauhc.utils.nms.NmsAccessor.Reflection;

import java.lang.reflect.Field;
import java.util.ArrayList;
import org.bukkit.Bukkit;

public class BiomeReplacer {

    public static void init() {
        try {
            Field biomeF = BiomeBase.class.getDeclaredField("biomes");
            biomeF.setAccessible(true);

            if (biomeF.get(null) instanceof BiomeBase[] biomes) {

                swap(biomes, BiomeBase.JUNGLE, BiomeBase.FOREST);
                swap(biomes, BiomeBase.OCEAN, BiomeBase.PLAINS);
                swap(biomes, BiomeBase.JUNGLE_HILLS, BiomeBase.FOREST_HILLS);
                swap(biomes, BiomeBase.JUNGLE_EDGE, BiomeBase.FOREST);
                swap(biomes, BiomeBase.DEEP_OCEAN, BiomeBase.FOREST);
                swap(biomes, BiomeBase.DESERT, BiomeBase.SAVANNA);
                swap(biomes, BiomeBase.MESA, BiomeBase.FOREST);
                swap(biomes, BiomeBase.MESA_PLATEAU, BiomeBase.FOREST);
                swap(biomes, BiomeBase.MESA_PLATEAU_F, BiomeBase.FOREST);
                swap(biomes, BiomeBase.DESERT_HILLS, BiomeBase.FOREST_HILLS);
                swap(biomes, BiomeBase.ICE_MOUNTAINS, BiomeBase.FOREST);
                swap(biomes, BiomeBase.ICE_PLAINS, BiomeBase.FOREST);
                swap(biomes, BiomeBase.BEACH, BiomeBase.SAVANNA);
                swap(biomes, BiomeBase.COLD_BEACH, BiomeBase.SMALL_MOUNTAINS);
                swap(biomes, BiomeBase.STONE_BEACH, BiomeBase.SAVANNA_PLATEAU);
                swap(biomes, BiomeBase.SWAMPLAND, BiomeBase.FOREST);
                swap(biomes, BiomeBase.FROZEN_OCEAN, BiomeBase.FOREST);
                swap(biomes, BiomeBase.MEGA_TAIGA, BiomeBase.PLAINS);
                swap(biomes, BiomeBase.MEGA_TAIGA_HILLS, BiomeBase.FOREST);
                swap(biomes, BiomeBase.TAIGA, BiomeBase.PLAINS);
                swap(biomes, BiomeBase.TAIGA_HILLS, BiomeBase.FOREST);
                swap(biomes, BiomeBase.EXTREME_HILLS, BiomeBase.FOREST);
                swap(biomes, BiomeBase.EXTREME_HILLS_PLUS, BiomeBase.FOREST_HILLS);

                Reflection.setFinalStatic(biomeF, biomes);
            }
            fixAnimals();
            biomeF.setAccessible(false);

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING,
                    "[BiomeReplacer] échec du remplacement de biomes (NMS) : " + e.getMessage(), e);
        }
    }

    private static void fixAnimals() throws ReflectiveOperationException {
        addAnimalsSpawn("PLAINS", BiomeBase.PLAINS);
        addAnimalsSpawn("DESERT", BiomeBase.DESERT);
        addAnimalsSpawn("EXTREME_HILLS", BiomeBase.EXTREME_HILLS);
        addAnimalsSpawn("FOREST", BiomeBase.FOREST);
        addAnimalsSpawn("TAIGA", BiomeBase.TAIGA);
        addAnimalsSpawn("SWAMPLAND", BiomeBase.SWAMPLAND);
        addAnimalsSpawn("RIVER", BiomeBase.RIVER);
        addAnimalsSpawn("FROZEN_OCEAN", BiomeBase.FROZEN_OCEAN);
        addAnimalsSpawn("FROZEN_RIVER", BiomeBase.FROZEN_RIVER);
        addAnimalsSpawn("MUSHROOM_ISLAND", BiomeBase.MUSHROOM_ISLAND);
        addAnimalsSpawn("MUSHROOM_SHORE", BiomeBase.MUSHROOM_SHORE);
        addAnimalsSpawn("BEACH", BiomeBase.BEACH);
        addAnimalsSpawn("DESERT_HILLS", BiomeBase.DESERT_HILLS);
        addAnimalsSpawn("FOREST_HILLS", BiomeBase.FOREST_HILLS);
        addAnimalsSpawn("TAIGA_HILLS", BiomeBase.TAIGA_HILLS);
        addAnimalsSpawn("SMALL_MOUNTAINS", BiomeBase.SMALL_MOUNTAINS);
        addAnimalsSpawn("JUNGLE", BiomeBase.JUNGLE);
        addAnimalsSpawn("JUNGLE_HILLS", BiomeBase.JUNGLE_HILLS);
        addAnimalsSpawn("JUNGLE_EDGE", BiomeBase.JUNGLE_EDGE);
        addAnimalsSpawn("STONE_BEACH", BiomeBase.STONE_BEACH);
        addAnimalsSpawn("COLD_BEACH", BiomeBase.COLD_BEACH);
        addAnimalsSpawn("BIRCH_FOREST", BiomeBase.BIRCH_FOREST);
        addAnimalsSpawn("BIRCH_FOREST_HILLS", BiomeBase.BIRCH_FOREST_HILLS);
        addAnimalsSpawn("ROOFED_FOREST", BiomeBase.ROOFED_FOREST);
        addAnimalsSpawn("COLD_TAIGA", BiomeBase.COLD_TAIGA);
        addAnimalsSpawn("COLD_TAIGA_HILLS", BiomeBase.COLD_TAIGA_HILLS);
        addAnimalsSpawn("MEGA_TAIGA", BiomeBase.MEGA_TAIGA);
        addAnimalsSpawn("MEGA_TAIGA_HILLS", BiomeBase.MEGA_TAIGA_HILLS);
        addAnimalsSpawn("EXTREME_HILLS_PLUS", BiomeBase.EXTREME_HILLS_PLUS);
        addAnimalsSpawn("SAVANNA", BiomeBase.SAVANNA);
        addAnimalsSpawn("SAVANNA_PLATEAU", BiomeBase.SAVANNA_PLATEAU);
        addAnimalsSpawn("MESA", BiomeBase.MESA);
        addAnimalsSpawn("MESA_PLATEAU_F", BiomeBase.MESA_PLATEAU_F);
        addAnimalsSpawn("MESA_PLATEAU", BiomeBase.MESA_PLATEAU);
        addAnimalsSpawn("FOREST", BiomeBase.FOREST);
    }

    private static void addAnimalsSpawn(String name, BiomeBase biomeBase) throws ReflectiveOperationException {
        Field biome = BiomeBase.class.getDeclaredField(name);
        Field defaultMobField = BiomeBase.class.getDeclaredField("au");
        defaultMobField.setAccessible(true);
        ArrayList<BiomeBase.BiomeMeta> mobs = new ArrayList<>();
        mobs.add(new BiomeBase.BiomeMeta(EntitySheep.class, 15, 10, 10));
        mobs.add(new BiomeBase.BiomeMeta(EntityRabbit.class, 4, 3, 5));
        mobs.add(new BiomeBase.BiomeMeta(EntityPig.class, 15, 10, 20));
        mobs.add(new BiomeBase.BiomeMeta(EntityChicken.class, 20, 10, 20));
        mobs.add(new BiomeBase.BiomeMeta(EntityCow.class, 20, 10, 20));
        mobs.add(new BiomeBase.BiomeMeta(EntityWolf.class, 5, 5, 10));
        defaultMobField.set(biomeBase, mobs);
        Reflection.setFinalStatic(biome, biomeBase);
    }

    private static void swap(BiomeBase[] biomes, BiomeBase from, BiomeBase to) {
        biomes[from.id] = to;
    }
}