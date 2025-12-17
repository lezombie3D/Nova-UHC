package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.TreeType;

public enum CenterType {
        ROOFT {
            @Override
            public void generate(WorldPopulator populator) {
                populator.generateForest(TreeType.DARK_OAK, TreeType.BROWN_MUSHROOM, TreeType.RED_MUSHROOM);
            }
        },
        TAIGA {
            @Override
            public void generate(WorldPopulator populator) {
                populator.generateForest(TreeType.BIG_TREE, TreeType.TALL_REDWOOD, TreeType.MEGA_REDWOOD);
            }
        },
        FOREST {
            @Override
            public void generate(WorldPopulator populator) {
            }
        },
        FLAT {
            @Override
            public void generate(WorldPopulator populator) {
            }
        };

        public abstract void generate(WorldPopulator populator);
    }