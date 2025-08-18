package net.novaproject.novauhc.scenario.special.fallenkigdom;

import lombok.Getter;
import lombok.Setter;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.task.LoadingChunkTask;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.TeamZone;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.ui.CustomInventory;
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

import java.util.*;

@Getter
@Setter
public class FallenKingdom extends Scenario {

    public static FallenKingdom instance;
    private final Map<UHCTeam, TeamZone> teamZones = new HashMap<>();
    private int ZONE_SIZE = 30;
    private List<Location> teamZoneLocations = new ArrayList<>();
    private final Map<UHCTeam, Integer> captureTimers = new HashMap<>();
    private final Map<UHCTeam, UHCTeam> capturingTeams = new HashMap<>();
    boolean annonce = false;
    Set<Player> inzone = new HashSet<>();
    private int episode = 0;
    private final Map<UHCTeam, Location> captureChestLocations = new HashMap<>();
    private int epTimer = 1200;
    private int assautep = 8;
    private int netherep = 4;
    private int endep = 6;
    private boolean endact = false;
    private boolean netheract = false;
    private boolean incheck = false;

    public static FallenKingdom get() {
        return instance;
    }

    @Override
    public void setup() {
        super.setup();
        instance = this;
        ConfigUtils.getLocations(getConfig(), "team_zone_locations").forEach(loc -> {
            if (loc.getWorld() != null) {
                teamZoneLocations.add(loc);
            }
        });
        ZONE_SIZE = getConfig().getInt("zone_size", 30);
    }

    @Override
    public String getPath() {
        return "special/fallenkingdom";
    }

    @Override
    public ScenarioLang[] getLang() {
        return FKLang.values();
    }

    @Override
    public String getName() {
        return "FallenKingdom";
    }

    @Override
    public String getDescription() {
        return "Mode de jeu par équipes avec des bases à défendre et des objectifs de capture.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FEATHER);
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
            episode = 0;
            captureTimers.clear();
            capturingTeams.clear();
            teamZones.clear();
            LobbyCreator.deleteWorld(Common.get().getArenaName(), Common.get().getLobbySpawn());
            LobbyCreator.cloneWorld(getConfig().getString("fk_template"), Common.get().getArenaName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    LoadingChunkTask.create(Common.get().getArena(), Common.get().getNether(), 1000);
                }
            }.runTaskLater(Main.get(), 20 * 5);
            UHCManager.get().setTimerpvp(7200);
            setupTeamZones();
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(Common.get().getLobbySpawn());
            }
            teamZones.clear();
            new WorldGenerator(Main.get(), Common.get().getArenaName());

        }
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            CommonString.TEAM_REDFINIED_AUTO.sendAll();
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
            int x = location.getBlockX();
            int z = location.getBlockZ();

            TeamZone zone = new TeamZone(team, x, z, ZONE_SIZE, Common.get().getArenaName());
            teamZones.put(team, zone);

        }
    }

    @Override
    public void onStart(Player player) {
        ScenarioLangManager.sendAll(FKLang.WELCOME_FK);
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = p.getPlayer();
            if (p.getTeam().isPresent()) {
                UHCTeam pTeam = p.getTeam().get();
                Location Tloc = getTeamZone(pTeam).getSpawn();
                Location PLoc = new Location(Tloc.getWorld(), Tloc.getX(), Tloc.getWorld().getHighestBlockYAt(Tloc.getBlockX(), Tloc.getBlockZ()) + 5, Tloc.getBlockZ());
                player.teleport(PLoc);
            }
        }
        World world = Bukkit.getWorld("arena");
        world.getWorldBorder().setSize(1000);
        world.setGameRuleValue("naturalRegeneration", "true");
        world.setPVP(false);
    }


    private boolean canBuildAtLocation(Player player, Location location) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (!uhcPlayer.isPlaying()) {
            return false;
        }

        if (!uhcPlayer.getTeam().isPresent()) {
            return false;
        }

        UHCTeam playerTeam = uhcPlayer.getTeam().get();

        for (Map.Entry<UHCTeam, TeamZone> entry : teamZones.entrySet()) {
            UHCTeam team = entry.getKey();
            TeamZone zone = entry.getValue();

            if (zone.isInZone(location)) {
                return team.equals(playerTeam);
            }
        }

        return false;
    }

    public Optional<TeamZone> getZoneAtLocation(Location location) {
        for (TeamZone zone : teamZones.values()) {
            if (zone.isInZone(location)) {
                return Optional.of(zone);
            }
        }
        return Optional.empty();
    }

    public TeamZone getTeamZone(UHCTeam team) {
        return teamZones.get(team);
    }


    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Player player = uhcPlayer.getPlayer();
        if (player.getBedSpawnLocation() == null) {
            player.spigot().respawn();
            World arenaWorld = Bukkit.getWorld("arena");
            player.setGameMode(GameMode.SURVIVAL);
            if (arenaWorld != null) {
                player.teleport(new Location(arenaWorld, 0, 142, 0));
            }

        } else {
            TeamZone teamZone = getTeamZone(uhcPlayer.getTeam().get());
            player.teleport(teamZone.getSpawn());
            player.setGameMode(GameMode.SURVIVAL);
            player.spigot().respawn();
        }
        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam uhcTeam = uhcPlayer.getTeam().get();
            TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), uhcTeam.getName(), uhcTeam.getPrefix(), "");
        }
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
            if (player.getTeam().isPresent()) {
                new Titles().sendActionText(player.getPlayer(), player.getArrowDirection(player.getPlayer().getLocation(), getTeamZone(player.getTeam().get()).getSpawn(), player.getPlayer().getLocation().getYaw()));
            }
        }

    }

    private void verifyStates(int timer) {
        if (incheck) return;
        incheck = true;

        try {
            if (timer == epTimer) {
                episode++;
                epTimer += 1200;

                ScenarioLangManager.sendAll(FKLang.ANNONCE_NEW_EPISODE);
            }

            if (episode == netherep && !netheract) {
                netheract = true;
                List<Location> netherPortals = ConfigUtils.getLocations(getConfig(), "nether_portals");
                netherPortals.forEach(location -> {
                    Block block = location.getBlock();
                    block.setType(Material.AIR);
                });

                ScenarioLangManager.sendAll(FKLang.ANNONCE_NETHER);
            }


            if (episode == endep && !endact) {
                endact = true;
                List<Location> enderPortals = ConfigUtils.getLocations(getConfig(), "end_portals");
                enderPortals.forEach(location -> {

                    int y = location.getBlockY();

                    int[][] offsets = {
                            {1, -1}, {1, 0}, {1, 1},
                            {0, -1}, {0, 0}, {0, 1},
                            {-1, -1}, {-1, 0}, {-1, 1}
                    };

                    for (int[] offset : offsets) {
                        int x = location.getBlockX() + offset[0];
                        int z = location.getBlockZ() + offset[1];
                        location.getWorld().getBlockAt(x, y, z).setType(Material.ENDER_PORTAL);
                    }
                });

                ScenarioLangManager.sendAll(FKLang.ANNONCE_END);
            }


            if (episode == assautep && !annonce) {
                annonce = true;
                ScenarioLangManager.sendAll(FKLang.ANNONCE_ASSAUT);
            }
        } finally {
            incheck = false;
        }
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (!canBuildAtLocation(player, block.getLocation()) && !(block.getType() == Material.TNT)) {
            event.setCancelled(true);
            return;
        }

        if (block.getType() == Material.TNT) {
            if (episode < assautep - 1) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (getZoneAtLocation(block.getLocation()).isPresent() && !canBuildAtLocation(player, block.getLocation())) {
            event.setCancelled(true);
        }
        if (block.getType() == Material.MOB_SPAWNER) {
            event.setCancelled(true);
            CommonString.DISABLE_ACTION.send(player);
        }
    }

    @Override
    public boolean isWin() {
        List<UHCTeam> activeTeams = new ArrayList<>();

        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            boolean hasActivePlayers = false;

            for (UHCPlayer player : team.getPlayers()) {
                if (player.isPlaying()) {
                    hasActivePlayers = true;
                    break;
                }
            }

            if (hasActivePlayers) {
                activeTeams.add(team);
            }
        }


        if (activeTeams.size() == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 1.0f, 1.0f);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onMove(Player player, PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (!uhcPlayer.isPlaying() || !uhcPlayer.getTeam().isPresent()) {
            return;
        }

        UHCTeam playerTeam = uhcPlayer.getTeam().get();
        Location playerLocation = player.getLocation();

        Optional<TeamZone> zoneOpt = getZoneAtLocation(playerLocation);
        if (!zoneOpt.isPresent()) return;

        TeamZone enemyZone = zoneOpt.get();
        if (enemyZone.getTeam().equals(playerTeam)) return;

        if (canBuildAtLocation(player, playerLocation)) {
            inzone.add(player);
        } else {
            inzone.remove(player);
        }

        UHCTeam enemyTeam = enemyZone.getTeam();
        boolean enemyHasActivePlayers = enemyTeam.getPlayers().stream().anyMatch(UHCPlayer::isPlaying);
        if (!enemyHasActivePlayers) return;

        Location chestLocation = findNearbyChest(playerLocation, 3);
        if (chestLocation == null) return;

        boolean allTeamMembersNearChest = true;
        int activeTeamMembers = 0;
        for (UHCPlayer teamMember : playerTeam.getPlayers()) {
            Player tmPlayer = teamMember.getPlayer();
            if (!teamMember.isPlaying() || tmPlayer == null || !tmPlayer.isOnline()) continue;

            activeTeamMembers++;
            if (!enemyZone.isInZone(tmPlayer.getLocation()) || tmPlayer.getLocation().distance(chestLocation) > 5) {
                allTeamMembersNearChest = false;
                break;
            }
        }

        if (activeTeamMembers == 0) return;

        if (allTeamMembersNearChest && episode > assautep - 1) {
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%enemy_team%", enemyTeam.getName());
            if (!captureTimers.containsKey(enemyTeam) || captureTimers.get(enemyTeam) == 0) {
                int captureTime = getConfig().getInt("capture_time", 30);
                captureTimers.put(enemyTeam, captureTime);
                capturingTeams.put(enemyTeam, playerTeam);
                captureChestLocations.put(enemyTeam, chestLocation);

                sendTeamMessage(playerTeam, ScenarioLangManager.get(FKLang.CAPTURE_FK, uhcPlayer, placeholders), Sound.LEVEL_UP);

                sendTeamMessage(enemyTeam, ScenarioLangManager.get(FKLang.WARNING_CAPTURE_FK, uhcPlayer, placeholders), Sound.WITHER_SPAWN);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!captureTimers.containsKey(enemyTeam) || !capturingTeams.containsKey(enemyTeam)) {
                            cancel();
                            return;
                        }

                        Location targetChest = captureChestLocations.get(enemyTeam);
                        if (targetChest == null) {
                            cancel();
                            return;
                        }

                        boolean allStillPresent = playerTeam.getPlayers().stream()
                                .filter(p -> p.isPlaying() && p.getPlayer() != null && p.getPlayer().isOnline())
                                .allMatch(p -> enemyZone.isInZone(p.getPlayer().getLocation())
                                        && p.getPlayer().getLocation().distance(targetChest) <= 5);

                        if (!allStillPresent) {
                            resetCapture(enemyTeam, playerTeam, ScenarioLangManager.get(FKLang.CAPTURE_FK_FAIL, uhcPlayer, placeholders));
                            cancel();
                            return;
                        }

                        int remainingTime = captureTimers.get(enemyTeam);
                        placeholders.put("%remaining_time%", String.valueOf(remainingTime));
                        if (remainingTime > 0) {
                            captureTimers.put(enemyTeam, remainingTime - 1);

                            if (remainingTime % 10 == 0 || remainingTime <= 5) {
                                sendTeamMessage(playerTeam, ScenarioLangManager.get(FKLang.REMAINING_CAPTURE_FK, uhcPlayer, placeholders), null);
                                sendTeamMessage(enemyTeam, ScenarioLangManager.get(FKLang.WARNING_REMAIN_CAPTURE_FK, uhcPlayer, placeholders), null);
                            }
                        } else {
                            eliminateTeam(enemyTeam, playerTeam);
                            cancel();
                        }
                    }
                }.runTaskTimer(Main.get(), 0, 20);
            }
        } else {
            if (Objects.equals(capturingTeams.get(enemyTeam), playerTeam)) {
                resetCapture(enemyTeam, playerTeam, ScenarioLangManager.get(FKLang.CAPTURE_FK_FAIL, uhcPlayer));
            }
        }
    }

    private Location findNearbyChest(Location loc, int radius) {
        Block center = loc.getBlock();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.ENDER_CHEST) {
                        return block.getLocation();
                    }
                }
            }
        }
        return null;
    }

    private void sendTeamMessage(UHCTeam team, String msg, Sound sound) {
        for (UHCPlayer member : team.getPlayers()) {
            Player p = member.getPlayer();
            if (p != null && p.isOnline()) {
                p.sendMessage(msg);
                if (sound != null) p.playSound(p.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    private void resetCapture(UHCTeam enemyTeam, UHCTeam playerTeam, String msg) {
        captureTimers.remove(enemyTeam);
        capturingTeams.remove(enemyTeam);
        captureChestLocations.remove(enemyTeam);

        sendTeamMessage(playerTeam, ChatColor.RED + msg, null);
    }

    private void eliminateTeam(UHCTeam enemyTeam, UHCTeam playerTeam) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%enemy_team%", enemyTeam.getName());
        placeholders.put("%capturing_team%", playerTeam.getName());
        for (UHCPlayer enemy : enemyTeam.getPlayers()) {
            Player ep = enemy.getPlayer();
            if (enemy.isPlaying() && ep != null && ep.isOnline()) {
                ep.sendMessage(ScenarioLangManager.get(FKLang.TEAM_ELIMINATION, enemy));
                ep.getWorld().strikeLightningEffect(ep.getLocation());
                ep.playSound(ep.getLocation(), Sound.WITHER_DEATH, 1.0f, 1.0f);
                enemy.setPlaying(false);
                ep.setGameMode(GameMode.SPECTATOR);
                TeamsTagsManager.setNameTag(ep, "zzzzz", "§8§o[Spec] ", "");
            }
        }

        sendTeamMessage(playerTeam, ScenarioLangManager.get(FKLang.KILL_TEAM, playerTeam.getPlayers().get(0), placeholders), Sound.ENDERDRAGON_DEATH);
        Bukkit.broadcastMessage(ScenarioLangManager.get(FKLang.ANNONCE_TEAM_ELIMINATION, playerTeam.getPlayers().get(0), placeholders));

        captureTimers.remove(enemyTeam);
        capturingTeams.remove(enemyTeam);
        captureChestLocations.remove(enemyTeam);

        UHCManager.get().checkVictory();
    }



    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (episode < assautep - 1) {
            if (event.getClickedBlock().getType() == Material.TNT) {
                event.setCancelled(true);
                CommonString.DISABLE_ACTION.send(player);
            }
        }
    }

    @Override
    public void onBlockIgnite(Block block, BlockIgniteEvent event) {
        if (event.getIgnitingBlock() == null) {
            return;
        }
        if (episode < assautep - 1) {
            if (block.getType() == Material.TNT) {
                event.setCancelled(true);
                CommonString.DISABLE_ACTION.send(event.getPlayer());
            }
        }
    }

    @Override
    public void onEtityExplose(Entity entity, EntityExplodeEvent event) {
        if (episode < assautep - 1) {
            if (entity.getType().toString().equals("PRIMED_TNT")) {
                event.setCancelled(true);
            }
            if (entity.getType() == EntityType.CREEPER) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }


    public int getAssaut() {
        return assautep;
    }

    public void setAssaut(int assautep) {
        this.assautep = assautep;
    }

    public int getNether() {
        return netherep;
    }

    public void setNether(int netherep) {
        this.netherep = netherep;
    }

    public int getEnd() {
        return endep;
    }

    public void setEnd(int endep) {
        this.endep = endep;
    }

    @Override
    public CustomInventory getMenu(Player player) {
        return new FallenUi(player);
    }
}
