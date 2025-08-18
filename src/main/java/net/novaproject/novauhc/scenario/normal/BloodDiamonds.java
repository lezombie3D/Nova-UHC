package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BloodDiamonds extends Scenario {

    @Override
    public String getName() {
        return "BloodDiamonds";
    }

    @Override
    public String getDescription() {
        return "Les joueurs perdent 1 cœur en minant un diamant.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_ORE);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive() || block.getType() != Material.DIAMOND_ORE) return;

        player.damage(2.0);
    }
}
