package net.novaproject.novauhc.task;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ScatterTask extends BukkitRunnable {

    private final List<UHCPlayer> uhcPlayers;
    private final List<Location> locations = new ArrayList<>();
    private final HashMap<UHCTeam, Location> teamloc = new HashMap<>();
    public ScatterTask() {
        this.uhcPlayers = new ArrayList<>(UHCPlayerManager.get().getPlayingOnlineUHCPlayers());
    }

    public static void giveStartInv(Player player, Map<String, ItemStack[]> savedInventory) {
        if (savedInventory == null) return;

        PlayerInventory inv = player.getInventory();

        if (savedInventory.containsKey("armor")) {
            inv.setArmorContents(savedInventory.get("armor"));
        }

        if (savedInventory.containsKey("inventory")) {
            inv.setContents(savedInventory.get("inventory"));
        }
    }

    @Override
    public void run() {
        if (uhcPlayers.isEmpty()) {
            onEndScatter();
            return;
        }

        UHCPlayer uhcPlayer = uhcPlayers.remove(0);
        onScatter(uhcPlayer);
    }

    private void onEndScatter() {
        cancel();

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1);
        });

        UHCManager.get().setGameState(UHCManager.GameState.INGAME);

        Bukkit.broadcastMessage(Common.get().getServertag() + "§aLa partie commence !");


        UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(player -> {
            if (!UHCManager.get().start.isEmpty()) {
                giveStartInv(player.getPlayer(), UHCManager.get().start);
            }
        });

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            UHCPlayerManager.get().getPlayingOnlineUHCPlayers().forEach(uhcPlayer -> {
                scenario.onStart(uhcPlayer.getPlayer());

            });
        });

        new GameTask().runTaskTimer(Main.get(), 0, 20);
    }

    private void onScatter(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        if (player == null) {
            uhcPlayer.setPlaying(false);
            return;
        }

        World world = Common.get().getArena();
        WorldBorder worldBorder = world.getWorldBorder();
        Random random = new Random();

        double radius = worldBorder.getSize() / 2;
        double x = worldBorder.getCenter().getX() + (random.nextDouble() * 2 - 1) * radius;
        double z = worldBorder.getCenter().getZ() + (random.nextDouble() * 2 - 1) * radius;
        double y = world.getHighestBlockYAt((int) x, (int) z);
        world.setDifficulty(Difficulty.HARD);
        world.setGameRuleValue("doDaylightCycle", "true");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("naturalRegeneration", "false");

        Location location = new Location(world, x, y, z);


        uhcPlayer.getTeam().ifPresent(team -> {
            if (!locations.contains(location) && !teamloc.containsKey(team)) {
                locations.add(location);
                teamloc.put(team, location);
            }
        });
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.scatter(uhcPlayer, location, teamloc);
        });
        if (ScenarioManager.get().getActiveSpecialScenarios().isEmpty()) {
            if (UHCManager.get().getTeam_size() != 1) {
                UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            } else {
                player.teleport(location);
            }
        }


        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();

        Bukkit.broadcastMessage(CommonString.TP_MESSAGE.getMessage(player));
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.NOTE_STICKS, 1, 1));

    }

}