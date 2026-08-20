package net.novaproject.novauhc.display;

import net.novaproject.novauhc.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.HandlerList;

public final class PerViewerHologramManager implements Listener, DisplaySpi.IPerViewerHologramManager {

    private static final PerViewerHologramManager INSTANCE = new PerViewerHologramManager();
    public static PerViewerHologramManager get() { return INSTANCE; }

    private static final double BASE_Y_OFFSET = 2.55;
    private static final double STACK_OFFSET = 0.30;

    private final Map<UUID, Map<UUID, LinkedHashMap<String, Hologram>>> holograms = new HashMap<>();
    private boolean started = false;
    private BukkitTask task;

    public void start() {
        if (started) return;
        started = true;
        Bukkit.getPluginManager().registerEvents(this, Main.get());
        task = Bukkit.getScheduler().runTaskTimer(Main.get(), this::tickAll, 1L, 1L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        started = false;
        HandlerList.unregisterAll(this);
    }

    public void show(Player viewer, Player target, String key, Function<Player, String> textSupplier) {
        show(viewer, target, key, textSupplier, BASE_Y_OFFSET);
    }

    public void show(Player viewer, Player target, String key, Function<Player, String> textSupplier,
                     double baseOffset) {
        if (viewer == null || target == null || key == null || textSupplier == null) return;

        Map<UUID, LinkedHashMap<String, Hologram>> byTarget =
                holograms.computeIfAbsent(viewer.getUniqueId(), k -> new HashMap<>());
        LinkedHashMap<String, Hologram> byKey =
                byTarget.computeIfAbsent(target.getUniqueId(), k -> new LinkedHashMap<>());

        Hologram existing = byKey.get(key);
        if (existing != null) {
            existing.textSupplier = textSupplier;
            existing.baseOffset = baseOffset;
            return;
        }

        Hologram h = new Hologram(viewer.getUniqueId(), target.getUniqueId(), key, textSupplier, baseOffset);
        byKey.put(key, h);
        byTarget.put(target.getUniqueId(), sortedByKey(byKey));
        h.spawn(viewer, target);
    }

    private static LinkedHashMap<String, Hologram> sortedByKey(Map<String, Hologram> byKey) {
        LinkedHashMap<String, Hologram> sorted = new LinkedHashMap<>();
        byKey.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));
        return sorted;
    }

    public void hide(Player viewer, Player target, String key) {
        if (viewer == null || target == null || key == null) return;
        Map<UUID, LinkedHashMap<String, Hologram>> byTarget = holograms.get(viewer.getUniqueId());
        if (byTarget == null) return;
        LinkedHashMap<String, Hologram> byKey = byTarget.get(target.getUniqueId());
        if (byKey == null) return;
        Hologram h = byKey.remove(key);
        if (h != null) h.destroy(viewer);
        if (byKey.isEmpty()) byTarget.remove(target.getUniqueId());
        if (byTarget.isEmpty()) holograms.remove(viewer.getUniqueId());
    }

    public void hideAllForViewer(Player viewer) {
        if (viewer == null) return;
        Map<UUID, LinkedHashMap<String, Hologram>> byTarget = holograms.remove(viewer.getUniqueId());
        if (byTarget == null) return;
        for (LinkedHashMap<String, Hologram> byKey : byTarget.values()) {
            for (Hologram h : byKey.values()) h.destroy(viewer);
        }
    }

    public void hideAllOnTarget(Player target) {
        if (target == null) return;
        UUID targetId = target.getUniqueId();
        for (Map.Entry<UUID, Map<UUID, LinkedHashMap<String, Hologram>>> e : holograms.entrySet()) {
            Player viewer = Bukkit.getPlayer(e.getKey());
            LinkedHashMap<String, Hologram> byKey = e.getValue().remove(targetId);
            if (byKey != null && viewer != null) {
                for (Hologram h : byKey.values()) h.destroy(viewer);
            }
        }
    }

    private void tickAll() {
        Iterator<Map.Entry<UUID, Map<UUID, LinkedHashMap<String, Hologram>>>> viewerIt = holograms.entrySet().iterator();
        while (viewerIt.hasNext()) {
            Map.Entry<UUID, Map<UUID, LinkedHashMap<String, Hologram>>> viewerEntry = viewerIt.next();
            Player viewer = Bukkit.getPlayer(viewerEntry.getKey());
            if (viewer == null || !viewer.isOnline()) {
                for (LinkedHashMap<String, Hologram> byKey : viewerEntry.getValue().values()) {
                    for (Hologram h : byKey.values()) h.markDestroyed();
                }
                viewerIt.remove();
                continue;
            }

            Iterator<Map.Entry<UUID, LinkedHashMap<String, Hologram>>> targetIt = viewerEntry.getValue().entrySet().iterator();
            while (targetIt.hasNext()) {
                Map.Entry<UUID, LinkedHashMap<String, Hologram>> targetEntry = targetIt.next();
                Player target = Bukkit.getPlayer(targetEntry.getKey());
                if (target == null || !target.isOnline()) {
                    for (Hologram h : targetEntry.getValue().values()) h.destroy(viewer);
                    targetIt.remove();
                    continue;
                }
                int idx = 0;
                for (Hologram h : targetEntry.getValue().values()) {
                    h.update(viewer, target, idx++);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        hideAllForViewer(e.getPlayer());
        hideAllOnTarget(e.getPlayer());
    }

    private static final class Hologram {
        private final UUID viewerId;
        private final UUID targetId;
        private final String key;
        Function<Player, String> textSupplier;
        double baseOffset;
        private int entityId;
        private boolean spawnedToViewer = false;
        private String lastSentName = null;
        private boolean lastVisible = false;

        Hologram(UUID viewerId, UUID targetId, String key, Function<Player, String> textSupplier, double baseOffset) {
            this.viewerId = viewerId;
            this.targetId = targetId;
            this.key = key;
            this.textSupplier = textSupplier;
            this.baseOffset = baseOffset;
        }

        void spawn(Player viewer, Player target) {
            Location loc = target.getLocation().clone().add(0, baseOffset, 0);
            entityId = HologramPackets.spawn(viewer, loc, null);
            spawnedToViewer = true;
        }

        void update(Player viewer, Player target, int stackIndex) {
            if (!spawnedToViewer) return;
            String text = textSupplier.apply(target);
            boolean visible = text != null;

            if (visible) {
                if (!text.equals(lastSentName) || !lastVisible) {
                    HologramPackets.text(viewer, entityId, text, true);
                    lastSentName = text;
                }
            } else if (lastVisible) {
                HologramPackets.text(viewer, entityId, null, false);
            }
            lastVisible = visible;

            Location at = target.getLocation().clone();
            at.setY(at.getY() + baseOffset + (stackIndex * STACK_OFFSET));
            HologramPackets.teleport(viewer, entityId, at);
        }

        void destroy(Player viewer) {
            if (!spawnedToViewer) return;
            HologramPackets.destroy(viewer, entityId);
            spawnedToViewer = false;
        }

        void markDestroyed() {
            spawnedToViewer = false;
        }

    }
}
