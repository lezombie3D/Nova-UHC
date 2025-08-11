package net.novaproject.novauhc.scenario.special.fallenkigdom;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.task.LoadingChunkTask;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.TeamZone;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.world.utils.LobbyCreator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
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

public class FallenKingdom extends Scenario {

    public static FallenKingdom instance;
    private final Map<UHCTeam, TeamZone> teamZones = new HashMap<>();
    private final int ZONE_SIZE = 30;
    private final int[][] zoneCoordinates = {
            {348, -113}, {216, 294}, {0, -367}, {-216, 294},
            {-348, -113}
    };
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
        instance = this;
    }

    @Override
    public String getName() {
        return "FallenKingdom";
    }

    @Override
    public String getDescription() {
        return "";
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
            episode = 0;
            captureTimers.clear();
            capturingTeams.clear();
            teamZones.clear();
            LobbyCreator.deleteWorld(Common.get().getArenaName(), Common.get().getLobbySpawn());
            LobbyCreator.cloneWorld("falen", Common.get().getArenaName());
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
            new WorldCreator(Common.get().getArenaName()).createWorld();

        }
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            Bukkit.broadcastMessage(ChatColor.YELLOW + "La taille des équipes a été automatiquement définie à 2 pour le mode FallenKingdom.");
        }
        setupTeamZones();

    }

    @Override
    public boolean hasCustomTeamTchat() {
        return true;
    }

    public String getArrowDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        angle = (angle + 360) % 360;

        if (angle >= 337.5 || angle < 22.5) return "↑";
        if (angle >= 22.5 && angle < 67.5) return "↗";
        if (angle >= 67.5 && angle < 112.5) return "→";
        if (angle >= 112.5 && angle < 157.5) return "↘";
        if (angle >= 157.5 && angle < 202.5) return "↓";
        if (angle >= 202.5 && angle < 247.5) return "↙";
        if (angle >= 247.5 && angle < 292.5) return "←";
        if (angle >= 292.5 && angle < 337.5) return "↖";

        return "?";
    }

    private void setupTeamZones() {
        teamZones.clear();

        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        int teamCount = Math.min(teams.size(), zoneCoordinates.length);

        for (int i = 0; i < teamCount; i++) {
            UHCTeam team = teams.get(i);
            int[] coords = zoneCoordinates[i];

            TeamZone zone = new TeamZone(team, coords[0], coords[1], ZONE_SIZE, "arena");
            teamZones.put(team, zone);

        }
    }

    @Override
    public void onStart(Player player) {
        String message = "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml§8§m----------§r\n" +
                ChatColor.AQUA + "Debut de l'episode : " + episode + "\n" +
                "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml§8§m----------§r";
        player.sendMessage(message);

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

    @Override
    public void sendCustomDeathMessage(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(ChatColor.RED + uhcPlayer.getPlayer().getName() + " a été tué par " + killer.getPlayer().getName());
        });
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

    public void setTeamZone(UHCTeam team, int centerX, int centerZ) {
        TeamZone zone = new TeamZone(team, centerX, centerZ, ZONE_SIZE, "arena");
        teamZones.put(team, zone);
        for (UHCPlayer player : team.getPlayers()) {
            if (player.getPlayer() != null && player.getPlayer().isOnline()) {
                player.getPlayer().sendMessage(ChatColor.GREEN + "Votre zone d'équipe a été définie en x: " +
                        centerX + ", z: " + centerZ + " (taille: " + ZONE_SIZE + "x" + ZONE_SIZE + ")");
            }
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        Player player = uhcPlayer.getPlayer();
        Location deathLocation = player.getLocation();
        player.setGameMode(GameMode.SPECTATOR);
        List<ItemStack> drops = event.getDrops();
        uhcPlayer.setPlaying(true);
        event.setDeathMessage(null);


        for (ItemStack drop : drops) {
            player.getWorld().dropItemNaturally(deathLocation, drop);
        }
        for (Player alive : Bukkit.getOnlinePlayers()) {
            alive.playSound(alive.getLocation(), Sound.WITHER_SPAWN, 1, 1);
        }
        Bukkit.broadcastMessage(Common.get().getServertag() + " Le joueur " + player.getName() + " est mort !");
        event.getDrops().clear();
        if (player.getBedSpawnLocation() == null) {
            player.spigot().respawn();
            World arenaWorld = Bukkit.getWorld("arena");
            player.setGameMode(GameMode.SURVIVAL);
            if (arenaWorld != null) {
                player.teleport(new Location(arenaWorld, 0, 142, 0));
            }

        } else {
            player.teleport(uhcPlayer.getPlayer().getBedSpawnLocation());
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
                new Titles().sendActionText(player.getPlayer(), getArrowDirection(player.getPlayer().getLocation(), getTeamZone(player.getTeam().get()).getSpawn()));
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

                Bukkit.broadcastMessage(
                        "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml" + "§8§m----------§r\n" +
                                ChatColor.AQUA + "Début de l'épisode : " + episode + "\n" +
                                "§8§m---------" + ChatColor.MAGIC + "jdsqjlkmsqjsqjml" + "§8§m----------§r"
                );
            }

            if (episode == netherep && !netheract) {
                netheract = true;
                Location fireLoc = new Location(Bukkit.getWorld("arena"), 0, 140, 0);
                Block block = Bukkit.getWorld("arena").getBlockAt(fireLoc);

                if (block.getType() == Material.AIR) {
                    block.setType(Material.FIRE);
                }
                Bukkit.broadcastMessage(ChatColor.RED + "Activation du nether");
            }


            if (episode == endep && !endact) {
                endact = true;
                int y = 156;
                int[][] coords = {
                        {1, -1}, {1, 0}, {1, 1},
                        {-1, -1}, {-1, 0}, {-1, 1},
                        {0, -1}, {0, 0}, {0, 1}
                };

                for (int[] coord : coords) {
                    Bukkit.getWorld("arena").getBlockAt(new Location(Bukkit.getWorld("arena"), coord[0], y, coord[1])).setType(Material.ENDER_PORTAL);
                }

                Bukkit.broadcastMessage(ChatColor.RED + "END activée");
            }


            if (episode == assautep && !annonce) {
                annonce = true;
                Bukkit.broadcastMessage(ChatColor.RED + "Les assauts sont activés!");
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
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas casser ce bloc.");
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
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (!uhcPlayer.isPlaying() || !uhcPlayer.getTeam().isPresent()) {
            return;
        }

        UHCTeam playerTeam = uhcPlayer.getTeam().get();
        Location playerLocation = player.getLocation();

        Optional<TeamZone> zoneOpt = getZoneAtLocation(playerLocation);
        if (!zoneOpt.isPresent() || zoneOpt.get().getTeam().equals(playerTeam)) {
            return;
        }
        if (canBuildAtLocation(player, playerLocation)) {
            if (!inzone.contains(player)) {
                player.sendMessage(ChatColor.RED + "Vous êtes dans votre zone");
                inzone.add(player);
            }
        } else {
            if (inzone.contains(player)) {
                player.sendMessage(ChatColor.RED + "Vous n'êtes plus dans votre zone");
                inzone.remove(player);
            }
        }
        TeamZone enemyZone = zoneOpt.get();
        UHCTeam enemyTeam = enemyZone.getTeam();

        boolean enemyHasActivePlayers = false;
        for (UHCPlayer enemyPlayer : enemyTeam.getPlayers()) {
            if (enemyPlayer.isPlaying()) {
                enemyHasActivePlayers = true;
                break;
            }
        }

        if (!enemyHasActivePlayers) {
            return;
        }

        Location chestLocation = null;
        Block currentBlock = playerLocation.getBlock();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block block = currentBlock.getRelative(x, y, z);
                    if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST
                            || block.getType() == Material.ENDER_CHEST) {
                        chestLocation = block.getLocation();
                        break;
                    }
                }
                if (chestLocation != null) break;
            }
            if (chestLocation != null) break;
        }

        if (chestLocation == null) {
            return;
        }

        boolean allTeamMembersNearChest = true;
        int activeTeamMembers = 0;
        for (UHCPlayer teamMember : playerTeam.getPlayers()) {
            if (!teamMember.isPlaying() || teamMember.getPlayer() == null || !teamMember.getPlayer().isOnline()) {
                continue;
            }
            activeTeamMembers++;

            if (!enemyZone.isInZone(teamMember.getPlayer().getLocation())) {
                allTeamMembersNearChest = false;
                break;
            }

            if (teamMember.getPlayer().getLocation().distance(chestLocation) > 5) {
                allTeamMembersNearChest = false;
                break;
            }
        }

        if (activeTeamMembers == 0) {
            return;
        }

        if (allTeamMembersNearChest && episode > assautep - 1) {
            if (!captureTimers.containsKey(enemyTeam) || captureTimers.get(enemyTeam) == 0) {
                int captureTime = 60;
                captureTimers.put(enemyTeam, captureTime);
                capturingTeams.put(enemyTeam, playerTeam);

                captureChestLocations.put(enemyTeam, chestLocation);

                for (UHCPlayer teamMember : playerTeam.getPlayers()) {
                    if (teamMember.getPlayer() != null && teamMember.getPlayer().isOnline()) {
                        teamMember.getPlayer().sendMessage(ChatColor.GREEN + "Votre équipe a commencé à capturer le coffre de l'équipe "
                                + enemyTeam.getName() + ChatColor.GREEN + "! Restez tous près du coffre pendant " + captureTime + " secondes.");
                        teamMember.getPlayer().playSound(teamMember.getPlayer().getLocation(), Sound.LEVEL_UP, 1.0f, 1.0f);
                    }
                }

                for (UHCPlayer enemyMember : enemyTeam.getPlayers()) {
                    if (enemyMember.getPlayer() != null && enemyMember.getPlayer().isOnline()) {
                        enemyMember.getPlayer().sendMessage(ChatColor.RED + "ALERTE! L'équipe " + playerTeam.getName()
                                + ChatColor.RED + " est en train de capturer votre coffre! Vous avez " + captureTime + " secondes pour les arrêter!");
                        enemyMember.getPlayer().playSound(enemyMember.getPlayer().getLocation(), Sound.WITHER_SPAWN, 1.0f, 1.0f);
                    }
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!captureTimers.containsKey(enemyTeam) || !capturingTeams.containsKey(enemyTeam)) {
                            cancel();
                            return;
                        }

                        Location targetChestLocation = captureChestLocations.get(enemyTeam);
                        if (targetChestLocation == null) {
                            cancel();
                            return;
                        }

                        boolean allStillPresent = true;
                        for (UHCPlayer teamMember : playerTeam.getPlayers()) {
                            if (!teamMember.isPlaying() || teamMember.getPlayer() == null || !teamMember.getPlayer().isOnline()) {
                                continue;
                            }

                            if (!enemyZone.isInZone(teamMember.getPlayer().getLocation())) {
                                allStillPresent = false;
                                break;
                            }

                            if (teamMember.getPlayer().getLocation().distance(targetChestLocation) > 5) {
                                allStillPresent = false;
                                break;
                            }
                        }

                        if (!allStillPresent) {
                            captureTimers.remove(enemyTeam);
                            capturingTeams.remove(enemyTeam);
                            captureChestLocations.remove(enemyTeam);

                            for (UHCPlayer teamMember : playerTeam.getPlayers()) {
                                if (teamMember.getPlayer() != null && teamMember.getPlayer().isOnline()) {
                                    teamMember.getPlayer().sendMessage(ChatColor.RED + "Capture annulée! Tous les membres de l'équipe doivent rester près du coffre.");
                                }
                            }
                            cancel();
                        } else {
                            int remainingTime = captureTimers.get(enemyTeam);
                            if (remainingTime > 0) {
                                remainingTime--;
                                captureTimers.put(enemyTeam, remainingTime);

                                if (remainingTime % 10 == 0 || remainingTime <= 5) {
                                    for (UHCPlayer teamMember : playerTeam.getPlayers()) {
                                        if (teamMember.getPlayer() != null && teamMember.getPlayer().isOnline()) {
                                            teamMember.getPlayer().sendMessage(ChatColor.YELLOW + "Capture en cours: " + remainingTime + " secondes restantes.");
                                        }
                                    }

                                    for (UHCPlayer enemyMember : enemyTeam.getPlayers()) {
                                        if (enemyMember.getPlayer() != null && enemyMember.getPlayer().isOnline()) {
                                            enemyMember.getPlayer().sendMessage(ChatColor.RED + "ALERTE! " + remainingTime + " secondes avant l'élimination!");
                                        }
                                    }
                                }
                            } else {
                                for (UHCPlayer enemyMember : enemyTeam.getPlayers()) {
                                    if (enemyMember.isPlaying() && enemyMember.getPlayer() != null && enemyMember.getPlayer().isOnline()) {
                                        enemyMember.getPlayer().sendMessage(ChatColor.RED + "Votre équipe a été éliminée! L'équipe "
                                                + playerTeam.getName() + ChatColor.RED + " a capturé votre coffre!");
                                        enemyMember.getPlayer().getWorld().strikeLightningEffect(enemyMember.getPlayer().getLocation());
                                        enemyMember.getPlayer().playSound(enemyMember.getPlayer().getLocation(), Sound.WITHER_DEATH, 1.0f, 1.0f);
                                        enemyMember.setPlaying(false);
                                        enemyMember.getPlayer().setGameMode(GameMode.SPECTATOR);
                                        TeamsTagsManager.setNameTag(player, "zzzzz", "§8§o[Spec] ", "");

                                    }
                                }

                                for (UHCPlayer teamMember : playerTeam.getPlayers()) {
                                    if (teamMember.getPlayer() != null && teamMember.getPlayer().isOnline()) {
                                        teamMember.getPlayer().sendMessage(ChatColor.GREEN + "Votre équipe a éliminé l'équipe "
                                                + enemyTeam.getName() + ChatColor.GREEN + "!");
                                        teamMember.getPlayer().playSound(teamMember.getPlayer().getLocation(), Sound.ENDERDRAGON_DEATH, 1.0f, 1.0f);
                                    }
                                }

                                Bukkit.broadcastMessage(ChatColor.GOLD + "L'équipe " + playerTeam.getName()
                                        + ChatColor.GOLD + " a éliminé l'équipe " + enemyTeam.getName()
                                        + ChatColor.GOLD + " en capturant leur coffre!");

                                captureTimers.remove(enemyTeam);
                                capturingTeams.remove(enemyTeam);
                                captureChestLocations.remove(enemyTeam);
                                UHCManager.get().checkVictory();
                            }
                        }
                    }
                }.runTaskTimer(Main.get(), 0, 20);
            }
        } else {
            if (captureTimers.containsKey(enemyTeam) && capturingTeams.get(enemyTeam).equals(playerTeam)) {
                captureTimers.remove(enemyTeam);
                capturingTeams.remove(enemyTeam);
                captureChestLocations.remove(enemyTeam);

                for (UHCPlayer teamMember : playerTeam.getPlayers()) {
                    if (teamMember.getPlayer() != null && teamMember.getPlayer().isOnline()) {
                        teamMember.getPlayer().sendMessage(ChatColor.RED + "Capture annulée! Tous les membres de l'équipe doivent être près du coffre.");
                    }
                }
            }
        }
    }


    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (episode < assautep - 1) {
            if (event.getClickedBlock().getType() == Material.TNT) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Les assauts sont désactivé");
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
                event.getPlayer().sendMessage(ChatColor.RED + "Les assauts sont désactivé");
            }
        }
    }

    @Override
    public void onEtityExplose(Entity entity, EntityExplodeEvent event) {
        if (episode < assautep - 1) {
            if (entity.getType().toString().equals("PRIMED_TNT")) {
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
