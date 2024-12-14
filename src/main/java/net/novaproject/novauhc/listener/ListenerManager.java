package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.listener.entity.EntityDeathEvent;
import net.novaproject.novauhc.listener.player.PlayerBlockEvent;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.listener.player.PlayerDeathEvent;
import net.novaproject.novauhc.utils.ui.CustomInventoryEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    public static void setup(){

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new PlayerConnectionEvent(), Main.get());
        pm.registerEvents(new PlayerBlockEvent(), Main.get());
        pm.registerEvents(new EntityDeathEvent(), Main.get());
        pm.registerEvents(new CustomInventoryEvent(), Main.get());
        pm.registerEvents(new PlayerDeathEvent(), Main.get());
    }

}
