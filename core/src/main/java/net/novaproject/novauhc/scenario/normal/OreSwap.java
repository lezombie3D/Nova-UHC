package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.LangManager;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class OreSwap extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }

    @Var(lang = ScenarioVarLang.class, nameKey = "ORESWAP_VAR_SWAP_INTERVAL_NAME", descKey = "ORESWAP_VAR_SWAP_INTERVAL_DESC", type = VariableType.TIME)
    private int swapInterval = 900;

    private final Map<Material, Material> currentOreMapping = new HashMap<>();

    private final Map<Material, Material> oreToItem = new HashMap<>();
    private BukkitRunnable swapTask;

    public OreSwap() {
        oreToItem.put(Material.COAL_ORE, Material.COAL);
        oreToItem.put(Material.IRON_ORE, Material.IRON_INGOT);
        oreToItem.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        oreToItem.put(Material.DIAMOND_ORE, Material.DIAMOND);
        oreToItem.put(Material.EMERALD_ORE, Material.EMERALD);
        oreToItem.put(Material.LAPIS_ORE, Material.INK_SACK);
        oreToItem.put(Material.REDSTONE_ORE, Material.REDSTONE);
    }

    @Override
    public String getName() {
        return "OreSwap";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.ORE_SWAP, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_ORE);
    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            initializeOreMapping();
            startSwapTask();
        } else {
            stopSwapTask();
            currentOreMapping.clear();
        }
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;

        Material blockType = block.getType();

        if (MiningScenarios.ORE_TYPES.contains(blockType)) {

            event.setCancelled(true);

            block.setType(Material.AIR);
            MiningScenarios.damageTool(player, player.getItemInHand());

            Material swappedOre = currentOreMapping.getOrDefault(blockType, blockType);
            Material itemToDrop = oreToItem.get(swappedOre);

            if (itemToDrop != null) {

                ItemStack drop = new ItemStack(itemToDrop, 1);

                if (itemToDrop == Material.INK_SACK) {
                    drop.setDurability((short) 4);
                    drop.setAmount(4 + new Random().nextInt(5));
                } else if (itemToDrop == Material.REDSTONE) {
                    drop.setAmount(4 + new Random().nextInt(2));
                } else if (itemToDrop == Material.COAL) {
                    drop.setAmount(1);
                }

                block.getWorld().dropItemNaturally(block.getLocation().add(0.5D, 0.5D, 0.5D), drop);

                String originalOreName = getOreName(blockType);
                String swappedOreName = getOreName(swappedOre);

                if (!blockType.equals(swappedOre)) {
                    LangManager.get().send(ScenarioLang.ORESWAP_ORE_SWAPPED, player, Map.of("%original%", originalOreName, "%swapped%", swappedOreName));
                }
            }
        }
    }

    private void initializeOreMapping() {
        currentOreMapping.clear();

        List<Material> shuffledOres = new ArrayList<>(MiningScenarios.ORE_TYPES);
        Collections.shuffle(shuffledOres);

        for (int i = 0; i < MiningScenarios.ORE_TYPES.size(); i++) {
            currentOreMapping.put(MiningScenarios.ORE_TYPES.get(i), shuffledOres.get(i));
        }

        broadcastOreMapping();
    }

    private void startSwapTask() {
        if (swapTask != null) {
            swapTask.cancel();
        }

        swapTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive()) {
                    cancel();
                    return;
                }

                initializeOreMapping();

                LangManager.get().sendAll(ScenarioLang.ORESWAP_SWAP_ANNOUNCEMENT);
            }
        };

        int interval = swapInterval * 20;
        swapTask.runTaskTimer(Main.get(), interval, interval);
    }

    private void stopSwapTask() {
        if (swapTask != null) {
            swapTask.cancel();
            swapTask = null;
        }
    }

    private void broadcastOreMapping() {
        LangManager.get().sendAll(ScenarioLang.ORESWAP_NEW_MAPPING);

        for (Map.Entry<Material, Material> entry : currentOreMapping.entrySet()) {
            Material original = entry.getKey();
            Material swapped = entry.getValue();

            String originalName = getOreName(original);
            String swappedName = getOreName(swapped);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%original%", originalName);
            placeholders.put("%swapped%", swappedName);
            if (!original.equals(swapped)) {
                LangManager.get().sendAll(ScenarioLang.ORESWAP_MAPPING_LINE, placeholders);
            } else {
                LangManager.get().sendAll(ScenarioLang.ORESWAP_MAPPING_UNCHANGED, placeholders);
            }
        }
    }

    private String getOreName(Material ore) {
        switch (ore) {
            case COAL_ORE:
                return "Charbon";
            case IRON_ORE:
                return "Fer";
            case GOLD_ORE:
                return "Or";
            case DIAMOND_ORE:
                return "Diamant";
            case EMERALD_ORE:
                return "Émeraude";
            case LAPIS_ORE:
                return "Lapis";
            case REDSTONE_ORE:
                return "Redstone";
            default:
                return ore.name();
        }
    }
}

