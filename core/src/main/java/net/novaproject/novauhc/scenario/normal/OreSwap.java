package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.lang.ScenarioLang;
import net.novaproject.novauhc.scenario.lang.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.lang.OreSwapLang;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class OreSwap extends Scenario {

    private final Map<Material, Material> currentOreMapping = new HashMap<>();
    private final List<Material> oreTypes = Arrays.asList(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    );

    private final Map<Material, Material> oreToItem = new HashMap<>();
    private BukkitRunnable swapTask;

    public OreSwap() {
        oreToItem.put(Material.COAL_ORE, Material.COAL);
        oreToItem.put(Material.IRON_ORE, Material.IRON_INGOT);
        oreToItem.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        oreToItem.put(Material.DIAMOND_ORE, Material.DIAMOND);
        oreToItem.put(Material.EMERALD_ORE, Material.EMERALD);
        oreToItem.put(Material.LAPIS_ORE, Material.INK_SACK); // Lapis Lazuli
        oreToItem.put(Material.REDSTONE_ORE, Material.REDSTONE);
    }

    @Override
    public String getName() {
        return "OreSwap";
    }

    @Override
    public String getDescription() {
        return "Les minerais changent aléatoirement toutes les 15 minutes.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_ORE);
    }

    @Override
    public String getPath() {
        return "oreswap";
    }

    @Override
    public ScenarioLang[] getLang() {
        return OreSwapLang.values();
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

        if (oreTypes.contains(blockType)) {
            // Cancel the original drop
            event.setCancelled(true);

            // Break the block manually
            block.setType(Material.AIR);

            // Get the swapped ore type
            Material swappedOre = currentOreMapping.getOrDefault(blockType, blockType);
            Material itemToDrop = oreToItem.get(swappedOre);

            if (itemToDrop != null) {
                // Drop the swapped item
                ItemStack drop = new ItemStack(itemToDrop, 1);

                // Handle special cases
                if (itemToDrop == Material.INK_SACK) {
                    drop.setDurability((short) 4); // Lapis Lazuli data value
                    drop.setAmount(4 + new Random().nextInt(5)); // 4-8 lapis
                } else if (itemToDrop == Material.REDSTONE) {
                    drop.setAmount(4 + new Random().nextInt(2)); // 4-5 redstone
                } else if (itemToDrop == Material.COAL) {
                    drop.setAmount(1);
                }

                block.getWorld().dropItemNaturally(block.getLocation(), drop);

                // Send message to player
                String originalOreName = getOreName(blockType);
                String swappedOreName = getOreName(swappedOre);

                if (!blockType.equals(swappedOre)) {
                    player.sendMessage("§6[OreSwap] §f" + originalOreName + " → " + swappedOreName + " !");
                }
            }
        }
    }

    private void initializeOreMapping() {
        currentOreMapping.clear();

        // Create a shuffled list of ore types
        List<Material> shuffledOres = new ArrayList<>(oreTypes);
        Collections.shuffle(shuffledOres);

        // Map each ore to a random ore
        for (int i = 0; i < oreTypes.size(); i++) {
            currentOreMapping.put(oreTypes.get(i), shuffledOres.get(i));
        }

        // Broadcast the new mapping
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

                // Shuffle the ore mapping
                initializeOreMapping();

                ScenarioLangManager.sendAll(OreSwapLang.SWAP_ANNOUNCEMENT);
            }
        };

        // Run based on config interval
        int interval = getConfig().getInt("swap_interval", 900) * 20; // Convert seconds to ticks
        swapTask.runTaskTimer(Main.get(), interval, interval);
    }

    private void stopSwapTask() {
        if (swapTask != null) {
            swapTask.cancel();
            swapTask = null;
        }
    }

    private void broadcastOreMapping() {
        ScenarioLangManager.sendAll(OreSwapLang.NEW_MAPPING);

        for (Map.Entry<Material, Material> entry : currentOreMapping.entrySet()) {
            Material original = entry.getKey();
            Material swapped = entry.getValue();

            String originalName = getOreName(original);
            String swappedName = getOreName(swapped);
            Map<String, Object> placeholders = new HashMap<>();
            placeholders.put("%original%", originalName);
            placeholders.put("%swapped%", swappedName);
            if (!original.equals(swapped)) {
                ScenarioLangManager.sendAll(OreSwapLang.MAPPING_LINE, placeholders);
            } else {
                ScenarioLangManager.sendAll(OreSwapLang.MAPPING_UNCHANGED, placeholders);
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
