package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerBlockEvent implements Listener {


    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {

        Player player = event.getPlayer();
        Block block = event.getBlock();

        //TODO VERIFIER QUE LA PARTIE EST LANCER
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
           scenario.onBreak(player, block, event);
        });

    }

}
