package net.novaproject.novauhc.scenario.special.gonefish;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class GoneFIsh extends Scenario {
    @Override
    public String getName() {
        return "GoneFish";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FISHING_ROD);
    }

    @Override
    public void onStart(Player player) {
        player.getPlayer().getInventory().addItem(new ItemCreator(Material.ANVIL).setAmount(20).getItemstack());
        player.getPlayer().getInventory().addItem(new ItemCreator(Material.FISHING_ROD).setUnbreakable(true).addEnchantment(Enchantment.LUCK, 250).addEnchantment(Enchantment.LURE, 5).getItemstack());

    }


    @Override
    public boolean isSpecial() {
        return true;
    }
}
