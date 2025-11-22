package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class OreRoulette extends Scenario {

    private final List<Material> oreTypes = Arrays.asList(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    );

    private final Map<Material, RouletteItem> oreRewards = new HashMap<>();
    private final Random random = new Random();

    public OreRoulette() {
        initializeRouletteRewards();
    }

    @Override
    public String getName() {
        return "OreRoulette";
    }

    @Override
    public String getDescription() {
        return "Chaque minerai miné donne un minerai aléatoire avec des probabilités différentes.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EMERALD_ORE);
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

            // Get random reward
            RouletteItem reward = getRandomReward();

            if (reward != null) {
                ItemStack drop = new ItemStack(reward.material, reward.amount);

                // Handle special cases for data values
                if (reward.material == Material.INK_SACK) {
                    drop.setDurability((short) 4); // Lapis Lazuli data value
                }

                block.getWorld().dropItemNaturally(block.getLocation(), drop);

                // Send message to player
                String originalOreName = getOreName(blockType);
                String rewardName = getItemName(reward.material);

                player.sendMessage("§d[OreRoulette] §f" + originalOreName + " → " +
                        reward.amount + "x " + rewardName + " !");

                // Special message for rare drops
                if (reward.material == Material.DIAMOND) {
                    player.sendMessage("§d[OreRoulette] §6§lCHANCE INCROYABLE ! Diamant obtenu !");
                } else if (reward.material == Material.EMERALD) {
                    player.sendMessage("§d[OreRoulette] §a§lTRÈS RARE ! Émeraude obtenue !");
                }
            }
        }
    }

    private void initializeRouletteRewards() {
        // Coal - 35% chance
        oreRewards.put(Material.COAL, new RouletteItem(Material.COAL, 1, 35));

        // Iron - 25% chance
        oreRewards.put(Material.IRON_INGOT, new RouletteItem(Material.IRON_INGOT, 1, 25));

        // Gold - 15% chance
        oreRewards.put(Material.GOLD_INGOT, new RouletteItem(Material.GOLD_INGOT, 1, 15));

        // Lapis - 10% chance (4-8 pieces)
        oreRewards.put(Material.INK_SACK, new RouletteItem(Material.INK_SACK, 4 + random.nextInt(5), 10));

        // Redstone - 8% chance (4-5 pieces)
        oreRewards.put(Material.REDSTONE, new RouletteItem(Material.REDSTONE, 4 + random.nextInt(2), 8));

        // Emerald - 4% chance
        oreRewards.put(Material.EMERALD, new RouletteItem(Material.EMERALD, 1, 4));

        // Diamond - 3% chance
        oreRewards.put(Material.DIAMOND, new RouletteItem(Material.DIAMOND, 1, 3));
    }

    private RouletteItem getRandomReward() {
        int totalWeight = oreRewards.values().stream().mapToInt(item -> item.weight).sum();
        int randomValue = random.nextInt(totalWeight);

        int currentWeight = 0;
        for (RouletteItem item : oreRewards.values()) {
            currentWeight += item.weight;
            if (randomValue < currentWeight) {
                // Create a new instance with potentially random amount
                return new RouletteItem(item.material, calculateAmount(item.material), item.weight);
            }
        }

        // Fallback to coal
        return new RouletteItem(Material.COAL, 1, 35);
    }

    private int calculateAmount(Material material) {
        switch (material) {
            case INK_SACK: // Lapis
                return 4 + random.nextInt(5); // 4-8
            case REDSTONE:
                return 4 + random.nextInt(2); // 4-5
            case COAL:
            case IRON_INGOT:
            case GOLD_INGOT:
            case DIAMOND:
            case EMERALD:
            default:
                return 1;
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

    private String getItemName(Material item) {
        switch (item) {
            case COAL:
                return "Charbon";
            case IRON_INGOT:
                return "Lingot de Fer";
            case GOLD_INGOT:
                return "Lingot d'Or";
            case DIAMOND:
                return "Diamant";
            case EMERALD:
                return "Émeraude";
            case INK_SACK:
                return "Lapis Lazuli";
            case REDSTONE:
                return "Redstone";
            default:
                return item.name();
        }
    }

    // Get roulette statistics (for admin commands)
    public Map<Material, RouletteItem> getRouletteRewards() {
        return new HashMap<>(oreRewards);
    }

    // Update roulette weights (admin command)
    public void updateWeight(Material material, int newWeight) {
        RouletteItem item = oreRewards.get(material);
        if (item != null) {
            oreRewards.put(material, new RouletteItem(item.material, item.amount, newWeight));
        }
    }

    // Inner class to represent roulette items
    private record RouletteItem(Material material, int amount, int weight) {
    }
}
