package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.task.LoadingChunkTask;
import org.bukkit.*;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class WorldGenerator {

    private static final int WORLD_RADIUS = 2000;
    private static String arenaName;
    private final Plugin plugin;
    private boolean isGenerating = false;
    private boolean isMapReady = false;

    public WorldGenerator(Plugin plugin, String arenaName) {
        this.plugin = plugin;
        WorldGenerator.arenaName = arenaName;
        Bukkit.getScheduler().runTaskLater(plugin, this::startGeneration, 20L);
    }

    private void startGeneration() {
        if (isGenerating) return;

        log("§eDémarrage de la génération de la map UHC...");
        isGenerating = true;
        isMapReady = false;

        safeDeleteWorld(arenaName);
        safeDeleteWorld(arenaName + "_nether");
        safeDeleteWorld(arenaName + "_the_end");
        Bukkit.getScheduler().runTaskLater(plugin, this::createUHCWorld, 40L);
    }

    private void createUHCWorld() {
        createEnd();
        createNether();
        World world = UHCWorldSettings.createUHCWorld(arenaName);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(6000L);
        world.getWorldBorder().setSize(WORLD_RADIUS);
        resetSpawnChunks(world);
        WaterFixer waterFixer = new WaterFixer(plugin);
        waterFixer.fixLiquids(world);
        if (ScenarioManager.get().getActiveScenarios().stream().anyMatch(Scenario::needRooft)){
            new WorldPopulator(world, WorldPopulator.CenterType.ROOFT);
        }else{
            LoadingChunkTask.create(world, Common.get().getNether(), (int) (world.getWorldBorder().getSize() / 2));
        }

    }

    private void resetSpawnChunks(World world) {
        int spawnChunkX = world.getSpawnLocation().getBlockX() >> 4;
        int spawnChunkZ = world.getSpawnLocation().getBlockZ() >> 4;

        for (int cx = spawnChunkX - 1; cx <= spawnChunkX + 1; cx++) {
            for (int cz = spawnChunkZ - 1; cz <= spawnChunkZ + 1; cz++) {
                if (world.isChunkLoaded(cx, cz)) {
                    world.unloadChunk(cx, cz, false, false);
                }
            }
        }
        world.loadChunk(spawnChunkX, spawnChunkZ);
    }

    private void safeDeleteWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            world.getPlayers().forEach(player ->
                    player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation())
            );
            Bukkit.unloadWorld(world, false);
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                deleteFolder(worldFolder);
            }
        } else {
            deleteFolder(new File(Bukkit.getWorldContainer(), worldName));
        }
    }

    private void deleteFolder(File folder) {
        if (folder.exists()) {
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteFolder(file);
                    }
                }
            }
            folder.delete();
        }
    }

    public void createNether() {
        String netherName = arenaName + "_nether";
        safeDeleteWorld(netherName);

        Bukkit.broadcastMessage(ChatColor.YELLOW + "Création du Nether...");

        WorldCreator creator = new WorldCreator(netherName);
        creator.environment(World.Environment.NETHER);
        creator.type(WorldType.NORMAL);
        World nether = creator.createWorld();

        if (nether == null) {
            return;
        }
        nether.setGameRuleValue("doFireTick", "false");
        nether.setGameRuleValue("naturalRegeneration", "false");
        nether.setDifficulty(Difficulty.NORMAL);

    }

    public void createEnd() {
        String endName = arenaName + "_the_end";
        deleteFolder(new File(endName));

        Bukkit.broadcastMessage(ChatColor.YELLOW + "Cr\u00E9ation du End...");

        WorldCreator creator = new WorldCreator(endName);
        creator.environment(World.Environment.THE_END);
        creator.type(WorldType.NORMAL);
        World end = creator.createWorld();

        if (end == null) {

            return;
        }
        end.setGameRuleValue("doFireTick", "false");
        end.setGameRuleValue("naturalRegeneration", "false");
        end.setDifficulty(Difficulty.HARD);

    }

    private void log(String message) {
        Bukkit.getConsoleSender().sendMessage("§8[§bUHC§8] §7" + message);
    }

    private void logSuccess(String message) {
        Bukkit.getConsoleSender().sendMessage("§8[§aUHC§8] §a" + message);
    }
}