package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
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

    @ScenarioVariable(
            name = "base_reward_amount",
            description = "Quantité de lingot d'or donnée pour chaque nouveau bloc miné",
            type = VariableType.INTEGER
    )
    private int baseRewardAmount = 1;

    @ScenarioVariable(
            name = "diamond_bonus",
            description = "Quantité de diamants pour le premier miner un diamant",
            type = VariableType.INTEGER
    )
    private int diamondBonus = 2;

    @ScenarioVariable(
            name = "emerald_bonus",
            description = "Quantité d'émeraudes pour le premier miner une émeraude",
            type = VariableType.INTEGER
    )
    private int emeraldBonus = 2;

    @ScenarioVariable(
            name = "gold_bonus",
            description = "Quantité de lingots d'or pour le premier miner un bloc d'or",
            type = VariableType.INTEGER
    )
    private int goldBonus = 3;

    @ScenarioVariable(
            name = "milestone_10_reward",
            description = "Quantité de pommes d'or pour le palier 10 blocs différents",
            type = VariableType.INTEGER
    )
    private int milestone10Reward = 3;

    @ScenarioVariable(
            name = "milestone_25_reward",
            description = "Quantité de livres enchantés pour le palier 25 blocs différents",
            type = VariableType.INTEGER
    )
    private int milestone25Reward = 2;

    @ScenarioVariable(
            name = "milestone_50_reward",
            description = "Quantité d'étoiles du Nether pour le palier 50 blocs différents",
            type = VariableType.INTEGER
    )
    private int milestone50Reward = 1;

    @Override
    public String getName() {
        return "BlockRush";
    }

    @Override
    public String getDescription() {
        return "Miner un type de bloc pour la première fois donne " + baseRewardAmount + " lingot(s) d'or et des bonus pour certains blocs.";
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

        playerMinedBlocks.putIfAbsent(playerUuid, new HashSet<>());
        Set<Material> playerMined = playerMinedBlocks.get(playerUuid);

        if (!playerMined.contains(blockType) && isRewardableBlock(blockType)) {
            playerMined.add(blockType);

            ItemStack goldReward = new ItemStack(Material.GOLD_INGOT, baseRewardAmount);
            giveOrDrop(player, goldReward, "Inventaire plein ! Or jeté au sol.");

            playerRewards.put(playerUuid, playerRewards.getOrDefault(playerUuid, 0) + 1);

            boolean isFirstEver = !firstMiners.containsKey(blockType);
            if (isFirstEver) {
                firstMiners.put(blockType, playerUuid);
                ItemStack bonusReward = getBonusReward(blockType);
                giveOrDrop(player, bonusReward, null);

                String blockName = getBlockDisplayName(blockType);
                Bukkit.broadcastMessage("§6§l[BlockRush] §f" + player.getName() +
                        " §fest le premier à miner §6" + blockName + " §f!");
                player.sendMessage("§6[BlockRush] §fPremière découverte ! Bonus : §6" +
                        getBonusDescription(blockType));
            } else {
                String blockName = getBlockDisplayName(blockType);
                player.sendMessage("§6[BlockRush] §fNouveau bloc miné : §6" + blockName +
                        " §f! +" + baseRewardAmount + " Lingot(s) d'Or");
            }

            int totalRewards = playerRewards.get(playerUuid);
            if (totalRewards == 10) giveMilestone(player, 10, milestone10Reward, Material.GOLDEN_APPLE);
            else if (totalRewards == 25) giveMilestone(player, 25, milestone25Reward, Material.ENCHANTED_BOOK);
            else if (totalRewards == 50) giveMilestone(player, 50, milestone50Reward, Material.NETHER_STAR);
        }
    }

    private void giveOrDrop(Player player, ItemStack item, String fullInventoryMessage) {
        if (item == null) return;
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        } else if (fullInventoryMessage != null) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
            player.sendMessage("§6[BlockRush] §f" + fullInventoryMessage);
        }
    }

    private void giveMilestone(Player player, int milestone, int amount, Material material) {
        ItemStack reward = new ItemStack(material, amount);
        giveOrDrop(player, reward, null);
        player.sendMessage("§6[BlockRush] §fRécompense étape : §6" + amount + "x " + getItemDisplayName(material));
        Bukkit.broadcastMessage("§6[BlockRush] §f" + player.getName() + " §fa miné " + milestone + " types de blocs différents !");
    }

    private boolean isRewardableBlock(Material blockType) {
        return switch (blockType) {
            case COAL_ORE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, EMERALD_ORE, LAPIS_ORE, REDSTONE_ORE, QUARTZ_ORE,
                 STONE, DIRT, GRASS, SAND, GRAVEL, CLAY, MYCEL,
                 LOG, LOG_2, LEAVES, LEAVES_2,
                 NETHERRACK, SOUL_SAND, NETHER_BRICK, NETHER_WARTS, GLOWSTONE,
                 ENDER_STONE, OBSIDIAN,
                 PRISMARINE, SEA_LANTERN, SPONGE,
                 MOSSY_COBBLESTONE, MONSTER_EGG,
                 ICE, PACKED_ICE, SNOW_BLOCK, CACTUS, SUGAR_CANE_BLOCK, PUMPKIN, MELON_BLOCK,
                 VINE, WATER_LILY -> true;
            default -> false;
        };
    }

    private ItemStack getBonusReward(Material blockType) {
        return switch (blockType) {
            case DIAMOND_ORE -> new ItemStack(Material.DIAMOND, diamondBonus);
            case EMERALD_ORE -> new ItemStack(Material.EMERALD, emeraldBonus);
            case GOLD_ORE -> new ItemStack(Material.GOLD_INGOT, goldBonus);
            case GLOWSTONE -> new ItemStack(Material.GLOWSTONE_DUST, 8);
            case NETHER_WARTS -> new ItemStack(Material.NETHER_WARTS, 4);
            case QUARTZ_ORE -> new ItemStack(Material.QUARTZ, 4);
            case ENDER_STONE -> new ItemStack(Material.ENDER_PEARL, 2);
            case OBSIDIAN -> new ItemStack(Material.OBSIDIAN, 2);
            case PRISMARINE -> new ItemStack(Material.PRISMARINE_SHARD, 4);
            case SEA_LANTERN -> new ItemStack(Material.PRISMARINE_CRYSTALS, 3);
            case SPONGE -> new ItemStack(Material.SPONGE, 1);
            case IRON_ORE -> new ItemStack(Material.IRON_INGOT, 2);
            case COAL_ORE -> new ItemStack(Material.COAL, 3);
            case LAPIS_ORE -> new ItemStack(Material.INK_SACK, 6, (short) 4);
            case REDSTONE_ORE -> new ItemStack(Material.REDSTONE, 6);
            default -> new ItemStack(Material.GOLD_NUGGET, 5);
        };
    }

    private String getBonusDescription(Material blockType) {
        ItemStack bonus = getBonusReward(blockType);
        if (bonus != null) return bonus.getAmount() + "x " + getItemDisplayName(bonus.getType());
        return "Bonus mystère";
    }

    private String getBlockDisplayName(Material material) {
        return switch (material) {
            case COAL_ORE -> "Minerai de Charbon";
            case IRON_ORE -> "Minerai de Fer";
            case GOLD_ORE -> "Minerai d'Or";
            case DIAMOND_ORE -> "Minerai de Diamant";
            case EMERALD_ORE -> "Minerai d'Émeraude";
            case LAPIS_ORE -> "Minerai de Lapis";
            case REDSTONE_ORE -> "Minerai de Redstone";
            case QUARTZ_ORE -> "Minerai de Quartz";
            case OBSIDIAN -> "Obsidienne";
            case ENDER_STONE -> "Pierre de l'End";
            case NETHERRACK -> "Netherrack";
            case GLOWSTONE -> "Pierre Lumineuse";
            case PRISMARINE -> "Prismarine";
            case SEA_LANTERN -> "Lanterne de Mer";
            case SPONGE -> "Éponge";
            default -> material.name().replace("_", " ");
        };
    }

    private String getItemDisplayName(Material material) {
        return switch (material) {
            case DIAMOND -> "Diamant";
            case EMERALD -> "Émeraude";
            case GOLD_INGOT -> "Lingot d'Or";
            case IRON_INGOT -> "Lingot de Fer";
            case COAL -> "Charbon";
            case GOLDEN_APPLE -> "Pomme d'Or";
            case ENCHANTED_BOOK -> "Livre Enchanté";
            case NETHER_STAR -> "Étoile du Nether";
            case ENDER_PEARL -> "Perle d'Ender";
            default -> material.name().replace("_", " ");
        };
    }
}
