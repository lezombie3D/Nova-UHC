package net.novaproject.novauhc.world.generation;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.debug.DebugLog;
import net.novaproject.novauhc.event.UhcWorldEvents;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.task.LoadingChunkTask;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class WorldGenerator {

    private static final int WORLD_RADIUS = 2000;
    private static final int DEV_WORLD_RADIUS = 1000;
    private static String arenaName;
    private final Plugin plugin;
    private static boolean isGenerating = false;
    private boolean isMapReady = false;

    public WorldGenerator(Plugin plugin, String arenaName) {
        this.plugin = plugin;
        WorldGenerator.arenaName = arenaName;
        Bukkit.getScheduler().runTaskLater(plugin, this::startGeneration, 20L);
    }

    public static boolean isGenerating() {
        return isGenerating;
    }

    private void startGeneration() {
        if (isGenerating) return;

        if (DebugLog.devMode()) {
            loadDevWorlds();
            return;
        }

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
        world.getWorldBorder().setSize(UHCManager.get().getBorderDefaultSize());

        resetSpawnChunks(world);
        int radius = (int) (world.getWorldBorder().getSize() / 2);
        if (UHCWorldSettings.appliedChunkGenerator() != null) {
            LoadingChunkTask.create(world, Common.get().getNether(), radius);
            return;
        }
        WaterFixer waterFixer = new WaterFixer(plugin);
        waterFixer.fixLiquids(world);
        CenterType chosen = CenterType.getApplied();
        if (chosen != null){
            new WorldPopulator(world, chosen, chosen.getBiome());
        }else if (ScenarioManager.get().getActiveScenarios().stream().anyMatch(Scenario::needRooft)){
            new WorldPopulator(world, CenterType.ROOFT, Biome.ROOFED_FOREST);
        }else{
            LoadingChunkTask.create(world, Common.get().getNether(), radius);
        }

    }

    private void loadDevWorlds() {
        log("§eMode dev : chargement de la map existante, sans régénération ni prégénération.");
        isGenerating = true;

        WorldCreator netherCreator = new WorldCreator(arenaName + "_nether");
        netherCreator.environment(World.Environment.NETHER);
        netherCreator.type(WorldType.NORMAL);
        netherCreator.createWorld();

        WorldCreator endCreator = new WorldCreator(arenaName + "_the_end");
        endCreator.environment(World.Environment.THE_END);
        endCreator.type(WorldType.NORMAL);
        endCreator.createWorld();

        World world = UHCWorldSettings.createUHCWorld(arenaName);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(6000L);
        world.getWorldBorder().setSize(DEV_WORLD_RADIUS);

        isGenerating = false;
        isMapReady = true;
        logSuccess("Map dev prête (bordure " + DEV_WORLD_RADIUS + " blocs).");
        Bukkit.getPluginManager().callEvent(new UhcWorldEvents.ArenaRegeneratedEvent(world));
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
                    player.teleport(Common.get().getLobbySpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN)
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

        LangManager.get().sendAll(CoreLang.TASK_NETHER_GENERATION);

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
        safeDeleteWorld(endName);

        LangManager.get().sendAll(CoreLang.TASK_END_GENERATION);

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

