package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
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
    public void onAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
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
                CommonString.DIM_NOT_ACCEIBLE.send(player);
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
                CommonString.DIM_NOT_ACCEIBLE.send(player);
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
            World endWorld = Bukkit.getWorld(Common.get().getArenaName() + "_the_end");
            if (endWorld == null) {
                CommonString.DIM_NOT_ACCEIBLE.send(player);
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
        Location loc = player.getLocation();

        if (UHCManager.get().isLobby()) {

            if (loc.getBlockY() < 0) {
                player.teleport(Common.get().getLobbySpawn());
            }
        }
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onMove(player, event);
        });

        if (player.getGameMode() == GameMode.SPECTATOR) {
            if (loc.getBlockY() < 0) {
                Location newLoc = loc.clone();
                newLoc.setY(0);
                player.teleport(newLoc);
            }

            WorldBorder border = player.getWorld().getWorldBorder();
            double size = border.getSize() / 2.0;
            Location center = border.getCenter();

            double minX = center.getX() - size;
            double maxX = center.getX() + size;
            double minZ = center.getZ() - size;
            double maxZ = center.getZ() + size;

            double x = loc.getX();
            double z = loc.getZ();

            if (x < minX || x > maxX || z < minZ || z > maxZ) {
                double newX = Math.max(minX + 0.5, Math.min(x, maxX - 0.5));
                double newZ = Math.max(minZ + 0.5, Math.min(z, maxZ - 0.5));
                Location newLoc = loc.clone();
                newLoc.setX(newX);
                newLoc.setZ(newZ);
                player.teleport(newLoc);
            }
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            event.setFormat(CommonString.LOBBY_CHAT_FORMAT.getMessage(player));
            return;
        }

        if (!uhcPlayer.isPlaying()) {
            CommonString.CANT_TALK_DEATH.send(player);
            event.setCancelled(true);
            return;
        }

        if (UHCManager.get().isChatdisbale()) {
            CommonString.CHAT_DISABLED.send(player);
            event.setCancelled(true);
            return;
        }

        List<Scenario> chatScenarios = ScenarioManager.get().getActiveScenarios().stream()
                .filter(Scenario::hasCustomTeamTchat)
                .collect(Collectors.toList());

        if (!chatScenarios.isEmpty()) {
            chatScenarios.forEach(s -> s.onChatSpeek(player, message, event));
            return;
        }

        if (!uhcPlayer.getTeam().isPresent()) {
            event.setFormat(CommonString.SOLO_CHAT_FORMAT.getMessage(player));
            return;
        }

        if (message.startsWith("!")) {
            event.setMessage(message.substring(1));
            event.setFormat(CommonString.CHAT_GLOBAL_FORMAT.getMessage(player));
            return;
        }

        UHCTeam team = uhcPlayer.getTeam().get();

        event.getRecipients().removeIf(p ->
                team.getPlayers().stream().noneMatch(u -> u.getPlayer().equals(p))
        );
        event.setFormat(CommonString.TEAM_CHAT_FORMAT.getMessage(player));
    }


}

