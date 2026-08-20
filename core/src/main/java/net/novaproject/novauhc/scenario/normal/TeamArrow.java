package net.novaproject.novauhc.scenario.normal;

import java.util.ArrayList;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.display.PerViewerHologramManager;
import net.novaproject.novauhc.display.apollo.VisualFeedback;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.LangManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Color;

public class TeamArrow extends Scenario {

    @Override
    public Family getFamily() { return Family.COMBAT; }

    private static final String HOLO_KEY = "teamarrow";

    private final Map<UUID, BukkitRunnable> playerTasks = new HashMap<>();
    private final Map<UUID, Set<UUID>> shown = new HashMap<>();

    @Override
    public String getName() {
        return "TeamArrow";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.TEAM_ARROW, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ARROW);
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (!uhcPlayer.getTeam().isPresent()) return;

        UUID uuid = player.getUniqueId();
        BukkitRunnable existing = playerTasks.get(uuid);
        if (existing != null) existing.cancel();

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Player viewer = uhcPlayer.getPlayer();
                if (!isActive() || viewer == null || !uhcPlayer.getTeam().isPresent()) {
                    clearViewer(uuid);
                    playerTasks.remove(uuid);
                    cancel();
                    return;
                }
                UHCTeam team = uhcPlayer.getTeam().get();
                Location viewerLoc = viewer.getLocation();
                float viewerYaw = viewerLoc.getYaw();

                boolean lunar = DisplayService.isLunar(viewer);
                java.awt.Color teamColor = toAwt(team);

                StringBuilder actionBar = new StringBuilder();
                boolean first = true;
                Set<UUID> want = new HashSet<>();
                List<VisualFeedback.TeammateMarker> markers = lunar ? new ArrayList<>() : null;

                for (UHCPlayer p : team.getPlayers()) {
                    if (p == uhcPlayer || !p.isPlaying()) continue;
                    Player teammate = p.getPlayer();
                    if (teammate == null) continue;

                    if (lunar) {
                        Location tl = teammate.getLocation();
                        markers.add(new VisualFeedback.TeammateMarker(
                                teammate.getUniqueId(), tl.getWorld().getName(),
                                tl.getX(), tl.getY(), tl.getZ(), teamColor));
                    } else {
                        want.add(teammate.getUniqueId());
                        PerViewerHologramManager.get().show(viewer, teammate, HOLO_KEY, t -> {
                            Player v = Bukkit.getPlayer(uuid);
                            if (v == null || !v.isOnline() || !v.getWorld().equals(t.getWorld())) return null;
                            int dist = (int) v.getLocation().distance(t.getLocation());
                            String dir = getArrowDirection(v.getLocation(), t.getLocation(), v.getLocation().getYaw());
                            return "§6" + dir + " §a" + t.getName() + " §7" + dist + "m";
                        });
                    }

                    if (!first) actionBar.append(" §8|§r ");
                    actionBar.append("§f").append(teammate.getName()).append(" §6")
                            .append(getArrowDirection(viewerLoc, teammate.getLocation(), viewerYaw));
                    first = false;
                }

                Set<UUID> prev = shown.computeIfAbsent(uuid, k -> new HashSet<>());
                for (UUID old : new HashSet<>(prev)) {
                    if (!want.contains(old)) {
                        Player tp = Bukkit.getPlayer(old);
                        if (tp != null) PerViewerHologramManager.get().hide(viewer, tp, HOLO_KEY);
                    }
                }
                shown.put(uuid, want);

                if (lunar) {
                    if (markers.isEmpty()) DisplayService.resetTeammates(viewer);
                    else DisplayService.teammates(viewer, markers);
                }

                if (actionBar.length() > 0) {
                    DisplayService.actionBar(viewer, actionBar.toString());
                }
            }
        };
        playerTasks.put(uuid, task);
        task.runTaskTimer(Main.get(), 0, 20);
    }

    private void clearViewer(UUID uuid) {
        Player viewer = Bukkit.getPlayer(uuid);
        Set<UUID> prev = shown.remove(uuid);
        if (viewer != null) {
            if (prev != null) {
                for (UUID t : prev) {
                    Player tp = Bukkit.getPlayer(t);
                    if (tp != null) PerViewerHologramManager.get().hide(viewer, tp, HOLO_KEY);
                }
            }
            DisplayService.resetTeammates(viewer);
        }
    }

    private static java.awt.Color toAwt(UHCTeam team) {
        try {
            Color c = team.dyeColor().getColor();
            return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue());
        } catch (Throwable t) {
            return java.awt.Color.GREEN;
        }
    }

    @Override
    public void onStop() {
        for (BukkitRunnable task : playerTasks.values()) {
            try { task.cancel(); } catch (Exception ignored) {}
        }
        playerTasks.clear();
        for (UUID uuid : new HashSet<>(shown.keySet())) {
            clearViewer(uuid);
        }
        shown.clear();
    }

    public String getArrowDirection(Location from, Location to, float playerYaw) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        double angle = Math.toDegrees(Math.atan2(-dx, dz));
        angle = (angle + 360) % 360;

        float normalizedYaw = (playerYaw + 360) % 360;

        double relativeAngle = (angle - normalizedYaw + 360) % 360;

        if (relativeAngle >= 337.5 || relativeAngle < 22.5) return "↑";
        if (relativeAngle >= 22.5 && relativeAngle < 67.5) return "↗";
        if (relativeAngle >= 67.5 && relativeAngle < 112.5) return "→";
        if (relativeAngle >= 112.5 && relativeAngle < 157.5) return "↘";
        if (relativeAngle >= 157.5 && relativeAngle < 202.5) return "↓";
        if (relativeAngle >= 202.5 && relativeAngle < 247.5) return "↙";
        if (relativeAngle >= 247.5 && relativeAngle < 292.5) return "←";
        if (relativeAngle >= 292.5 && relativeAngle < 337.5) return "↖";

        return "?";
    }
}
