package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

public class Meetup extends Scenario {
    @Override
    public String getName() {
        return "Meetup";
    }

    @Override
    public String getDescription() {
        return "Donne un équipement UHC";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_CHESTPLATE);
    }

    @Override
    public void onStart(Player player) {

        player.giveExp(10000);
        player.setSaturation(100);
        ItemCreator helmet = new ItemCreator(Material.DIAMOND_HELMET);
        helmet.setUnbreakable(true);
        helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemCreator chestplate = new ItemCreator(Material.DIAMOND_CHESTPLATE);
        chestplate.setUnbreakable(true);
        chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemCreator leggings = new ItemCreator(Material.IRON_LEGGINGS);
        leggings.setUnbreakable(true);
        leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        ItemCreator boots = new ItemCreator(Material.DIAMOND_BOOTS);
        boots.setUnbreakable(true);
        boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
        ItemCreator bootsIron = new ItemCreator(Material.IRON_BOOTS);
        bootsIron.setUnbreakable(true);
        bootsIron.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
        ItemCreator gap = new ItemCreator(Material.GOLDEN_APPLE);
        gap.setAmount(24);
        ItemCreator lava = new ItemCreator(Material.LAVA_BUCKET);
        lava.setAmount(3);
        ItemCreator eau = new ItemCreator(Material.WATER_BUCKET);
        eau.setAmount(3);
        ItemCreator block = new ItemCreator(Material.WOOD);
        block.setAmount(256);
        ItemCreator bow = new ItemCreator(Material.BOW);
        bow.setUnbreakable(true);
        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 3);
        ItemCreator arrow = new ItemCreator(Material.ARROW);
        arrow.setAmount(64);
        ItemCreator rod = new ItemCreator(Material.FISHING_ROD);
        ItemCreator shovel = new ItemCreator(Material.DIAMOND_SPADE);
        shovel.setUnbreakable(true);
        shovel.addEnchantment(Enchantment.DIG_SPEED, 3);
        ItemCreator pick = new ItemCreator(Material.DIAMOND_PICKAXE);
        pick.setUnbreakable(true);
        pick.addEnchantment(Enchantment.DIG_SPEED, 3);
        ItemCreator axe = new ItemCreator(Material.DIAMOND_AXE);
        axe.setUnbreakable(true);
        axe.addEnchantment(Enchantment.DIG_SPEED, 3);
        ItemCreator food = new ItemCreator(Material.GOLDEN_CARROT);
        food.setAmount(64);
        ItemCreator sword = new ItemCreator(Material.DIAMOND_SWORD);
        sword.setUnbreakable(true);
        sword.addEnchantment(Enchantment.DAMAGE_ALL, 3);

        PlayerInventory inventory = player.getInventory();

        inventory.setHelmet(helmet.getItemstack());
        inventory.setChestplate(chestplate.getItemstack());
        inventory.setLeggings(leggings.getItemstack());
        inventory.addItem(boots.getItemstack());
        inventory.addItem(sword.getItemstack());
        inventory.addItem(food.getItemstack());
        inventory.addItem(gap.getItemstack());
        inventory.addItem(lava.getItemstack());
        inventory.addItem(eau.getItemstack());
        inventory.addItem(axe.getItemstack());
        inventory.addItem(pick.getItemstack());
        inventory.addItem(shovel.getItemstack());
        inventory.addItem(bow.getItemstack());
        inventory.addItem(arrow.getItemstack());
        inventory.addItem(rod.getItemstack());
        inventory.addItem(block.getItemstack());
        inventory.addItem(bootsIron.getItemstack());

    }

}
