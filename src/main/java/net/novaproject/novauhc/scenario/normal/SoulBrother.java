package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SoulBrother extends Scenario {

    private final Map<UUID, World> playerWorlds = new HashMap<>();
    private final Map<UUID, UUID> soulBrothers = new HashMap<>();
    private final int REUNION_TIME = 60 * 60; // 1 hour in seconds
    private boolean reunionTime = false;

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
            setupSoulBrothers();
        } else {
            reuniteAllPlayers();
        }
    }

    @Override
    public void onSec(Player p) {
        if (!isActive()) return;

        int currentTime = UHCManager.get().getTimer();

        // Check if it's time for reunion
        if (!reunionTime && currentTime >= REUNION_TIME) {
            startReunion();
        }

        // Send periodic updates to soul brothers
        if (currentTime % 300 == 0 && !reunionTime) { // Every 5 minutes
            sendSoulBrotherUpdate(p);
        }
    }

    private void setupSoulBrothers() {
        List<UHCTeam> teams = UHCTeamManager.get().getTeams();

        if (teams.isEmpty()) {
            Bukkit.broadcastMessage("§d[SoulBrother] §cAucune équipe trouvée ! Le scénario ne peut pas démarrer.");
            return;
        }
        new WorldGenerator(Main.get(), "world1");
        new WorldGenerator(Main.get(), "world2");
        World world1 = Bukkit.getWorld("world1");
        World world2 = Bukkit.getWorld("world2");

        if (world1 == null || world2 == null) {
            Bukkit.broadcastMessage("§d[SoulBrother] §cMondes non disponibles ! Utilisation du monde principal.");
            return;
        }

        // Copy world seed to ensure identical terrain
        world2.getWorldFolder().mkdirs();

        Bukkit.broadcastMessage("§d§l[SoulBrother] §fLes âmes sœurs sont séparées dans des mondes parallèles !");

        // Separate team members
        for (UHCTeam team : teams) {
            List<UHCPlayer> members = team.getPlayers();

            if (members.size() >= 2) {
                // Split team members between worlds
                for (int i = 0; i < members.size(); i++) {
                    UHCPlayer member = members.get(i);
                    Player player = member.getPlayer();

                    if (i % 2 == 0) {
                        // Even index players go to world 1
                        playerWorlds.put(player.getUniqueId(), world1);
                        teleportToWorld(player, world1);
                    } else {
                        // Odd index players go to world 2
                        playerWorlds.put(player.getUniqueId(), world2);
                        teleportToWorld(player, world2);

                        // Link soul brothers
                        if (i > 0) {
                            UUID brother1 = members.get(i - 1).getPlayer().getUniqueId();
                            UUID brother2 = player.getUniqueId();
                            soulBrothers.put(brother1, brother2);
                            soulBrothers.put(brother2, brother1);
                        }
                    }
                }
            }
        }

        startSoulBrotherTask();
    }

    private void teleportToWorld(Player player, World targetWorld) {
        Location spawnLoc = targetWorld.getSpawnLocation();

        spawnLoc.add(
                (Math.random() - 0.5) * 1000,
                0,
                (Math.random() - 0.5) * 1000
        );

        spawnLoc.setY(targetWorld.getHighestBlockYAt(spawnLoc) + 1);

        player.teleport(spawnLoc);

        String worldName = targetWorld.getName().equals("world") ? "Alpha" : "Beta";
        player.sendMessage("§d[SoulBrother] §fVous êtes dans le monde " + worldName + " !");
    }

    private void startSoulBrotherTask() {
        new BukkitRunnable() {
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

        // Create reunion world (use main world)
        World reunionWorld = Bukkit.getWorld("world");

        // Teleport all players to reunion world
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();

            // Find a safe reunion location
            Location reunionLoc = findReunionLocation(reunionWorld);
            player.teleport(reunionLoc);

            player.sendMessage("§d[SoulBrother] §fVous avez été réuni avec votre âme sœur !");

            // Notify about soul brother
            UUID brotherUuid = soulBrothers.get(player.getUniqueId());
            if (brotherUuid != null) {
                Player brother = Bukkit.getPlayer(brotherUuid);
                if (brother != null) {
                    player.sendMessage("§d[SoulBrother] §fVotre âme sœur est §d" + brother.getName() + " §f!");

                    // Give reunion bonus
                    giveReunionBonus(player);
                }
            }
        }

        // Play reunion sound
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Player player = uhcPlayer.getPlayer();
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.ENDERDRAGON_DEATH, 1.0f, 1.5f);
        }
    }

    private Location findReunionLocation(World world) {
        Location spawn = world.getSpawnLocation();

        // Random location within 500 blocks of spawn
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
                // Send location update
                Location brotherLoc = brother.getLocation();
                player.sendMessage("§d[SoulBrother] §fVotre âme sœur " + brother.getName() +
                        " est en " + brotherLoc.getBlockX() + ", " + brotherLoc.getBlockZ() +
                        " (Vie: " + (int) brother.getHealth() + "/20)");

                // Send to brother too
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

        return playerWorld.getName().equals("world") ? "Alpha" : "Beta";
    }

    // Check if two players are soul brothers
    public boolean areSoulBrothers(Player player1, Player player2) {
        UUID brother1 = soulBrothers.get(player1.getUniqueId());
        return brother1 != null && brother1.equals(player2.getUniqueId());
    }
}
