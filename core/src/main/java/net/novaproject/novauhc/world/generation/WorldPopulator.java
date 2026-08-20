package net.novaproject.novauhc.world.generation;

import java.util.Map;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.task.LoadingChunkTask;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

public class WorldPopulator {

    private final World arena;
    private final CenterType type;

    public WorldPopulator(World arena, CenterType type, Biome biome) {
        this.arena = arena;
        this.type = type;
        LoadingChunkTask t = LoadingChunkTask.get();
        if (t != null && !t.isFinished()) {
            LoadingChunkTask.stopPregen();
        }
        clearCenter(biome);
    }

    public void generateForest(TreeType one, TreeType two, TreeType three,
                               int chance1, int chance2, int chance3) {

        new BukkitRunnable() {

            final int radius = 250;
            final int maxProgress = 60;
            int progress = 0;
            int y = 50;

            @Override
            public void run() {

                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {

                        Block block = arena.getBlockAt(x, y, z);

                        if (block.getType() == Material.AIR &&
                                (block.getRelative(0, -1, 0).getType() == Material.DIRT
                                        || block.getRelative(0, -1, 0).getType() == Material.GRASS)) {

                            int i = ThreadLocalRandom.current().nextInt(100);

                            if (i <= chance1)
                                block.getWorld().generateTree(block.getLocation(), one);
                            else if (i == chance2)
                                block.getWorld().generateTree(block.getLocation(), two);
                            else if (i == chance3)
                                block.getWorld().generateTree(block.getLocation(), three);
                        }
                    }
                }

                y++;
                progress++;

                double percent = progress * 100.0 / maxProgress;
                String formatted = String.format("%.1f", percent);

                String progressBar = TextUtils.getProgressBar(
                        progress,
                        maxProgress,
                        40,
                        "|",
                        ChatColor.GREEN,
                        ChatColor.GRAY
                );

                for (Player player : Bukkit.getOnlinePlayers()) {
                    DisplayService.actionBar(
                            player,
                            LangManager.get().get(
                                    CoreLang.TASK_FOREST_GENERATION_ACTION_BAR,
                                    player,
                                    Map.of(
                                            "%percent%", formatted,
                                            "%bar%", progressBar
                                    )
                            )
                    );
                }

                if (progress >= maxProgress) {
                    LoadingChunkTask.create(
                            arena,
                            Common.get().getNether(),
                            (int) (arena.getWorldBorder().getSize() / 2)
                    );
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 1L, 5L);
    }

    private void clearCenter(Biome biome) {

        new BukkitRunnable() {

            final int radius = 250;
            final int maxProgress = 80;
            int progress = 0;
            int y = 50;

            @Override
            public void run() {

                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {

                        Block block = arena.getBlockAt(x, y, z);

                        block.setBiome(biome);
                        Material type = block.getType();

                        if (type == Material.LEAVES || type == Material.LEAVES_2
                                || type == Material.LOG || type == Material.LOG_2
                                || type == Material.HUGE_MUSHROOM_1
                                || type == Material.HUGE_MUSHROOM_2) {

                            block.setType(Material.AIR);

                        } else if (type == Material.WATER
                                || type == Material.STATIONARY_WATER
                                || type == Material.LAVA
                                || type == Material.STATIONARY_LAVA) {

                            block.setType(Material.GRASS);
                        }
                    }
                }

                progress++;
                y++;

                double percent = progress * 100.0 / maxProgress;
                String formatted = String.format("%.1f", percent);

                String progressBar = TextUtils.getProgressBar(
                        progress,
                        maxProgress,
                        40,
                        "|",
                        ChatColor.GREEN,
                        ChatColor.GRAY
                );

                for (Player player : Bukkit.getOnlinePlayers()) {
                    DisplayService.actionBar(
                            player,
                            LangManager.get().get(
                                    CoreLang.TASK_CLEAN_ACTION_BAR,
                                    player,
                                    Map.of(
                                            "%percent%", formatted,
                                            "%bar%", progressBar
                                    )
                            )
                    );
                }

                if (progress >= maxProgress) {
                    cancel();
                    type.generate(WorldPopulator.this);
                }
            }
        }.runTaskTimer(Main.get(), 1L, 5L);
    }

    private void sendProgress(String message, int progress, int max) {
        String bar = TextUtils.getProgressBar(
                progress,
                max,
                20,
                "|",
                ChatColor.YELLOW,
                ChatColor.GRAY
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            DisplayService.actionBar(
                    player,
                    ChatColor.YELLOW + message + ": "
                            + ChatColor.GREEN + progress + "% §8[§r"
                            + bar + "§8]"
            );
        }
    }
}