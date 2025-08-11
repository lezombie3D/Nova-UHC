package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class InfiniteEnchanter extends Scenario {
    @Override
    public String getName() {
        return "InfiniteEnchanter";
    }

    @Override
    public String getDescription() {
        return "Donne beaucoup d'xp";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ENCHANTMENT_TABLE);
    }

    @Override
    public void onStart(Player player) {
        player.setLevel(10000000);
    }
}
