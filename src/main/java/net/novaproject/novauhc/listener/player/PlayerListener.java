package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerListener implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {

        if (UHCManager.get().isLobby()) {
            event.setCancelled(true);
            event.setFoodLevel(20);
            return;
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

            scenario.noFood(event);
        });
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItem();
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

            scenario.onPickUp(player, item, event);
        });
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> scenario.onPortal(event));

        Player player = event.getPlayer();
        World fromWorld = player.getWorld();

        if (fromWorld.getName().equalsIgnoreCase("arena")) {
            World netherWorld = Bukkit.getWorld(Common.get().getArenaName() + "_nether");
            if (netherWorld == null) {
                player.sendMessage(ChatColor.RED + "Le Nether n'est pas disponible !");
                event.setCancelled(true);
                return;
            }

            Location netherSpawn = new Location(
                    netherWorld,
                    event.getFrom().getX() / 8,
                    event.getFrom().getY(),
                    event.getFrom().getZ() / 8
            );

            TravelAgent portal = event.getPortalTravelAgent();
            Location loc = portal.setSearchRadius(20).findOrCreate(netherSpawn);
            player.teleport(loc);
            return;
        }

        if (fromWorld.getName().equalsIgnoreCase(Common.get().getArenaName() + "_nether")) {
            World overworld = Common.get().getArena();
            if (overworld == null) {
                player.sendMessage(ChatColor.RED + "Le monde principal n'est pas disponible !");
                event.setCancelled(true);
                return;
            }

            Location spawn = new Location(
                    overworld,
                    event.getFrom().getX() * 8,
                    event.getFrom().getY(),
                    event.getFrom().getZ() * 8
            );

            TravelAgent portal = event.getPortalTravelAgent();
            Location loc = portal.setSearchRadius(20).findOrCreate(spawn);
            player.teleport(loc);
            return;
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            World endWorld = Bukkit.getWorld("arena_the_end");
            if (endWorld == null) {
                player.sendMessage(ChatColor.RED + "L'End n'est pas disponible !");
                event.setCancelled(true);
                return;
            }

            Location endSpawn = endWorld.getSpawnLocation();
            event.setTo(endSpawn);
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        Player player = event.getPlayer();
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onMove(player, event);
        });

    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        event.setCancelled(true);
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        System.out.println(player.getDisplayName() + " " + message);

        // Si la partie n'est pas en cours
        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.DARK_GRAY + "❯ "
                        + player.getName() + " » "
                        + ChatColor.WHITE + message);
            }
            return;
        }

        // Si le joueur ne joue pas
        if (!uhcPlayer.isPlaying()) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de parler !");
            return;
        }

        // Si le chat est désactivé
        if (UHCManager.get().isChatdisbale()) return;

        // Scénarios avec chat custom
        List<Scenario> chatScenarios = ScenarioManager.get().getActiveScenarios().stream()
                .filter(Scenario::hasCustomTeamTchat)
                .collect(Collectors.toList());

        if (!chatScenarios.isEmpty()) {
            chatScenarios.forEach(s -> s.onChatSpeek(player, message, event));
            return;
        }

        if (UHCManager.get().getTeam_size() == 1) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.DARK_GRAY + "❯ "
                        + player.getName() + " » "
                        + ChatColor.WHITE + message);
            }
            return;
        }

        // Chat global avec "!"
        if (message.startsWith("!")) {
            for (UHCPlayer lobby : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                lobby.getPlayer().sendMessage(ChatColor.GREEN + "✦ Global ✦ "
                        + ChatColor.DARK_GRAY + player.getName() + " » "
                        + ChatColor.WHITE + message.substring(1));
            }
            return;
        }

        // Chat d'équipe normal
        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();
            for (UHCPlayer teamPlayer : team.getPlayers()) {
                teamPlayer.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "❖ Team ❖ "
                        + ChatColor.DARK_GRAY + player.getName() + " » "
                        + ChatColor.WHITE + message);
            }
        }
    }

}

