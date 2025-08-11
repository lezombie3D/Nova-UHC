package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.listener.entity.EntityBowEvent;
import net.novaproject.novauhc.listener.entity.EntityDamageEntity;
import net.novaproject.novauhc.listener.entity.EntityDeathEvent;
import net.novaproject.novauhc.listener.player.*;
import net.novaproject.novauhc.utils.ui.CustomInventoryEvent;
import net.novaproject.novauhc.world.generation.ChunkUnloadListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    public static void setup(){

        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new PlayerConnectionEvent(), Main.get());
        pm.registerEvents(new PlayerBlockEvent(), Main.get());
        pm.registerEvents(new EntityDeathEvent(), Main.get());
        pm.registerEvents(new CustomInventoryEvent(), Main.get());
        pm.registerEvents(new PlayerDeathEvent(), Main.get());
        pm.registerEvents(new PlayerCraftEvent(),Main.get());
        pm.registerEvents(new PlayerInteractEvent(),Main.get());
        pm.registerEvents(new PlayerTakeDamage(),Main.get());
        pm.registerEvents(new PlayerListener(), Main.get());
        pm.registerEvents(new EntityBowEvent(), Main.get());
        pm.registerEvents(new EntityDamageEntity(), Main.get());
        pm.registerEvents(new ChunkUnloadListener(), Main.get());

    }

}
