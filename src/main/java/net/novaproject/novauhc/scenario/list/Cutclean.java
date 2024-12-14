package net.novaproject.novauhc.scenario.list;

import net.novaproject.novauhc.scenario.Scenario;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Cutclean extends Scenario {

    @Override
    public String getName() {
        return "Cutclean";
    }

    @Override
    public String getDescription() {
        return "Ores and animal drops are automatically smelted, no furnaces needed.\n";
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {

        List<Material> types = Arrays.asList(Material.IRON_ORE, Material.GOLD_ORE);

        Material type = block.getType();

        if (!types.contains(type)) {
            return;
        }

        block.setType(Material.AIR);

        Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);

        if (type == Material.IRON_ORE){

            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_INGOT));

        }else if (type == Material.GOLD_ORE){

            loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_INGOT));

        }


    }

    @Override
    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent e) {


        if (e.getEntityType() == EntityType.PIG) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.GRILLED_PORK, 2));
        }

        if (e.getEntityType() == EntityType.COW) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.COOKED_BEEF, 2));
            e.getDrops().add(new ItemStack(Material.LEATHER));
        }

        if (e.getEntityType() == EntityType.CHICKEN) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.COOKED_CHICKEN, 2));
            e.getDrops().add(new ItemStack(Material.FEATHER, 2));
        }

        if (e.getEntityType() == EntityType.SHEEP) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.COOKED_MUTTON, 2));
        }

        if (e.getEntityType() == EntityType.RABBIT) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.COOKED_RABBIT, 1));
            int random = new Random().nextInt(100);
            if (random < 5) {
                e.getDrops().add(new ItemStack(Material.RABBIT_FOOT));
            }
        }

        if (e.getEntityType() == EntityType.SPIDER) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.SPIDER_EYE, 1));
            e.getDrops().add(new ItemStack(Material.STRING, 2));
        }

        if (e.getEntityType() == EntityType.SKELETON) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.BONE, 2));
            e.getDrops().add(new ItemStack(Material.ARROW, 3));
        }

    }
}
