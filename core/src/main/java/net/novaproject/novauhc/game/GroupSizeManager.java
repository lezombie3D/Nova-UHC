package net.novaproject.novauhc.game;

import net.novaproject.novauhc.utils.UHCUtils.ServerMonitor;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerDeathEvent;
import net.novaproject.novauhc.event.UhcPlayerEvents.UhcPlayerTimeoutEvent;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;

public class GroupSizeManager implements Listener, GameSpi.IGroupSizeManager {

    private static GroupSizeManager instance;

    private int currentSize = -1;
    private int forcedSize = -1;

    public GroupSizeManager() {
        instance = this;
    }

    public static GroupSizeManager get() {
        return instance;
    }

    public boolean isEnabled() {
        ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
        return scenarioRole != null && scenarioRole.isGroupSizeAnnounce();
    }

    public int getCurrentSize() {
        if (forcedSize > 0) return forcedSize;
        if (currentSize > 0) return currentSize;
        return compute();
    }

    public void setForcedSize(int size) {
        this.forcedSize = size;
        if (size > 0) {
            announce(size);
        }
    }

    private int compute() {
        ScenarioRole<?> scenarioRole = KPIBuilder.activeScenarioRole();
        int divisor = scenarioRole != null ? Math.max(scenarioRole.getGroupSizeDivisor(), 1) : 5;
        int min = scenarioRole != null ? Math.max(scenarioRole.getGroupSizeMin(), 1) : 2;
        int max = scenarioRole != null ? Math.max(scenarioRole.getGroupSizeMax(), min) : 5;
        int alive = ServerMonitor.displayedPlayerCount(UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size());
        int size = (int) Math.ceil(alive / (double) divisor);
        return Math.max(min, Math.min(max, size));
    }

    private void recalculate() {
        if (!isEnabled() || forcedSize > 0) return;
        int newSize = compute();
        if (currentSize > 0 && newSize >= currentSize) return;
        boolean first = currentSize <= 0;
        currentSize = newSize;
        if (!first) {
            announce(newSize);
        }
    }

    private void announce(int size) {
        String message = LangManager.get().get(CoreLang.COMMON_GROUP_SIZE_ANNOUNCE, null, Map.of("%size%", size));
        Bukkit.broadcastMessage(message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player == null || !player.isOnline()) continue;
            DisplayService.title(player, "", message, 40);
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 0.8f);
        }
    }

    @EventHandler
    public void onPlayerDeath(UhcPlayerDeathEvent event) {
        recalculate();
    }

    @EventHandler
    public void onPlayerTimeout(UhcPlayerTimeoutEvent event) {
        recalculate();
    }
}

