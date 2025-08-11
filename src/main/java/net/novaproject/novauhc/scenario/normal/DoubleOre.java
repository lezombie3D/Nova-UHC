package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DoubleOre extends Scenario {

    private static final String PLAYER_PLACED_TAG = "playerPlaced";

    @Override
    public String getName() {
        return "DoubleOre";
    }

    @Override
    public String getDescription() {
        return "Doubles les minerais";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLD_BLOCK);
    }
    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {

        if (!isActive()) return;

        Material type = block.getType();
        List<Material> types = Arrays.asList(Material.IRON_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE);

        if (!types.contains(type)) return;

        if (block.hasMetadata(PLAYER_PLACED_TAG)) {
            block.removeMetadata(PLAYER_PLACED_TAG, Main.get());
            return;
        }

        // Annule les drops par défaut
        event.setCancelled(true);
        block.setType(Material.AIR);

        Location loc = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
        Optional<Scenario> cutCleanScenario = ScenarioManager.get().getScenarioByName("cutclean");

        if (cutCleanScenario.isPresent() && ScenarioManager.get().getActiveScenarios().contains(cutCleanScenario.get())) {

            switch (type) {
                case IRON_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_INGOT, 3));
                    break;
                case GOLD_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_INGOT, 3));
                    break;
                case DIAMOND_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 2));
                    break;
                case EMERALD_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.EMERALD, 2));
                    break;
                default:
                    break;
            }
        } else {

            switch (type) {
                case IRON_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_ORE, 2));
                    break;
                case GOLD_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.GOLD_ORE, 2));
                    break;
                case DIAMOND_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.DIAMOND, 2));
                    break;
                case EMERALD_ORE:
                    loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.EMERALD, 2));
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {

        block.setMetadata(PLAYER_PLACED_TAG, new FixedMetadataValue(Main.get(), true));

    }
}
