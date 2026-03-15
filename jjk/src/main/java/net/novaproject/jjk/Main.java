package net.novaproject.jjk;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public class Main extends JavaPlugin {

    public static Main instance;

    @Override
    public void onEnable(){
        instance = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                JJKUHC jjk = new JJKUHC();
                ScenarioManager.get().addScenario(jjk);
                jjk.setup();
            }
        }.runTaskLater(this,20L);
    }

    @Override
    public void onDisable() {

    }


    public void sendMessage(Player player,String message){
        player.sendMessage(message);
    }

}
