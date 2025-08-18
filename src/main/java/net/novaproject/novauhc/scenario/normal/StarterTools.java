package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class StarterTools extends Scenario {
    @Override
    public String getName() {
        return "Starter-Tools";
    }

    @Override
    public String getDescription() {
        return "Donne des outils de base à tous les joueurs au début de la partie.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOOK);
    }

    @Override
    public void onStart(Player player) {

        player.setSaturation(100);
        ItemCreator gap = new ItemCreator(Material.APPLE).setAmount(64);
        ItemCreator eau = new ItemCreator(Material.WATER_BUCKET).setAmount(1);
        ItemCreator block = new ItemCreator(Material.WOOD).setAmount(256);
        ItemCreator shovel = new ItemCreator(Material.IRON_SPADE).setUnbreakable(true).addEnchantment(Enchantment.DIG_SPEED, 3);
        ItemCreator pick = new ItemCreator(Material.IRON_PICKAXE).setUnbreakable(true).addEnchantment(Enchantment.DIG_SPEED, 3);
        ItemCreator axe = new ItemCreator(Material.IRON_AXE).setUnbreakable(true).addEnchantment(Enchantment.DIG_SPEED, 3);
        ItemCreator food = new ItemCreator(Material.GOLDEN_CARROT).setAmount(64);
        ItemCreator book = new ItemCreator(Material.BOOK).setAmount(7);

        PlayerInventory inventory = player.getInventory();

        inventory.addItem(food.getItemstack());
        inventory.addItem(gap.getItemstack());
        inventory.addItem(eau.getItemstack());
        inventory.addItem(axe.getItemstack());
        inventory.addItem(pick.getItemstack());
        inventory.addItem(shovel.getItemstack());
        inventory.addItem(book.getItemstack());
        inventory.addItem(block.getItemstack());
    }
}
