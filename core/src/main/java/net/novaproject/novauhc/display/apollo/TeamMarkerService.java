package net.novaproject.novauhc.display.apollo;

import net.novaproject.novauhc.display.DisplaySpi;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamArrow;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Color;

public final class TeamMarkerService {

    private static final Set<UUID> HAD_MARKERS = new HashSet<>();
    private static BukkitRunnable task;

    private static final DisplaySpi.ITeamMarkerService INSTANCE = new DisplaySpi.ITeamMarkerService() {
        @Override public void start(JavaPlugin plugin) { TeamMarkerService.start(plugin); }
        @Override public void stop() { TeamMarkerService.stop(); }
    };

    public static DisplaySpi.ITeamMarkerService instance() { return INSTANCE; }


    public static void start(JavaPlugin plugin) {
        if (task != null) return;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        task.runTaskTimer(plugin, 40L, 20L);
    }

    public static void stop() {
        clearAll();
        if (task != null) {
            try { task.cancel(); } catch (Exception ignored) {}
            task = null;
        }
    }

    private static void tick() {
        if (UHCPlayerManager.get() == null) return;
        if (!VisualFeedback.isActive(VisualFeedback.ApolloConfig.Capability.TEAM_MARKERS)
                || UHCManager.get() == null || UHCManager.get().getTeam_size() <= 1) {
            clearAll();
            return;
        }
        if (ScenarioManager.get() != null) {
            TeamArrow teamArrow = ScenarioManager.get().getScenario(TeamArrow.class);
            if (teamArrow != null && teamArrow.isActive()) return;
        }

        Set<UUID> updated = new HashSet<>();
        for (UHCPlayer viewer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Optional<UHCTeam> team = viewer.getTeam();
            if (team.isEmpty()) continue;
            Player vp = viewer.getPlayer();
            if (vp == null) continue;

            java.awt.Color color = toAwt(team.get());
            List<VisualFeedback.TeammateMarker> markers = new ArrayList<>();
            for (UHCPlayer mate : team.get().getPlayers()) {
                if (mate == viewer || !mate.isPlaying()) continue;
                Player tp = mate.getPlayer();
                if (tp == null) continue;
                markers.add(new VisualFeedback.TeammateMarker(
                        tp.getUniqueId(), tp.getWorld().getName(),
                        tp.getLocation().getX(), tp.getLocation().getY(), tp.getLocation().getZ(),
                        color));
            }

            if (markers.isEmpty()) {
                if (HAD_MARKERS.remove(vp.getUniqueId())) VisualFeedback.resetTeammates(vp);
            } else {
                VisualFeedback.teammates(vp, markers);
                updated.add(vp.getUniqueId());
            }
        }

        HAD_MARKERS.removeIf(uuid -> {
            if (updated.contains(uuid)) return false;
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer != null) VisualFeedback.resetTeammates(viewer);
            return true;
        });
        HAD_MARKERS.addAll(updated);
    }

    private static void clearAll() {
        if (HAD_MARKERS.isEmpty()) return;
        for (UUID uuid : HAD_MARKERS) {
            Player viewer = Bukkit.getPlayer(uuid);
            if (viewer != null) VisualFeedback.resetTeammates(viewer);
        }
        HAD_MARKERS.clear();
    }

    private static java.awt.Color toAwt(UHCTeam team) {
        try {
            Color c = team.dyeColor().getColor();
            return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue());
        } catch (Throwable t) {
            return java.awt.Color.GREEN;
        }
    }
}

