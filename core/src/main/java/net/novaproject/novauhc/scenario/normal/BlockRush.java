package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BlockRush extends Scenario {

    private final Map<Material, UUID> firstMiners = new HashMap<>();
    private final Map<UUID, Set<Material>> playerMinedBlocks = new HashMap<>();
    private final Map<UUID, Integer> playerRewards = new HashMap<>();

    @Override
    public String getName() {
        return "BlockRush";
    }

    @Override
    public String getDescription() {
        return "Miner un type de bloc pour la première fois donne 1 lingot d'or !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLD_INGOT);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;

        Material blockType = block.getType();
        UUID playerUuid = player.getUniqueId();

        if (!playerMinedBlocks.containsKey(playerUuid)) {
            playerMinedBlocks.put(playerUuid, new HashSet<>());
        }

        Set<Material> playerMined = playerMinedBlocks.get(playerUuid);

        if (!playerMined.contains(blockType) && isRewardableBlock(blockType)) {
            playerMined.add(blockType);

            ItemStack goldReward = new ItemStack(Material.GOLD_INGOT, 1);

            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(goldReward);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), goldReward);
                player.sendMessage("§6[BlockRush] §fInventaire plein ! Or jeté au sol.");
            }

            playerRewards.put(playerUuid, playerRewards.getOrDefault(playerUuid, 0) + 1);

            boolean isFirstEver = !firstMiners.containsKey(blockType);
            if (isFirstEver) {
                firstMiners.put(blockType, playerUuid);

                ItemStack bonusReward = getBonusReward(blockType);
                if (bonusReward != null) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(bonusReward);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), bonusReward);
                    }
                }

                String blockName = getBlockDisplayName(blockType);
                Bukkit.broadcastMessage("§6§l[BlockRush] §f" + player.getName() +
                        " §fest le premier à miner §6" + blockName + " §f!");

                player.sendMessage("§6[BlockRush] §fPremière découverte ! Bonus : §6" +
                        getBonusDescription(blockType));
            } else {
                String blockName = getBlockDisplayName(blockType);
                player.sendMessage("§6[BlockRush] §fNouveau bloc miné : §6" + blockName +
                        " §f! +1 Lingot d'Or");
            }

            int totalRewards = playerRewards.get(playerUuid);
            if (totalRewards == 10) {
                giveMilestoneReward(player, 10);
                Bukkit.broadcastMessage("§6[BlockRush] §f" + player.getName() +
                        " §fa miné 10 types de blocs différents !");
            } else if (totalRewards == 25) {
                giveMilestoneReward(player, 25);
                Bukkit.broadcastMessage("§6[BlockRush] §f" + player.getName() +
                        " §fa miné 25 types de blocs différents ! Explorateur expert !");
            } else if (totalRewards == 50) {
                giveMilestoneReward(player, 50);
                Bukkit.broadcastMessage("§6[BlockRush] §f" + player.getName() +
                        " §fa miné 50 types de blocs différents ! Maître mineur !");
            }
        }
    }

    private boolean isRewardableBlock(Material blockType) {
        switch (blockType) {
            case COAL_ORE:
            case IRON_ORE:
            case GOLD_ORE:
            case DIAMOND_ORE:
            case EMERALD_ORE:
            case LAPIS_ORE:
            case REDSTONE_ORE:
            case QUARTZ_ORE:

            case STONE:
            case DIRT:
            case GRASS:
            case SAND:
            case GRAVEL:
            case CLAY:
            case MYCEL:

            case LOG:
            case LOG_2:
            case LEAVES:
            case LEAVES_2:

            case NETHERRACK:
            case SOUL_SAND:
            case NETHER_BRICK:
            case NETHER_WARTS:
            case GLOWSTONE:

            case ENDER_STONE:
            case OBSIDIAN:

            case PRISMARINE:
            case SEA_LANTERN:
            case SPONGE:

            case MOSSY_COBBLESTONE:
            case MONSTER_EGG:
            case ICE:
            case PACKED_ICE:
            case SNOW_BLOCK:
            case CACTUS:
            case SUGAR_CANE_BLOCK:
            case PUMPKIN:
            case MELON_BLOCK:
            case VINE:
            case WATER_LILY:
                return true;

            default:
                return false;
        }
    }

    private ItemStack getBonusReward(Material blockType) {
        switch (blockType) {

            case DIAMOND_ORE:
                return new ItemStack(Material.DIAMOND, 2);
            case EMERALD_ORE:
                return new ItemStack(Material.EMERALD, 2);
            case GOLD_ORE:
                return new ItemStack(Material.GOLD_INGOT, 3);

            // Nether blocks
            case GLOWSTONE:
                return new ItemStack(Material.GLOWSTONE_DUST, 8);
            case NETHER_WARTS:
                return new ItemStack(Material.NETHER_WARTS, 4);
            case QUARTZ_ORE:
                return new ItemStack(Material.QUARTZ, 4);

            // End blocks
            case ENDER_STONE:
                return new ItemStack(Material.ENDER_PEARL, 2);
            case OBSIDIAN:
                return new ItemStack(Material.OBSIDIAN, 2);

            // Ocean blocks
            case PRISMARINE:
                return new ItemStack(Material.PRISMARINE_SHARD, 4);
            case SEA_LANTERN:
                return new ItemStack(Material.PRISMARINE_CRYSTALS, 3);
            case SPONGE:
                return new ItemStack(Material.SPONGE, 1);

            // Common blocks get smaller bonuses
            case IRON_ORE:
                return new ItemStack(Material.IRON_INGOT, 2);
            case COAL_ORE:
                return new ItemStack(Material.COAL, 3);
            case LAPIS_ORE:
                return new ItemStack(Material.INK_SACK, 6, (short) 4);
            case REDSTONE_ORE:
                return new ItemStack(Material.REDSTONE, 6);

            default:
                return new ItemStack(Material.GOLD_NUGGET, 5);
        }
    }

    private String getBonusDescription(Material blockType) {
        ItemStack bonus = getBonusReward(blockType);
        if (bonus != null) {
            return bonus.getAmount() + "x " + getItemDisplayName(bonus.getType());
        }
        return "Bonus mystère";
    }

    private void giveMilestoneReward(Player player, int milestone) {
        ItemStack reward = null;

        switch (milestone) {
            case 10:
                reward = new ItemStack(Material.GOLDEN_APPLE, 3);
                break;
            case 25:
                reward = new ItemStack(Material.ENCHANTED_BOOK, 2);
                break;
            case 50:
                reward = new ItemStack(Material.NETHER_STAR, 1);
                break;
        }

        if (reward != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(reward);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), reward);
            }

            player.sendMessage("§6[BlockRush] §fRécompense étape : §6" +
                    reward.getAmount() + "x " + getItemDisplayName(reward.getType()));
        }
    }

    private String getBlockDisplayName(Material material) {
        switch (material) {
            case COAL_ORE:
                return "Minerai de Charbon";
            case IRON_ORE:
                return "Minerai de Fer";
            case GOLD_ORE:
                return "Minerai d'Or";
            case DIAMOND_ORE:
                return "Minerai de Diamant";
            case EMERALD_ORE:
                return "Minerai d'Émeraude";
            case LAPIS_ORE:
                return "Minerai de Lapis";
            case REDSTONE_ORE:
                return "Minerai de Redstone";
            case QUARTZ_ORE:
                return "Minerai de Quartz";
            case OBSIDIAN:
                return "Obsidienne";
            case ENDER_STONE:
                return "Pierre de l'End";
            case NETHERRACK:
                return "Netherrack";
            case GLOWSTONE:
                return "Pierre Lumineuse";
            case PRISMARINE:
                return "Prismarine";
            case SEA_LANTERN:
                return "Lanterne de Mer";
            case SPONGE:
                return "Éponge";
            default:
                return material.name().replace("_", " ");
        }
    }

    private String getItemDisplayName(Material material) {
        switch (material) {
            case DIAMOND:
                return "Diamant";
            case EMERALD:
                return "Émeraude";
            case GOLD_INGOT:
                return "Lingot d'Or";
            case IRON_INGOT:
                return "Lingot de Fer";
            case COAL:
                return "Charbon";
            case GOLDEN_APPLE:
                return "Pomme d'Or";
            case ENCHANTED_BOOK:
                return "Livre Enchanté";
            case NETHER_STAR:
                return "Étoile du Nether";
            case ENDER_PEARL:
                return "Perle d'Ender";
            default:
                return material.name().replace("_", " ");
        }
    }

}
