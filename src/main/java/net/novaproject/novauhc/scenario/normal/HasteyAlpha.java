package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Set;

public class HasteyAlpha extends Scenario {
    @Override
    public String getName() {
        return "HasteyAlpha";
    }

    @Override
    public String getDescription() {
        return "hsdqhdskhqds";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_PICKAXE);
    }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {

        ItemStack item = result;
        Set<Material> validTools = EnumSet.of(
                Material.DIAMOND_PICKAXE,
                Material.DIAMOND_AXE,
                Material.DIAMOND_SPADE,
                Material.IRON_PICKAXE,
                Material.IRON_AXE,
                Material.IRON_SPADE,
                Material.WOOD_PICKAXE,
                Material.WOOD_AXE,
                Material.WOOD_SPADE,
                Material.STONE_PICKAXE,
                Material.STONE_AXE,
                Material.STONE_SPADE,
                Material.GOLD_AXE,
                Material.GOLD_PICKAXE,
                Material.GOLD_SPADE
        );

        if (validTools.contains(item.getType())) {
            item.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
            event.getInventory().setResult(item);
        }

    }

}
