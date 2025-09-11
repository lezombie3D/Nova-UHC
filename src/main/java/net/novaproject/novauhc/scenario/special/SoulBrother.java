package net.novaproject.novauhc.scenario.special;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.SoulBrotherLang;
import net.novaproject.novauhc.task.ShadowLoadingChunkTask;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.world.utils.LobbyCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SoulBrother extends Scenario {

    private final Map<UUID, World> playerWorlds = new HashMap<>();
    private final Map<UUID, UUID> soulBrothers = new HashMap<>();
    private int REUNION_TIME;
    private boolean reunionTime = false;
    private BukkitTask soulBrotherTask;
    private boolean scatterInitialized = false;

    @Override
    public String getName() {
        return "SoulBrother";
    }

    @Override
    public String getDescription() {
        return "Équipes séparées dans 2 mondes identiques, réunies après 1 heure !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SOUL_SAND);
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
            LobbyCreator.cloneWorld(Common.get().getArenaName(), "world1");
            LobbyCreator.cloneWorld(Common.get().getArenaName(), "world2");
            REUNION_TIME = getConfig().getInt("reunion_time");
            new BukkitRunnable() {
                @Override
                public void run() {
                    new ShadowLoadingChunkTask(Bukkit.getWorld("world1"), 1000);
                    new ShadowLoadingChunkTask(Bukkit.getWorld("world2"), 1000);
                }
            }.runTaskLater(Main.get(), 20 * 5);
        }
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            CommonString.TEAM_REDFINIED_AUTO.sendAll();
        }
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;

        int currentTime = UHCManager.get().getTimer();

        if (!reunionTime && currentTime >= REUNION_TIME) {
            startReunion();
        }

        if (currentTime % 300 == 0 && !reunionTime) {
            sendSoulBrotherUpdate(p);
        }
    }


    @Override
    public String getPath() {
        return "special/soulbrother";
    }

    @Override
    public ScenarioLang[] getLang() {
        return SoulBrotherLang.values();
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (!scatterInitialized) {
            scatterInitialized = true;
            Bukkit.broadcastMessage("§d§l[SoulBrother] §fLes âmes sœurs sont séparées dans des mondes parallèles !");
            startSoulBrotherTask();
        }

        World world1 = Bukkit.getWorld("world1");
        World world2 = Bukkit.getWorld("world2");

        if (world1 == null || world2 == null) {
            Bukkit.broadcastMessage("§c[SoulBrother] Erreur: Les mondes parallèles ne sont pas disponibles !");
            return;
        }

        Player player = uhcPlayer.getPlayer();
        if (player == null) return;

        UHCTeam team = uhcPlayer.getTeam().orElse(null);
        if (team == null || team.getPlayers().size() < 2) {
            player.teleport(location);
            return;
        }

        Location teamLocation = teamloc.get(team);
        if (teamLocation == null) {
            teamLocation = location;
        }

        List<UHCPlayer> members = team.getPlayers();
        int playerIndex = members.indexOf(uhcPlayer);

        if (playerIndex % 2 == 0) {
            playerWorlds.put(player.getUniqueId(), world1);
            teleportToWorld(player, world1, teamLocation);
        } else {
            playerWorlds.put(player.getUniqueId(), world2);
            teleportToWorld(player, world2, teamLocation);

            if (playerIndex > 0) {
                UHCPlayer previousMember = members.get(playerIndex - 1);
                UUID brother1 = previousMember.getPlayer().getUniqueId();
                UUID brother2 = player.getUniqueId();
                soulBrothers.put(brother1, brother2);
                soulBrothers.put(brother2, brother1);
            }
        }
    }

    private void teleportToWorld(Player player, World targetWorld, Location location) {
        Location targetLocation = new Location(targetWorld, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        player.teleport(targetLocation);

        String worldName = targetWorld.getName().equals("world1") ? "Alpha" : "Beta";
        player.sendMessage("§d[SoulBrother] §fVous êtes dans le monde " + worldName + " !");
    }

    private void startSoulBrotherTask() {
        if (soulBrotherTask != null) {
            return;
        }
        this.soulBrotherTask = new BukkitRunnable() {

            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                int currentTime = UHCManager.get().getTimer();

                // Send warnings before reunion
                int timeUntilReunion = REUNION_TIME - currentTime;

                if (timeUntilReunion == 300) { // 5 minutes before
                    Bukkit.broadcastMessage("§d[SoulBrother] §fRéunion des âmes sœurs dans 5 minutes !");
                } else if (timeUntilReunion == 60) { // 1 minute before
                    Bukkit.broadcastMessage("§d[SoulBrother] §fRéunion des âmes sœurs dans 1 minute !");
                } else if (timeUntilReunion == 10) { // 10 seconds before
                    Bukkit.broadcastMessage("§d[SoulBrother] §fRéunion dans 10 secondes !");
                }
            }
        }.runTaskTimer(Main.get(), 0, 20);
    }

    private void startReunion() {
        reunionTime = true;

        Bukkit.broadcastMessage("§d§l[SoulBrother] §fLES ÂMES SŒURS SE RETROUVENT !");

        World reunionWorld = Common.get().getArena();

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();

            Location reunionLoc = findReunionLocation(reunionWorld);
            player.teleport(reunionLoc);

            player.sendMessage("§d[SoulBrother] §fVous avez été réuni avec votre âme sœur !");

            UUID brotherUuid = soulBrothers.get(player.getUniqueId());
            if (brotherUuid != null) {
                Player brother = Bukkit.getPlayer(brotherUuid);
                if (brother != null) {
                    player.sendMessage("§d[SoulBrother] §fVotre âme sœur est §d" + brother.getName() + " §f!");

                    giveReunionBonus(player);
                }
            }
        }

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.ENDERDRAGON_DEATH, 1.0f, 1.5f);
        }
    }

    private Location findReunionLocation(World world) {
        Location spawn = world.getSpawnLocation();

        int x = spawn.getBlockX() + (int) ((Math.random() - 0.5) * 1000);
        int z = spawn.getBlockZ() + (int) ((Math.random() - 0.5) * 1000);
        int y = world.getHighestBlockYAt(x, z) + 1;

        return new Location(world, x, y, z);
    }

    private void giveReunionBonus(Player player) {
        // Give reunion bonus items
        player.getInventory().addItem(
                new org.bukkit.inventory.ItemStack(Material.GOLDEN_APPLE, 2),
                new org.bukkit.inventory.ItemStack(Material.DIAMOND, 1),
                new org.bukkit.inventory.ItemStack(Material.EXP_BOTTLE, 5)
        );

        player.sendMessage("§d[SoulBrother] §fBonus de réunion reçu !");
    }

    private void sendSoulBrotherUpdate(Player player) {
        UUID brotherUuid = soulBrothers.get(player.getUniqueId());
        if (brotherUuid != null) {
            Player brother = Bukkit.getPlayer(brotherUuid);
            if (brother != null && brother.isOnline()) {
                Location brotherLoc = brother.getLocation();
                player.sendMessage("§d[SoulBrother] §fVotre âme sœur " + brother.getName() +
                        " est en " + brotherLoc.getBlockX() + ", " + brotherLoc.getBlockZ() +
                        " (Vie: " + (int) brother.getHealth() + "/20)");

                Location playerLoc = player.getLocation();
                brother.sendMessage("§d[SoulBrother] §fVotre âme sœur " + player.getName() +
                        " est en " + playerLoc.getBlockX() + ", " + playerLoc.getBlockZ() +
                        " (Vie: " + (int) player.getHealth() + "/20)");
            }
        }
    }

    private void reuniteAllPlayers() {
        if (reunionTime) return;

        World mainWorld = Bukkit.getWorld("world");

        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            if (!player.getWorld().equals(mainWorld)) {
                Location reunionLoc = findReunionLocation(mainWorld);
                player.teleport(reunionLoc);
                player.sendMessage("§d[SoulBrother] §fVous avez été ramené au monde principal !");
            }
        }

        reunionTime = true;
    }

    // Get soul brother of a player
    public Player getSoulBrother(Player player) {
        UUID brotherUuid = soulBrothers.get(player.getUniqueId());
        return brotherUuid != null ? Bukkit.getPlayer(brotherUuid) : null;
    }

    // Check if reunion has happened
    public boolean hasReunionHappened() {
        return reunionTime;
    }

    // Get time until reunion
    public int getTimeUntilReunion() {
        if (reunionTime) return 0;

        int currentTime = UHCManager.get().getTimer();
        return Math.max(0, REUNION_TIME - currentTime);
    }

    public void forceReunion() {
        if (isActive() && !reunionTime) {
            startReunion();
            Bukkit.broadcastMessage("§d[SoulBrother] §fRéunion forcée par un administrateur !");
        }
    }

    // Get player's current world assignment
    public String getPlayerWorldAssignment(Player player) {
        World playerWorld = playerWorlds.get(player.getUniqueId());
        if (playerWorld == null) return "Non assigné";

        return playerWorld.getName().equals("world1") ? "Alpha" : "Beta";
    }

    // Check if two players are soul brothers
    public boolean areSoulBrothers(Player player1, Player player2) {
        UUID brother1 = soulBrothers.get(player1.getUniqueId());
        return brother1 != null && brother1.equals(player2.getUniqueId());
    }
}
