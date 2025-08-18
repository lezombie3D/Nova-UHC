package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class NoWoodenTool extends Scenario {
    @Override
    public String getName() {
        return "NoWoodenTool";
    }

    @Override
    public String getDescription() {
        return "Interdit la fabrication et l'utilisation d'outils en bois.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOD_SPADE);
    }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {

        ItemStack item = event.getInventory().getResult();
        if (item.getType() == Material.WOOD_SPADE) {
            event.getInventory().setResult(new ItemStack(Material.IRON_SPADE));
        }
        if (item.getType() == Material.WOOD_PICKAXE) {
            event.getInventory().setResult(new ItemStack(Material.IRON_PICKAXE));
        }
        if (item.getType() == Material.WOOD_AXE) {
            event.getInventory().setResult(new ItemStack(Material.IRON_AXE));
        }
        if (item.getType() == Material.WOOD_SWORD) {
            event.getInventory().setResult(new ItemStack(Material.IRON_SWORD));

        }
    }

}
