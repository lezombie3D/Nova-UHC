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

    public WorldPopulator(World arena) {
        this.arena = arena;
    }

    public void setRoofed() {
        if (ScenarioManager.get().getActiveScenarios().stream().anyMatch(Scenario::needRooft)) {
            new BukkitRunnable() {
                final int yInicial = 50;
                int progress = 0;
                int YChange = this.yInicial;

                public void run() {
                    int radius = 250;
                    if (this.progress == 0)

                        for (int x = -radius; x <= radius; x++) {
                            for (int z = -radius; z <= radius; z++) {
                                Block block = arena.getBlockAt(x, this.YChange, z);
                                block.setBiome(Biome.ROOFED_FOREST);
                                if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2 || block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
                                    block.setType(Material.AIR);
                                    if (block.getLocation().add(0.0D, -1.0D, 0.0D).getBlock().getType().equals(Material.DIRT))
                                        block.getLocation().add(0.0D, -1.0D, 0.0D).getBlock().setType(Material.GRASS);
                                } else if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
                                    block.setType(Material.GRASS);
                                }
                            }
                        }
                    this.YChange++;
                    this.progress++;
                    for (Player player : Bukkit.getOnlinePlayers())
                        new Titles().sendActionText(player, ChatColor.YELLOW + "Nettoyage du centre: " + ChatColor.GREEN + this.progress + "% \u00A78[\u00A7r" +
                                ProgressBar.getProgressBar(this.progress, 80, 20, "|", ChatColor.YELLOW, ChatColor.GRAY) + "\u00A78]");
                    if (this.progress >= 80) {
                        cancel();

                        WorldPopulator.this.addRooftTree();
                    }
                }
            }.runTaskTimer(Main.get(), 1L, 5L);
        } else {
            LoadingChunkTask.create(arena, Common.get().getNether(), (int) (arena.getWorldBorder().getSize() / 2));
        }
    }

    private void addRooftTree() {
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

}
