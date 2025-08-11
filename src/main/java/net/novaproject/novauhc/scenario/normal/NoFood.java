package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class NoFood extends Scenario {
    @Override
    public String getName() {
        return "NoFood";
    }

    @Override
    public String getDescription() {
        return "empêche les joueur de mourir de faim";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_CARROT);
    }

    @Override
    public void noFood(FoodLevelChangeEvent event) {
        event.setCancelled(true);
        event.setFoodLevel(20);

    }
}

