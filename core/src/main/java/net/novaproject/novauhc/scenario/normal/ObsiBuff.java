package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.EnumSet;
import java.util.Set;

public class ObsiBuff extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }

    private static final String PLAYER_PLACED_TAG = "obsiBuff_playerPlaced";

    private static final Set<Material> PICKAXES = EnumSet.of(
            Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLD_PICKAXE, Material.DIAMOND_PICKAXE
    );

    @Var(lang = ScenarioVarLang.class,
            nameKey = "OBSIBUFF_VAR_HASTE_LEVEL_NAME", descKey = "OBSIBUFF_VAR_HASTE_LEVEL_DESC",
            type = VariableType.INTEGER)
    private int hasteLevel = 2;

    @Var(lang = ScenarioVarLang.class,
            nameKey = "OBSIBUFF_VAR_HASTE_DURATION_NAME", descKey = "OBSIBUFF_VAR_HASTE_DURATION_DESC",
            type = VariableType.TIME)
    private int hasteDurationSec = 30;

    @Var(lang = ScenarioVarLang.class,
            nameKey = "OBSIBUFF_VAR_DROP_AMOUNT_NAME", descKey = "OBSIBUFF_VAR_DROP_AMOUNT_DESC",
            type = VariableType.INTEGER)
    private int dropAmount = 2;

    @Override public String getName() { return "ObsiBuff"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.OBSI_BUFF, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.OBSIDIAN);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;
        if (block.getType() != Material.OBSIDIAN) return;

        if (block.hasMetadata(PLAYER_PLACED_TAG)) {
            block.removeMetadata(PLAYER_PLACED_TAG, Main.get());
            return;
        }

        ItemStack tool = player.getItemInHand();
        if (tool == null || !PICKAXES.contains(tool.getType())) return;

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.FAST_DIGGING,
                hasteDurationSec * 20,
                hasteLevel,
                false, false), true);

        if (dropAmount > 1) {
            Location loc = block.getLocation().clone().add(0.5, 0.5, 0.5);
            block.getWorld().dropItemNaturally(loc, new ItemStack(Material.OBSIDIAN, dropAmount - 1));
        }
    }

    @Override
    public void onPlace(Player player, Block block, BlockPlaceEvent event) {
        if (block.getType() == Material.OBSIDIAN) {
            block.setMetadata(PLAYER_PLACED_TAG, new FixedMetadataValue(Main.get(), true));
        }
    }
}

