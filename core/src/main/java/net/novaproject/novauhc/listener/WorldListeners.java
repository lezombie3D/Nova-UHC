package net.novaproject.novauhc.listener;

import org.bukkit.generator.ChunkGenerator;
import net.novaproject.novauhc.world.generation.UHCWorldSettings;
import net.novaproject.novauhc.world.generation.TerrainChunkGenerator;
import net.novaproject.novauhc.world.generation.BiomeMapChunkManager;
import java.util.concurrent.ThreadLocalRandom;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.world.generation.CaveBooster;
import net.novaproject.novauhc.world.generation.DistanceWorldChunkManager;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.WorldInitEvent;

public final class WorldListeners {

    private WorldListeners() {
    }

    public static class WorldInitListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onWorldInit(WorldInitEvent event) {
            World world = event.getWorld();
            if (!isUhcWorld(world)) return;
            if (world.getEnvironment() != World.Environment.NORMAL) return;

            if (!installBiomeMapping(world)) {
                DistanceWorldChunkManager.install(world);
            }

            UHCManager m = UHCManager.get();
            CaveBooster.install(world, m.getCaveMultiplier(), m.getRavineMultiplier());

            for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
                BlockPopulator populator = scenario.getPopulator(world);
                if (populator != null) world.getPopulators().add(populator);
            }
        }

        private static boolean installBiomeMapping(World world) {
            ChunkGenerator generator = UHCWorldSettings.appliedChunkGenerator();
            if (!(generator instanceof TerrainChunkGenerator terrain)) return false;
            return BiomeMapChunkManager.install(world, terrain.biomeMapping());
        }

        private static boolean isUhcWorld(World world) {
            Common common = Common.get();
            if (common == null) return false;
            String arena = common.getArenaName();
            if (arena == null) return false;
            String name = world.getName();
            return name.equals(arena)
                    || name.equals(arena + "_nether")
                    || name.equals(arena + "_the_end");
        }
    }

    public static class RedstoneBlocker implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onRedstone(BlockRedstoneEvent event) {
            event.setNewCurrent(event.getOldCurrent());
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPistonExtend(BlockPistonExtendEvent event) {
            event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPistonRetract(BlockPistonRetractEvent event) {
            event.setCancelled(true);
        }
    }

    public static class SurfaceSpawnListener implements Listener {

        private static final int SURFACE_SKY_LIGHT = 8;

        @EventHandler(ignoreCancelled = true)
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            SpawnReason reason = event.getSpawnReason();
            if (reason != SpawnReason.NATURAL && reason != SpawnReason.CHUNK_GEN) return;

            UHCManager uhc = UHCManager.get();
            if (uhc == null) return;

            if (uhc.isEntityDisabled(event.getEntityType())) {
                event.setCancelled(true);
                return;
            }

            Common common = Common.get();
            if (common == null) return;

            World arena = common.getArena();
            if (arena == null || !arena.equals(event.getLocation().getWorld())) return;

            if (event.getEntity() instanceof Monster || event.getEntity() instanceof Slime) {
                if (reason != SpawnReason.NATURAL) return;
                if (event.getLocation().getBlock().getLightFromSky() < SURFACE_SKY_LIGHT) return;
                roll(event, uhc.getSurfaceHostileSpawnPercent());
                return;
            }

            roll(event, uhc.getPassiveSpawnPercent());
        }

        private void roll(CreatureSpawnEvent event, double percent) {
            if (percent >= 1.0) return;
            if (ThreadLocalRandom.current().nextDouble() >= percent) {
                event.setCancelled(true);
            }
        }
    }
}

