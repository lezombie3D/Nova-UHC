package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class Vanilla_plus extends Scenario {
    @Override
    public String getName() {
        return "Vanilla+";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.APPLE);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
        if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2) {
            int random = new Random().nextInt(100);
            if (random < 70) {
                block.getWorld().dropItemNaturally(loc, new ItemStack(Material.APPLE));
            }
        }
        if (block.getType() == Material.GRAVEL) {
            int random = new Random().nextInt(100);
            if (random < 70) {
                block.getWorld().dropItemNaturally(loc, new ItemStack(Material.FLINT));
            }
        }


    }
}
