package net.novaproject.novauhc.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.ObjectiveMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.RenderType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore.Action;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PerViewerScore {

    public static final int SLOT_TAB = 0;
    public static final int SLOT_BELOW_NAME = 2;

    private static final int MAX_OBJECTIVE_NAME = 16;

    private final String objective;
    private final String title;
    private final int slot;
    private final Map<UUID, Map<String, Integer>> shown = new HashMap<>();

    public PerViewerScore(String objective, String title, int slot) {
        this.objective = objective.length() > MAX_OBJECTIVE_NAME
                ? objective.substring(0, MAX_OBJECTIVE_NAME)
                : objective;
        this.title = title;
        this.slot = slot;
    }

    public void update(Player viewer, Map<String, ? extends Number> values) {
        if (viewer == null || !viewer.isOnline()) return;
        UUID id = viewer.getUniqueId();
        boolean first = !shown.containsKey(id);
        Map<String, Integer> current = shown.computeIfAbsent(id, key -> new LinkedHashMap<>());

        if (first) {
            send(viewer, new WrapperPlayServerScoreboardObjective(objective, ObjectiveMode.CREATE,
                    AdventureSerializer.fromLegacyFormat(title), RenderType.INTEGER));
            send(viewer, new WrapperPlayServerDisplayScoreboard(slot, objective));
        }

        for (Map.Entry<String, ? extends Number> entry : values.entrySet()) {
            int score = entry.getValue().intValue();
            Integer previous = current.put(entry.getKey(), score);
            if (previous != null && previous == score) continue;
            send(viewer, new WrapperPlayServerUpdateScore(entry.getKey(), Action.CREATE_OR_UPDATE_ITEM,
                    objective, Optional.of(score)));
        }

        for (String gone : new ArrayList<>(current.keySet())) {
            if (values.containsKey(gone)) continue;
            current.remove(gone);
            send(viewer, new WrapperPlayServerUpdateScore(gone, Action.REMOVE_ITEM, objective, Optional.empty()));
        }
    }

    public void clear(UUID viewerId) {
        if (shown.remove(viewerId) == null) return;
        Player viewer = Bukkit.getPlayer(viewerId);
        if (viewer == null || !viewer.isOnline()) return;
        send(viewer, new WrapperPlayServerScoreboardObjective(objective, ObjectiveMode.REMOVE,
                AdventureSerializer.fromLegacyFormat(title), RenderType.INTEGER));
    }

    public void clearAll() {
        for (UUID viewerId : new HashSet<>(shown.keySet())) clear(viewerId);
    }

    private static void send(Player viewer, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
}
