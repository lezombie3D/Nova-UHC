package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerCraftEvent implements Listener {


    @EventHandler
    public void onCraft(CraftItemEvent event){

        ItemStack result = event.getRecipe().getResult();
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {

            scenario.onCraft(result, event);
        });
    }
}
