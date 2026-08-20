package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.task.LoadingChunkTask;
import org.bukkit.TreeType;
import org.bukkit.block.Biome;

public enum CenterType {
        ROOFT(Biome.ROOFED_FOREST) {
            @Override
            public void generate(WorldPopulator populator) {
                populator.generateForest(TreeType.DARK_OAK, TreeType.BROWN_MUSHROOM, TreeType.RED_MUSHROOM,4,98,99);
            }
        },
        TAIGA(Biome.TAIGA) {
            @Override
            public void generate(WorldPopulator populator) {
                populator.generateForest(TreeType.TALL_REDWOOD, TreeType.REDWOOD, TreeType.REDWOOD,4,98,99);
            }
        },
        FOREST(Biome.FOREST) {
            @Override
            public void generate(WorldPopulator populator) {
                populator.generateForest(TreeType.TREE, TreeType.BIRCH, TreeType.JUNGLE,1,2,-1);
            }
        },
        FLAT(Biome.PLAINS) {
            @Override
            public void generate(WorldPopulator populator) {
                LoadingChunkTask.create(Common.get().getArena(), Common.get().getNether(), (int) (Common.get().getArena().getWorldBorder().getSize() / 2));
            }
        };

        private final Biome biome;

        CenterType(Biome biome) {
            this.biome = biome;
        }

        public Biome getBiome() {
            return biome;
        }

        public abstract void generate(WorldPopulator populator);

        private static CenterType applied = null;

        public static CenterType getApplied() {
            return applied;
        }

        public static void setApplied(CenterType type) {
            applied = type;
        }
    }

