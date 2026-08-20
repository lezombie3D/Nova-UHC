package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.player.utils.PlayerUtils;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BlockRush extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }

    private final Map<UUID, Set<Material>> playerMinedBlocks = new HashMap<>();

    @Var(lang = ScenarioVarLang.class, nameKey = "BLOCKRUSH_VAR_BASE_REWARD_AMOUNT_NAME", descKey = "BLOCKRUSH_VAR_BASE_REWARD_AMOUNT_DESC", type = VariableType.INTEGER)
    private int baseRewardAmount = 1;

    @Override public String getName() { return "BlockRush"; }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.BLOCK_RUSH, player)
                .replace("%reward%", String.valueOf(baseRewardAmount));
    }

    @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLD_INGOT); }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;
        Material blockType = block.getType();
        if (!isRewardableBlock(blockType)) return;

        Set<Material> mined = playerMinedBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (!mined.add(blockType)) return;

        PlayerUtils.giveOrDrop(player, new ItemStack(Material.GOLD_INGOT, baseRewardAmount));
        player.sendMessage(t(ScenarioLang.BLOCKRUSH_NEW_BLOCK, Map.of(
                "%block%", blockType.name().replace("_", " ").toLowerCase(),
                "%amount%", baseRewardAmount)));
    }

    private boolean isRewardableBlock(Material m) {
        return switch (m) {
            case COAL_ORE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, EMERALD_ORE, LAPIS_ORE, REDSTONE_ORE, QUARTZ_ORE,
                 STONE, DIRT, GRASS, SAND, GRAVEL, CLAY, MYCEL, LOG, LOG_2, LEAVES, LEAVES_2,
                 NETHERRACK, SOUL_SAND, NETHER_BRICK, NETHER_WARTS, GLOWSTONE, ENDER_STONE, OBSIDIAN,
                 PRISMARINE, SEA_LANTERN, SPONGE, MOSSY_COBBLESTONE, MONSTER_EGG, ICE, PACKED_ICE,
                 SNOW_BLOCK, CACTUS, SUGAR_CANE_BLOCK, PUMPKIN, MELON_BLOCK, VINE, WATER_LILY -> true;
            default -> false;
        };
    }

    @Override
    public void onStop() {
        playerMinedBlocks.clear();
    }
}
