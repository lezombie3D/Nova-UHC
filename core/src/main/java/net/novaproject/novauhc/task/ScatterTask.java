package net.novaproject.novauhc.task;

import net.novaproject.novauhc.world.SpawnPlatform;
import net.novaproject.novauhc.utils.UHCUtils.ServerMonitor;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.event.UhcGameEvents.UhcScatterEndEvent;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.game.GameLoop.GameTask;
import net.novaproject.novauhc.game.Lifecycles;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.player.utils.InventorySnapshots;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ScatterTask extends BukkitRunnable {

    public static final Set<UUID> scattered = ConcurrentHashMap.newKeySet();

    private static final int MAX_SAFE_RETRIES = 30;
    private static final double MIN_SPAWN_DISTANCE = 50.0;
    private static final double MIN_SPAWN_DISTANCE_SQ = MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE;
    private static final double TPS_THRESHOLD = 19.0;
    private static final int TPS_MAX_POLLS = 80;

    private final List<UHCPlayer> uhcPlayers;
    private final List<Location> locations = new ArrayList<>();
    private final HashMap<UHCTeam, Location> teamloc = new HashMap<>();
    private final List<SpawnPlatform> platforms = new ArrayList<>();
    private int tpsPolls = -1;

    public ScatterTask() {
        this.uhcPlayers = new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers());
        scattered.clear();
    }

    public static void giveStartInv(Player player, Map<String, ItemStack[]> savedInventory) {
        if (player == null) return;
        InventorySnapshots.restorePlayerInventory(player, savedInventory);
    }

    private static void broadcastSound(Sound sound) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online != null && online.isOnline()) {
                online.playSound(online.getLocation(), sound, 1f, 1f);
            }
        }
    }

    @Override
    public void run() {
        if (uhcPlayers.isEmpty()) {
            tpsPolls++;
            if (currentTps() >= TPS_THRESHOLD || tpsPolls >= TPS_MAX_POLLS) {
                onEndScatter();
                return;
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online != null && online.isOnline()) {
                    DisplayService.actionBar(online,
                            LangManager.get().get(CoreLang.TASK_SCATTER_TPS_WAIT, online));
                }
            }
            return;
        }

        UHCPlayer uhcPlayer = uhcPlayers.remove(0);
        onScatter(uhcPlayer);
    }

    private double currentTps() {
        return ServerMonitor.serverTps();
    }

    private Location buildPlatformAt(World world, Location ground) {
        FileConfiguration config = ConfigUtils.getGeneralConfig();
        int height = Math.max(config.getInt("start-platforms.height", 30), 5);
        int size = Math.max(config.getInt("start-platforms.size", 3), 1);
        int glassColor = config.getInt("start-platforms.glass-color", 14);

        int groundY = world.getHighestBlockYAt(ground.getBlockX(), ground.getBlockZ());
        int y = Math.min(groundY + height, 250);
        Location center = new Location(world, ground.getBlockX(), y, ground.getBlockZ());

        SpawnPlatform platform =
                new SpawnPlatform(center, size, glassColor);
        platform.build();
        platforms.add(platform);
        return platform.getSpawnLocation();
    }

    private void dropPlatforms() {
        if (platforms.isEmpty()) return;
        platforms.forEach(SpawnPlatform::destroy);
        platforms.clear();
        broadcastSound(Sound.EXPLODE);
    }

    private void preloadChunks(Location location) {
        if (location == null || location.getWorld() == null) return;
        int cx = location.getBlockX() >> 4;
        int cz = location.getBlockZ() >> 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                location.getWorld().getChunkAt(cx + dx, cz + dz).load(true);
            }
        }
    }

    private void onEndScatter() {
        cancel();

        broadcastSound(Sound.FIREWORK_TWINKLE);

        UHCManager.get().setGameState(UHCManager.GameState.INGAME);
        scattered.clear();

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            UHCPlayer up = UHCPlayerManager.get().getPlayer(onlinePlayer);
            if (up != null && up.isPlaying() && up.getTeam().isPresent()) {
                UHCTeam team = up.getTeam().get();
                DisplayService.nametag(onlinePlayer, team.name(), team.prefix(), "");
            } else {
                RankManager.get().applyTag(onlinePlayer);
            }
        });

        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.TASK_GAME_START, null, Map.of("%servertag%", Common.get().getServertag())));

        dropPlatforms();

        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(uhcPlayer -> {
            Player player = uhcPlayer.getPlayer();
            if (player != null && player.isOnline()) {
                if (!UHCManager.get().start.isEmpty()) {
                    giveStartInv(player, UHCManager.get().start);
                }
            }
        });

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(uhcPlayer -> {
                Player player = uhcPlayer.getPlayer();
                if (player != null && player.isOnline()) {
                    scenario.onStart(player);
                }
            });
        });

        ScenarioManager.get().getActiveScenarios().forEach(Scenario::onGameStart);

        Lifecycles.startAll();

        new GameTask().runTaskTimer(Main.get(), 0, 20L);

        Bukkit.getPluginManager().callEvent(new UhcScatterEndEvent());
    }

    private void onScatter(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        if (player == null || !player.isOnline()) {
            uhcPlayer.setPlaying(false);
            return;
        }

        World world = Common.get().getArena();
        WorldBorder worldBorder = world.getWorldBorder();
        world.setDifficulty(Difficulty.HARD);
        world.setGameRuleValue("doDaylightCycle", "true");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("naturalRegeneration", "false");

        Location chosen = uhcPlayer.getTeam()
                .map(team -> teamloc.get(team))
                .orElse(null);

        if (chosen == null) {
            chosen = scenarioSpawn(world);
            if (chosen == null) {
                chosen = findSafeLocation(world, worldBorder);
                if (UHCManager.get().isStartPlatformsEnabled()) {
                    chosen = buildPlatformAt(world, chosen);
                }
            }
            locations.add(chosen);
            Location finalLoc = chosen;
            uhcPlayer.getTeam().ifPresent(team -> teamloc.put(team, finalLoc));
        }

        final Location location = chosen;
        preloadChunks(location);
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.scatter(uhcPlayer, location, teamloc);
        });

        if (ScenarioManager.get().getActiveSpecialScenarios().isEmpty()) {
            if (UHCManager.get().getTeam_size() != 1 && uhcPlayer.getTeam().isPresent()) {
                UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            } else {
                player.teleport(location);
            }
        }

        player.setGameMode(GameMode.SURVIVAL);
        InventorySnapshots.clearPlayerInventory(player);
        player.updateInventory();
        scattered.add(uhcPlayer.getUuid());

        LangManager.get().sendAll(CoreLang.COMMON_TP_MESSAGE, Map.of("%player%", player.getName()));

        broadcastSound(Sound.NOTE_STICKS);
    }

    private Location scenarioSpawn(World world) {
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            Location spawn = scenario.resolveSpawn(world);
            if (spawn != null) return spawn;
        }
        return null;
    }

    private Location findSafeLocation(World world, WorldBorder worldBorder) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double radius = worldBorder.getSize() / 2.0 - 5.0;
        if (radius < 10.0) radius = 10.0;
        double centerX = worldBorder.getCenter().getX();
        double centerZ = worldBorder.getCenter().getZ();

        Location fallback = null;
        for (int attempt = 0; attempt < MAX_SAFE_RETRIES; attempt++) {
            double x = centerX + (random.nextDouble() * 2 - 1) * radius;
            double z = centerZ + (random.nextDouble() * 2 - 1) * radius;
            int bx = (int) Math.floor(x);
            int bz = (int) Math.floor(z);

            int y = world.getMaxHeight() - 1;
            while (y > 0 && world.getBlockAt(bx, y, bz).getType() == Material.AIR) y--;
            if (y <= 0) continue;

            Block surface = world.getBlockAt(bx, y, bz);
            if (surface.isLiquid() || isHazard(surface.getType())) continue;

            Location candidate = new Location(world, bx + 0.5, y + 1.0, bz + 0.5);
            if (fallback == null) fallback = candidate;

            boolean tooClose = false;
            for (Location existing : locations) {
                if (existing.getWorld() != candidate.getWorld()) continue;
                if (existing.distanceSquared(candidate) < MIN_SPAWN_DISTANCE_SQ) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;

            return candidate;
        }
        return fallback != null ? fallback : new Location(world, centerX, world.getHighestBlockYAt((int) centerX, (int) centerZ) + 1.0, centerZ);
    }

    private boolean isHazard(Material type) {
        return type == Material.CACTUS || type == Material.FIRE;
    }
}