package net.novaproject.novauhc.display;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SpectatorNametagService {

    private static final String KEY = "spec_role";

    private static final Lang LINE = DynamicLang.of("ui.spectator.nametag",
            "%role% §8│ §c%health% ❤");
    private static final Lang NO_ROLE = DynamicLang.of("ui.spectator.nametag_no_role", "§7Sans rôle");

    private static final Set<UUID> shown = new HashSet<>();

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
        shown.clear();
    }

    private static void tick() {
        if (!UHCManager.get().isGame()) return;

        List<UHCPlayer> alive = UHCPlayerManager.get().getPlayingOnlineUHCPlayers();

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            UHCPlayer viewerUp = UHCPlayerManager.get().getPlayer(viewer);
            boolean spectating = viewerUp != null && (viewerUp.isSpec() || !viewerUp.isPlaying());

            if (!spectating) {
                if (shown.remove(viewer.getUniqueId())) hideAll(viewer, alive);
                continue;
            }

            shown.add(viewer.getUniqueId());
            for (UHCPlayer targetUp : alive) {
                Player target = targetUp.getPlayer();
                if (target == null || !target.isOnline() || target.equals(viewer)) continue;
                PerViewerHologramManager.get().show(viewer, target, KEY,
                        watched -> line(viewer, watched));
            }
        }
    }

    private static void hideAll(Player viewer, List<UHCPlayer> alive) {
        for (UHCPlayer targetUp : alive) {
            Player target = targetUp.getPlayer();
            if (target != null) PerViewerHologramManager.get().hide(viewer, target, KEY);
        }
    }

    private static String line(Player viewer, Player target) {
        UHCPlayer targetUp = UHCPlayerManager.get().getPlayer(target);
        Role role = targetUp == null ? null : KPIBuilder.roleOf(targetUp);
        Camps camp = role == null ? null : role.getCamp();
        String roleName = role == null
                ? LangManager.get().get(NO_ROLE, viewer)
                : (camp == null ? "§f" : camp.getColor()) + role.getName();
        return LangManager.get().get(LINE, viewer, Map.of(
                "%role%", roleName,
                "%health%", String.format("%.1f", target.getHealth() / 2.0)));
    }
}
