package net.novaproject.ultimate.fallenkigdom;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CommonLang;

import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.special.FKLang;
import net.novaproject.novauhc.scenario.Scenario;

import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.task.LoadingChunkTask;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.TeamZone;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.*;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import net.novaproject.novauhc.world.utils.LobbyCreator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

@Getter
@Setter
public class FallenKingdom extends Scenario {

    public static FallenKingdom instance;

    @ScenarioVariable(lang = ScenarioVarLang.class,
            nameKey = "FK_VAR_ZONE_SIZE_NAME",
            descKey = "FK_VAR_ZONE_SIZE_DESC",
            type = VariableType.INTEGER)
    private int zoneSize = 30;

    @ScenarioVariable(lang = ScenarioVarLang.class,
            nameKey = "FK_VAR_EP_TIMER_NAME",
            descKey = "FK_VAR_EP_TIMER_DESC",
            type = VariableType.TIME)
    private int epTimer = 1200;

    @ScenarioVariable(lang = ScenarioVarLang.class,
            nameKey = "FK_VAR_ASSAUT_EP_NAME",
            descKey = "FK_VAR_ASSAUT_EP_DESC",
            type = VariableType.INTEGER)
    private int assautep = 8;

    @ScenarioVariable(lang = ScenarioVarLang.class,
            nameKey = "FK_VAR_NETHER_EP_NAME",
            descKey = "FK_VAR_NETHER_EP_DESC",
            type = VariableType.INTEGER)
    private int netherep = 4;

    @ScenarioVariable(lang = ScenarioVarLang.class,
            nameKey = "FK_VAR_END_EP_NAME",
            descKey = "FK_VAR_END_EP_DESC",
            type = VariableType.INTEGER)
    private int endep = 6;


    private final Map<UHCTeam, TeamZone> teamZones = new HashMap<>();
    private final Map<UHCTeam, Integer> captureTimers = new HashMap<>();
    private final Map<UHCTeam, UHCTeam> capturingTeams = new HashMap<>();
    private final Map<UHCTeam, Location> captureChestLocations = new HashMap<>();
    private final Map<UHCTeam, BukkitTask> activeCaptureTask = new HashMap<>();
    private final Set<Player> inzone = new HashSet<>();

    private List<Location> teamZoneLocations = new ArrayList<>();
    private int episode = 0;
    private int epTimerCurrent = 1200;
    private boolean endact = false;
    private boolean netheract = false;
    private boolean annonce = false;
    private volatile boolean incheck = false;

    public static FallenKingdom get() {
        return instance;
    }

    @Override
    public void setup() {
        super.setup();
        instance = this;
        teamZoneLocations.clear();
        ConfigUtils.getLocations(getConfig(), "team_zone_locations").forEach(loc -> {
            if (loc.getWorld() != null) {
                teamZoneLocations.add(loc);
            }
        });
    }

    @Override
    public String getPath() {
        return "special/fallenkingdom";
    }

    @Override
    public String getName() {
        return "FallenKingdom";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.FK, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FEATHER);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            UHCManager.get().setTeam_size(2);
            for (int i = 0; i < teamZoneLocations.size(); i++) {
                UHCTeamManager.get().deleteTeams();
                UHCTeamManager.get().createTeam(UHCManager.get().getTeam_size());
            }
            resetState();
            LobbyCreator.deleteWorld(Common.get().getArenaName(), Common.get().getLobbySpawn());
            LobbyCreator.cloneWorld(getConfig().getString("fk_template"), Common.get().getArenaName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    if(LobbyCreator.worldsBeingTasked.isEmpty()){
                        cancel();
                        LoadingChunkTask.create(Common.get().getArena(), Common.get().getNether(), 1000);
                        UHCManager.get().setTimerpvp(7200);
                        setupTeamZones();
                        Common.get().getArena().getWorldBorder().setSize(1000);
                        UHCManager.get().setTimerborder(-2);
                    }
                }
            }.runTaskTimer(Main.get(),0,20);

        } else {
            cancelAllCaptureTasks();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(Common.get().getLobbySpawn());
            }
            teamZones.clear();
            new WorldGenerator(Main.get(), Common.get().getArenaName());
            UHCManager.get().setTimerborder(1200);
        }
    }

    private void resetState() {
        episode = 0;
        epTimerCurrent = epTimer; 
        annonce = false;
        endact = false;
        netheract = false;
        captureTimers.clear();
        capturingTeams.clear();
        captureChestLocations.clear();
        teamZones.clear();
        cancelAllCaptureTasks();
    }


    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            LangManager.get().sendAll(CommonLang.TEAM_REDFINIED_AUTO);
        }
        for (int i = 0; i < teamZoneLocations.size(); i++) {
            UHCTeamManager.get().deleteTeams();
            UHCTeamManager.get().createTeam(UHCManager.get().getTeam_size());
        }
        setupTeamZones();
    }

    private void setupTeamZones() {
        teamZones.clear();
        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        int teamCount = Math.min(teams.size(), teamZoneLocations.size());

        for (int i = 0; i < teamCount; i++) {
            UHCTeam team = teams.get(i);
            Location location = teamZoneLocations.get(i);
            TeamZone zone = new TeamZone(
                    team,
                    location.getBlockX(),
                    location.getBlockZ(),
                    zoneSize, 
                    Common.get().getArenaName()
            );
            teamZones.put(team, zone);
        }
    }

    public TeamZone getTeamZone(UHCTeam team) {
        return teamZones.get(team);
    }

    public Optional<TeamZone> getZoneAtLocation(Location location) {
        for (TeamZone zone : teamZones.values()) {
            if (zone.isInZone(location)) {
                return Optional.of(zone);
            }
        }
        return Optional.empty();
    }

    
    
    

    @Override
    public void onStart(Player player) {
        LangManager.get().sendAll(FKLang.WELCOME_FK);
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = p.getPlayer();
            if (player == null) continue;

            if (p.getTeam().isPresent()) {
                TeamZone zone = getTeamZone(p.getTeam().get());
                if (zone == null) continue;

                Location spawn = zone.getSpawn();
                World world = spawn.getWorld();
                if (world == null) continue;

                int highY = world.getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 2;
                player.teleport(new Location(world, spawn.getX(), highY, spawn.getZ()));
            }
        }

        World world = Bukkit.getWorld(Common.get().getArenaName());
        if (world != null) {
            world.getWorldBorder().setSize(1000);
            world.setGameRuleValue("naturalRegeneration", "true");
            world.setPVP(false);
        }
    }

    
    
    

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Player player = uhcPlayer.getPlayer();
        if (player == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                player.spigot().respawn();
                player.setGameMode(GameMode.SURVIVAL);

                if (uhcPlayer.getTeam().isPresent()) {
                    TeamZone teamZone = getTeamZone(uhcPlayer.getTeam().get());
                    if (teamZone != null) {
                        player.teleport(teamZone.getSpawn());
                    }
                    UHCTeam team = uhcPlayer.getTeam().get();
                    TeamsTagsManager.setNameTag(player, team.name(), team.prefix(), "");
                } else {
                    World arenaWorld = Bukkit.getWorld(Common.get().getArenaName());
                    if (arenaWorld != null) {
                        player.teleport(new Location(arenaWorld, 0, 142, 0));
                    }
                }
            }
        }.runTaskLater(Main.get(), 2L);
    }

    @Override
    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
        if (entity instanceof Creeper) {
            entity.getWorld().dropItemNaturally(entity.getLocation(), new ItemStack(Material.TNT));
        }
    }

    
    
    

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        verifyStates(timer);

        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player bp = player.getPlayer();
            if (bp == null || !bp.isOnline()) continue;
            if (!player.getTeam().isPresent()) continue;

            TeamZone zone = getTeamZone(player.getTeam().get());
            if (zone == null) continue;

            new Titles().sendActionText(bp, player.getArrowDirection(
                    bp.getLocation(), zone.getSpawn(), bp.getLocation().getYaw()
            ));
        }
    }

    private void verifyStates(int timer) {
        if (incheck) return;
        incheck = true;

        try {
            if (timer == epTimerCurrent) {
                episode++;
                epTimerCurrent += epTimer; 
                LangManager.get().sendAll(FKLang.ANNONCE_NEW_EPISODE);
            }

            if (episode >= netherep && !netheract) {
                netheract = true;
                ConfigUtils.getLocations(getConfig(), "nether_portals").forEach(loc -> {
                    loc.getBlock().setType(Material.AIR);
                });
                LangManager.get().sendAll(FKLang.ANNONCE_NETHER);
            }

            if (episode >= endep && !endact) {
                endact = true;
                ConfigUtils.getLocations(getConfig(), "end_portals").forEach(loc -> {
                    int y = loc.getBlockY();
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            loc.getWorld().getBlockAt(
                                    loc.getBlockX() + dx, y, loc.getBlockZ() + dz
                            ).setType(Material.ENDER_PORTAL);
                        }
                    }
                });
                LangManager.get().sendAll(FKLang.ANNONCE_END);
            }

            if (episode >= assautep && !annonce) {
                annonce = true;
                LangManager.get().sendAll(FKLang.ANNONCE_ASSAUT);
            }
        } finally {
            incheck = false;
        }
    }

    
    
    

    private boolean canBuildAtLocation(Player player, Location location) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying() || !uhcPlayer.getTeam().isPresent()) {
            return false;
        }

        UHCTeam playerTeam = uhcPlayer.getTeam().get();

        for (Map.Entry<UHCTeam, TeamZone> entry : teamZones.entrySet()) {
            if (entry.getValue().isInZone(location)) {
                return entry.getKey().equals(playerTeam);
            }
        }
        return true;
    }

    private boolean isAssaultStarted() {
        return episode >= assautep;
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (block.getType() == Material.TNT && !isAssaultStarted()) {
            event.setCancelled(true);
            LangManager.get().send(FKLang.TNT_DISABLED, player);
            return;
        }

        if (!canBuildAtLocation(player, block.getLocation())) {
            event.setCancelled(true);
            LangManager.get().send(FKLang.BUILD_DENIED, player);
        }
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (block.getType() == Material.MOB_SPAWNER) {
            event.setCancelled(true);
            LangManager.get().send(FKLang.DISABLE_ACTION, player);
            return;
        }

        if (getZoneAtLocation(block.getLocation()).isPresent()
                && !canBuildAtLocation(player, block.getLocation())) {
            event.setCancelled(true);
            LangManager.get().send(FKLang.BUILD_DENIED, player);
        }
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        if (!isAssaultStarted() && event.getClickedBlock().getType() == Material.TNT) {
            event.setCancelled(true);
            LangManager.get().send(FKLang.TNT_DISABLED, player);
        }
    }

    @Override
    public void onBlockIgnite(Block block, BlockIgniteEvent event) {
        if (event.getIgnitingBlock() == null) return;

        if (!isAssaultStarted() && block.getType() == Material.TNT) {
            event.setCancelled(true);
            if (event.getPlayer() != null) {
                LangManager.get().send(FKLang.TNT_DISABLED, event.getPlayer());
            }
        }
    }

    @Override
    public void onEtityExplose(Entity entity, EntityExplodeEvent event) {
        if (isAssaultStarted()) return;

        if (entity.getType().toString().equals("PRIMED_TNT")
                || entity.getType() == EntityType.CREEPER) {
            event.setCancelled(true);
        }
    }

    
    
    

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || !uhcPlayer.isPlaying() || !uhcPlayer.getTeam().isPresent()) return;

        UHCTeam playerTeam = uhcPlayer.getTeam().get();
        Location playerLocation = player.getLocation();

        Optional<TeamZone> zoneOpt = getZoneAtLocation(playerLocation);
        if (!zoneOpt.isPresent()) return;

        TeamZone enemyZone = zoneOpt.get();
        UHCTeam enemyTeam = enemyZone.getTeam();

        if (enemyTeam.equals(playerTeam)) return;
        if (!isAssaultStarted()) return;

        boolean enemyAlive = enemyTeam.getPlayers().stream().anyMatch(UHCPlayer::isPlaying);
        if (!enemyAlive) return;

        Location chestLocation = findNearbyChest(playerLocation, 3);
        if (chestLocation == null) return;

        boolean allNearChest = isEntireTeamNearChest(playerTeam, enemyZone, chestLocation);

        if (allNearChest) {
            startCaptureIfAbsent(enemyTeam, playerTeam, enemyZone, chestLocation);
        } else {
            if (Objects.equals(capturingTeams.get(enemyTeam), playerTeam)) {
                cancelCapture(enemyTeam, playerTeam);
            }
        }
    }

    private boolean isEntireTeamNearChest(UHCTeam team, TeamZone zone, Location chest) {
        int activeMembers = 0;
        for (UHCPlayer member : team.getPlayers()) {
            Player p = member.getPlayer();
            if (!member.isPlaying() || p == null || !p.isOnline()) continue;
            activeMembers++;
            if (!zone.isInZone(p.getLocation()) || p.getLocation().distance(chest) > 5) {
                return false;
            }
        }
        return activeMembers > 0;
    }

    private void startCaptureIfAbsent(UHCTeam enemyTeam, UHCTeam playerTeam,
                                      TeamZone enemyZone, Location chestLocation) {
        if (activeCaptureTask.containsKey(enemyTeam)) return;

        int captureTime = getConfig().getInt("capture_time", 30);
        captureTimers.put(enemyTeam, captureTime);
        capturingTeams.put(enemyTeam, playerTeam);
        captureChestLocations.put(enemyTeam, chestLocation);

        sendTeamLang(playerTeam, FKLang.CAPTURE_START, Map.of("%enemy_team%", enemyTeam.name()), Sound.LEVEL_UP);
        sendTeamLang(enemyTeam, FKLang.CAPTURE_WARNING, Map.of(), Sound.WITHER_SPAWN);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!captureTimers.containsKey(enemyTeam) || !capturingTeams.containsKey(enemyTeam)) {
                    cleanupCapture(enemyTeam);
                    cancel();
                    return;
                }

                Location targetChest = captureChestLocations.get(enemyTeam);
                if (targetChest == null) {
                    cleanupCapture(enemyTeam);
                    cancel();
                    return;
                }

                if (!isEntireTeamNearChest(playerTeam, enemyZone, targetChest)) {
                    cancelCapture(enemyTeam, playerTeam);
                    cancel();
                    return;
                }

                int remaining = captureTimers.getOrDefault(enemyTeam, 0);

                if (remaining > 0) {
                    captureTimers.put(enemyTeam, remaining - 1);

                    if (remaining % 10 == 0 || remaining <= 5) {
                        Map<String, String> ph = Map.of("%remaining_time%", String.valueOf(remaining));
                        sendTeamLang(playerTeam, FKLang.CAPTURE_PROGRESS, ph, null);
                        sendTeamLang(enemyTeam, FKLang.CAPTURE_PROGRESS_WARNING, ph, null);
                    }
                } else {
                    eliminateTeam(enemyTeam, playerTeam);
                    cancel();
                }
            }
        }.runTaskTimer(Main.get(), 20L, 20L);

        activeCaptureTask.put(enemyTeam, task);
    }

    private void cancelCapture(UHCTeam enemyTeam, UHCTeam playerTeam) {
        sendTeamLang(playerTeam, FKLang.CAPTURE_FAIL, Map.of(), null);
        cleanupCapture(enemyTeam);
    }

    private void cleanupCapture(UHCTeam enemyTeam) {
        captureTimers.remove(enemyTeam);
        capturingTeams.remove(enemyTeam);
        captureChestLocations.remove(enemyTeam);
        BukkitTask task = activeCaptureTask.remove(enemyTeam);
        if (task != null) task.cancel();
    }

    private void cancelAllCaptureTasks() {
        for (BukkitTask task : activeCaptureTask.values()) {
            if (task != null) task.cancel();
        }
        activeCaptureTask.clear();
        captureTimers.clear();
        capturingTeams.clear();
        captureChestLocations.clear();
    }

    
    
    

    private void eliminateTeam(UHCTeam enemyTeam, UHCTeam playerTeam) {
        for (UHCPlayer enemy : enemyTeam.getPlayers()) {
            Player ep = enemy.getPlayer();
            if (!enemy.isPlaying() || ep == null || !ep.isOnline()) continue;

            LangManager.get().send(FKLang.TEAM_ELIMINATED, ep);
            ep.getWorld().strikeLightningEffect(ep.getLocation());
            ep.playSound(ep.getLocation(), Sound.WITHER_DEATH, 1.0f, 1.0f);
            enemy.setPlaying(false);
            ep.setGameMode(GameMode.SPECTATOR);
            TeamsTagsManager.setNameTag(ep, "zzzzz", "§8§lSPEC §r§8", "");
        }

        sendTeamLang(playerTeam, FKLang.TEAM_CAPTURED,
                Map.of("%enemy_team%", enemyTeam.name()), Sound.ENDERDRAGON_DEATH);

        Map<String, String> ph = Map.of(
                "%enemy_team%", enemyTeam.name(),
                "%capturing_team%", playerTeam.name()
        );
        for (Player p : Bukkit.getOnlinePlayers()) {
            String msg = LangManager.get().get(FKLang.TEAM_ELIMINATION_BROADCAST, p);
            for (Map.Entry<String, String> entry : ph.entrySet()) {
                msg = msg.replace(entry.getKey(), entry.getValue());
            }
            p.sendMessage(msg);
        }

        cleanupCapture(enemyTeam);
        UHCManager.get().checkVictory();
    }

    
    
    

    @Override
    public boolean isWin() {
        long activeTeams = UHCTeamManager.get().getTeams().stream()
                .filter(team -> team.getPlayers().stream().anyMatch(UHCPlayer::isPlaying))
                .count();

        if (activeTeams == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 1.0f, 1.0f);
            }
            return true;
        }
        return false;
    }

    
    
    

    private Location findNearbyChest(Location loc, int radius) {
        Block center = loc.getBlock();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Material type = center.getRelative(x, y, z).getType();
                    if (type == Material.CHEST || type == Material.TRAPPED_CHEST
                            || type == Material.ENDER_CHEST) {
                        return center.getRelative(x, y, z).getLocation();
                    }
                }
            }
        }
        return null;
    }

    private void sendTeamLang(UHCTeam team, FKLang lang, Map<String, String> placeholders, Sound sound) {
        for (UHCPlayer member : team.getPlayers()) {
            Player p = member.getPlayer();
            if (p == null || !p.isOnline()) continue;

            String msg = LangManager.get().get(lang, p);
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace(entry.getKey(), entry.getValue());
            }
            p.sendMessage(msg);
            if (sound != null) {
                p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }
}