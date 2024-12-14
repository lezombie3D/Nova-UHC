package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionEvent implements Listener {
    @EventHandler
    public void onPlayerConnectionEvent(PlayerJoinEvent event) {

        event.setJoinMessage(null);

        Player player = event.getPlayer();

        UHCPlayerManager.get().connect(player);
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {

        event.setQuitMessage(null);

        Player player = event.getPlayer();
        UHCPlayerManager.get().disconnect(player);


    }


}
