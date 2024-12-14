package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.Bukkit;
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
        event.setJoinMessage("§f[§2+§f] §f" + player.getName() +"§f ("+ Bukkit.getOnlinePlayers().size()+"/"+ Bukkit.getMaxPlayers() +")");
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {

        event.setQuitMessage(null);

        Player player = event.getPlayer();
        UHCPlayerManager.get().disconnect(player);
        event.setQuitMessage("§f[§c-§f] §f" + player.getName() +"§f ("+ (Bukkit.getOnlinePlayers().size()-1)+"/"+ Bukkit.getMaxPlayers() +")");


    }


}
