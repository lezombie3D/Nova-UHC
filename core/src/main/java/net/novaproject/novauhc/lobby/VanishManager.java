package net.novaproject.novauhc.lobby;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VanishManager implements Listener {

    private static final Set<UUID> VANISHED = ConcurrentHashMap.newKeySet();

    public static boolean isVanished(UUID uuid) {
        return VANISHED.contains(uuid);
    }

    public static boolean toggle(Player player) {
        if (VANISHED.remove(player.getUniqueId())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(player);
            }
            return false;
        }
        VANISHED.add(player.getUniqueId());
        applyVanish(player);
        return true;
    }

    private static void applyVanish(Player vanished) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(vanished)) continue;
            if (RankManager.isStaff(online)) continue;
            online.hidePlayer(vanished);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        for (UUID uuid : VANISHED) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished == null || RankManager.isStaff(joiner) || joiner.getUniqueId().equals(uuid)) continue;
            joiner.hidePlayer(vanished);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (VANISHED.remove(event.getPlayer().getUniqueId())) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.showPlayer(event.getPlayer());
            }
        }
    }
}

