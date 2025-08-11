package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class NoNameTag extends Scenario {

    @Override
    public String getName() {
        return "NoNameTag";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NAME_TAG);
    }

    @Override
    public void onStart(Player player) {
        player.setCustomNameVisible(false);
    }
}
