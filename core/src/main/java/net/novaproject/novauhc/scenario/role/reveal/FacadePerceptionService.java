package net.novaproject.novauhc.scenario.role.reveal;

import java.util.logging.Level;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcRolesDistributedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FacadePerceptionService {

    private static final Map<UUID, Map<UUID, ChatColor>> lastApplied = new HashMap<>();

    private static BukkitTask task;

    public static void start(JavaPlugin plugin) {
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 40L, 20L);
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastApplied.clear();
    }

    public static void cleanup(UUID viewerUuid) {
        lastApplied.remove(viewerUuid);
    }

    private static void tick() {
        List<UHCPlayer> players = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();
        for (UHCPlayer targetUp : players) {
            Player target = targetUp.getPlayer();
            if (target == null || !target.isOnline()) continue;
            Role role = KPIBuilder.roleOf(targetUp);
            boolean faceted = role != null && role.hasFacades();

            for (UHCPlayer viewerUp : players) {
                Player viewer = viewerUp.getPlayer();
                if (viewer == null || !viewer.isOnline() || viewer.equals(target)) continue;

                boolean apply = faceted && role.hasFacadeFor(viewer);
                ChatColor prev = lastApplied.getOrDefault(viewer.getUniqueId(), Collections.emptyMap())
                        .get(target.getUniqueId());

                if (apply) {
                    ChatColor color = colorOf(role.getPerceivedCamp(viewer));
                    if (color != null && color != prev) {
                        DisplayService.perceptionColorFor(viewer, target, color);
                        lastApplied.computeIfAbsent(viewer.getUniqueId(), k -> new HashMap<>())
                                .put(target.getUniqueId(), color);
                    }
                } else if (prev != null) {
                    DisplayService.perceptionColorFor(viewer, target, null);
                    lastApplied.get(viewer.getUniqueId()).remove(target.getUniqueId());
                }
            }
        }
    }

    private static ChatColor colorOf(Camps camp) {
        if (camp == null) return null;
        String c = camp.getColor();
        if (c == null || c.length() < 2) return null;
        return ChatColor.getByChar(c.charAt(1));
    }

    public static class RoleRevealListener implements Listener {

        @EventHandler
        public void onRolesDistributed(UhcRolesDistributedEvent event) {
            event.getRoles().forEach((player, role) -> {
                try {
                    role.registerKnowPlayers();
                } catch (Throwable t) {
                    Bukkit.getLogger().log(Level.WARNING, "[Reveal] registerKnowPlayers en échec pour " + role.getName(), t);
                }
            });
            event.getRoles().forEach((player, role) -> {
                try {
                    role.sendKnowPlayers();
                } catch (Throwable t) {
                    Bukkit.getLogger().log(Level.WARNING, "[Reveal] sendKnowPlayers en échec pour " + role.getName(), t);
                }
            });
            RankManager.get().refreshTags();
        }
    }
}
