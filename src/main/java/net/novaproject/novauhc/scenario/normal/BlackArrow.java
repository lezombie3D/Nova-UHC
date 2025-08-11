package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlackArrow extends Scenario {
    @Override
    public String getName() {
        return "BlackArrow";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ARROW);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {


        Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);

        if (block.getType() == Material.COAL_ORE) {
            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.ARROW));
        }
    }
}
