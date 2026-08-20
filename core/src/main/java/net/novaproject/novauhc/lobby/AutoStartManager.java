package net.novaproject.novauhc.lobby;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class AutoStartManager implements Listener {

    private boolean triggered = false;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (triggered) return;
        if (!UHCManager.get().isAutoStartEnabled()) return;
        if (UHCManager.get().getGameState() != UHCManager.GameState.LOBBY) return;

        int minPlayers = UHCManager.get().getAutoStartMinPlayers();
        int online = Bukkit.getOnlinePlayers().size();
        if (online < minPlayers) return;

        triggered = true;
        LangManager.get().sendAll(CoreLang.COMMON_AUTO_START_TRIGGERED, Map.of("%players%", online));
        UHCManager.get().setCanceled(false);
        UHCManager.get().onStart();
        UHCManager.get().setStarted(true);
    }
}

