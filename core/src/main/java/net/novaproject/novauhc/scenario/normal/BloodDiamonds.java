package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class BloodDiamonds extends Scenario {
    @ScenarioVariable(name = "Quantité de dégâts", description = "Quantité de cœurs perdus en minant un diamant.",type = VariableType.DOUBLE)
    private int damageAmount = 1;

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

        player.damage(damageAmount);
    }
}
