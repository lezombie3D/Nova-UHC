package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.task.LoadingChunkTask;
import net.novaproject.novauhc.utils.ProgressBar;
import net.novaproject.novauhc.utils.Titles;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class WorldPopulator {

    private final World arena;
    private CenterType type;

    public WorldPopulator(World arena, CenterType type) {
        this.arena = arena;
        this.type = type;
    }


    private void generateRooftForest(){
        (new Thread(() -> (new BukkitRunnable() {
            final int yInicial = 50;
            int progress = 0;
            int YChange = this.yInicial;

            public void run() {
                for (int radius = 250, x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = arena.getBlockAt(x, this.YChange, z);
                        if (block.getType() == Material.AIR && (arena.getBlockAt(x, this.YChange - 1, z).getType().equals(Material.DIRT) || arena.getBlockAt(x, this.YChange - 1, z).getType().equals(Material.GRASS))) {
                            int i = ThreadLocalRandom.current().nextInt(36);
                            if (i <= 2)
                                block.getWorld().generateTree(block.getLocation(), TreeType.DARK_OAK);
                            if (i == 33) {
                                block.getWorld().generateTree(block.getLocation(), TreeType.BROWN_MUSHROOM);
                            } else if (i == 34) {
                                block.getWorld().generateTree(block.getLocation(), TreeType.RED_MUSHROOM);
                            }
                        }
                    }
                }
                this.YChange++;
                this.progress++;
                for (Player player : Bukkit.getOnlinePlayers())
                    new Titles().sendActionText(player, ChatColor.YELLOW + "Création de la forêt: " + ChatColor.GREEN + this.progress + "% \u00A78[\u00A7r" +
                            ProgressBar.getProgressBar(this.progress, 100, 20, "|", ChatColor.YELLOW, ChatColor.GRAY) + "\u00A78]");
                if (this.progress >= 60) {
                    LoadingChunkTask.create(arena, Common.get().getNether(), (int) (arena.getWorldBorder().getSize() / 2));
                    cancel();
                }
            }
        }).runTaskTimer(Main.get(), 1L, 5L))).start();
    }

    private void clearCenter() {
        new BukkitRunnable() {
            int progress = 0;
            int y = 50;

            @Override
            public void run() {
                int radius = 250;

                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block block = arena.getBlockAt(x, y, z);

                        block.setBiome(Biome.ROOFED_FOREST);

                        Material type = block.getType();

                        if (type == Material.LEAVES || type == Material.LEAVES_2
                                || type == Material.LOG || type == Material.LOG_2) {

                            block.setType(Material.AIR);

                        } else if (type == Material.WATER || type == Material.STATIONARY_WATER) {

                            block.setType(Material.GRASS);
                        }
                    }
                }
                for (Player player : Bukkit.getOnlinePlayers()) new Titles().sendActionText(player, ChatColor.YELLOW + "Nettoyage du centre: " + ChatColor.GREEN + this.progress + "% \u00A78[\u00A7r" + ProgressBar.getProgressBar(this.progress, 80, 20, "|", ChatColor.YELLOW, ChatColor.GRAY) + "\u00A78]");
                progress++;
                y++;

                if (progress >= 80) {
                    cancel();
                    type.generate(WorldPopulator.this);
                }
            }

        }.runTaskTimer(Main.get(), 1L, 5L);
    }

    public enum CenterType {

        ROOFT {
            @Override
            public void generate(WorldPopulator populator) {
                populator.clearCenter();
                populator.generateRooftForest();
            }
        },

        TAIGA {
            @Override
            public void generate(WorldPopulator populator) {
                populator.clearCenter();
                //populator.generateTaigaForest();
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
                populator.clearCenter();
            }
        };

        public abstract void generate(WorldPopulator populator);
    }

}
