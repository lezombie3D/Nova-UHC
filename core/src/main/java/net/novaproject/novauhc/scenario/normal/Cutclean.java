package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.LangManager;

public class Cutclean extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }

    @Override
    public String getName() {
        return "Cutclean";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.CUTCLEAN, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_INGOT);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;

        Material type = block.getType();
        List<Material> types = Arrays.asList(Material.IRON_ORE, Material.GOLD_ORE);

        if (!types.contains(type)) return;

        event.setCancelled(true);
        block.setType(Material.AIR);
        MiningScenarios.damageTool(player, player.getItemInHand());

        Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
        ExperienceOrb orb = block.getWorld().spawn(loc, ExperienceOrb.class);

        boolean isDoubleOreActive = ScenarioManager.get().isScenarioActive("DoubleOre");
        int amount = isDoubleOreActive ? 2 : 1;

        switch (type) {
            case IRON_ORE:
                Magnet.giveOrDrop(player, loc, new ItemStack(Material.IRON_INGOT, amount));
                orb.setExperience(3 + (int) (Math.random() * 5));
                break;
            case GOLD_ORE:
                Magnet.giveOrDrop(player, loc, new ItemStack(Material.GOLD_INGOT, amount));
                orb.setExperience(5 + (int) (Math.random() * 8));
                break;
            default:
                break;
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

