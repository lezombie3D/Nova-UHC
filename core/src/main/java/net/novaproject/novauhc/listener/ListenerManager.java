package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.listener.effect.ArmorDurabilityNerfEvent;
import net.novaproject.novauhc.listener.player.LunarApolloListener;
import net.novaproject.novauhc.lunar.LunarApolloManager;
import net.novaproject.novauhc.listener.effect.AttackNerfEvent;
import net.novaproject.novauhc.listener.effect.ResistanceNerfEvent;
import net.novaproject.novauhc.listener.entity.EntityBowEvent;
import net.novaproject.novauhc.listener.entity.EntityDamageEntity;
import net.novaproject.novauhc.listener.entity.EntityDeathEvent;
import net.novaproject.novauhc.listener.player.*;
import net.novaproject.novauhc.utils.ui.CustomInventoryEvent;
import net.novaproject.novauhc.world.generation.ChunkUnloadListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class ListenerManager {

    public static void setup() {
        PluginManager pm = Bukkit.getPluginManager();
        Main plugin = Main.get();

        pm.registerEvents(new PlayerConnectionEvent(), plugin);
        pm.registerEvents(new PlayerBlockEvent(),      plugin);
        pm.registerEvents(new PlayerDeathEvent(),      plugin);
        pm.registerEvents(new PlayerCraftEvent(),      plugin);
        pm.registerEvents(new PlayerInteractEvent(),   plugin);
        pm.registerEvents(new PlayerTakeDamage(),      plugin);
        pm.registerEvents(new PlayerListener(),        plugin);

        pm.registerEvents(new EntityDeathEvent(),      plugin);
        pm.registerEvents(new EntityBowEvent(),        plugin);
        pm.registerEvents(new EntityDamageEntity(),    plugin);

        pm.registerEvents(new AttackNerfEvent(),           plugin);
        pm.registerEvents(new ResistanceNerfEvent(),       plugin);
        pm.registerEvents(new ArmorDurabilityNerfEvent(),  plugin);

        pm.registerEvents(new CustomInventoryEvent(),  plugin);
        pm.registerEvents(new ChunkUnloadListener(),   plugin);

        if (LunarApolloManager.get().isAvailable()) {
            pm.registerEvents(new LunarApolloListener(), plugin);
        }
    }
}