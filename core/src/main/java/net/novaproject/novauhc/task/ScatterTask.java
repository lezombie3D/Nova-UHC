package net.novaproject.novauhc.task;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.lang.TaskLang;
import net.novaproject.novauhc.scenario.Scenario;
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
        if (savedInventory == null || player == null) return;

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
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.FIREWORK_TWINKLE, 1, 1);
            }
        });

        UHCManager.get().setGameState(UHCManager.GameState.INGAME);

        Bukkit.broadcastMessage(LangManager.get().get(TaskLang.GAME_START, null, Map.of("%servertag%", Common.get().getServertag())));

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

        new GameTask().runTaskTimer(Main.get(), 0, 20L);
    }

    private void onScatter(UHCPlayer uhcPlayer) {
        Player player = uhcPlayer.getPlayer();
        if (player == null || !player.isOnline()) {
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

        LangManager.get().sendAll(CommonLang.TP_MESSAGE, Map.of("%player%", player.getName()));

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.NOTE_STICKS, 1, 1);
            }
        });
    }
}