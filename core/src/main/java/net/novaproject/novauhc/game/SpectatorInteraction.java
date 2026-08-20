package net.novaproject.novauhc.game;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.toolbox.TargetSelector;
import net.novaproject.novauhc.command.cmd.StaffCMD;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.ui.ModerationMenus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpectatorInteraction implements Listener {

    private static final long DEBOUNCE_MS = 400L;
    private static final long DOUBLE_SNEAK_MS = 500L;
    private static final double SNEAK_RANGE = 60.0;

    private static final SpectatorInteraction INSTANCE = new SpectatorInteraction();

    private static final Map<UUID, Long> LAST_OPEN = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_SNEAK = new ConcurrentHashMap<>();

    private SpectatorInteraction() {
    }

    public static void start() {
        Bukkit.getPluginManager().registerEvents(INSTANCE, Main.get());
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;
        Player viewer = event.getPlayer();
        if (!canInspect(viewer)) return;

        long now = System.currentTimeMillis();
        Long previous = LAST_SNEAK.put(viewer.getUniqueId(), now);
        if (!within(previous, now, DOUBLE_SNEAK_MS)) return;

        LAST_SNEAK.remove(viewer.getUniqueId());
        open(viewer, TargetSelector.lookedAt(viewer, SNEAK_RANGE));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        LAST_OPEN.remove(event.getPlayer().getUniqueId());
        LAST_SNEAK.remove(event.getPlayer().getUniqueId());
    }

    public static void open(Player viewer, Player target) {
        if (viewer == null || target == null || viewer.equals(target)) return;
        if (!viewer.isOnline() || !target.isOnline()) return;
        if (!canInspect(viewer)) return;

        long now = System.currentTimeMillis();
        if (within(LAST_OPEN.get(viewer.getUniqueId()), now, DEBOUNCE_MS)) return;
        LAST_OPEN.put(viewer.getUniqueId(), now);

        new ModerationMenus.SpectatorActions(viewer, target.getUniqueId()).open();
    }

    static boolean within(Long previous, long now, long windowMs) {
        return previous != null && now >= previous && now - previous < windowMs;
    }

    public static void openByEntityId(Player viewer, int entityId) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getEntityId() != entityId) continue;
            open(viewer, online);
            return;
        }
    }

    private static boolean canInspect(Player viewer) {
        return RankManager.isStaff(viewer) || StaffCMD.isSpectating(viewer);
    }
}
