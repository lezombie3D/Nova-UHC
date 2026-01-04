package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Inventors extends Scenario {

    private final Map<Material, UUID> firstCrafters = new HashMap<>();
    private final Map<UUID, Integer> playerInventions = new HashMap<>();

    @Override
    public String getName() {
        return "Inventors";
    }

    @Override
    public String getDescription() {
        return "Le premier à crafter un objet est annoncé et reçoit un bonus !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WORKBENCH);
    }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {
        if (!isActive()) return;

        Player player = (Player) event.getWhoClicked();
        Material craftedItem = result.getType();
        UUID playerUuid = player.getUniqueId();

        // Check if this is the first time this item is crafted
        if (!firstCrafters.containsKey(craftedItem)) {
            firstCrafters.put(craftedItem, playerUuid);

            // Increment player's invention count
            playerInventions.put(playerUuid, playerInventions.getOrDefault(playerUuid, 0) + 1);

            // Announce the invention
            String itemName = getItemDisplayName(craftedItem);
            Bukkit.broadcastMessage("§e§l[Inventors] §f" + player.getName() +
                    " §fest le premier à crafter §e" + itemName + " §f!");

            // Give bonus reward
            giveInventorBonus(player, craftedItem);

            // Special milestone rewards
            int inventionCount = playerInventions.get(playerUuid);
            if (inventionCount == 5) {
                Bukkit.broadcastMessage("§e§l[Inventors] §f" + player.getName() +
                        " §fa inventé 5 objets ! Récompense spéciale !");
                giveMilestoneReward(player, 5);
            } else if (inventionCount == 10) {
                Bukkit.broadcastMessage("§e§l[Inventors] §f" + player.getName() +
                        " §fa inventé 10 objets ! Maître inventeur !");
                giveMilestoneReward(player, 10);
            }
        }
    }

    private void giveInventorBonus(Player player, Material craftedItem) {
        // Give bonus based on item rarity/importance
        ItemStack bonus = null;
        String bonusDescription = "";

        switch (craftedItem) {
            // Tools
            case WOOD_PICKAXE:
            case WOOD_AXE:
            case WOOD_SWORD:
            case WOOD_SPADE:
            case WOOD_HOE:
                bonus = new ItemStack(Material.APPLE, 2);
                bonusDescription = "2 Pommes";
                break;

            case STONE_PICKAXE:
            case STONE_AXE:
            case STONE_SWORD:
            case STONE_SPADE:
            case STONE_HOE:
                bonus = new ItemStack(Material.BREAD, 3);
                bonusDescription = "3 Pains";
                break;

            case IRON_PICKAXE:
            case IRON_AXE:
            case IRON_SWORD:
            case IRON_SPADE:
            case IRON_HOE:
                bonus = new ItemStack(Material.IRON_INGOT, 2);
                bonusDescription = "2 Lingots de Fer";
                break;

            case DIAMOND_PICKAXE:
            case DIAMOND_AXE:
            case DIAMOND_SWORD:
            case DIAMOND_SPADE:
            case DIAMOND_HOE:
                bonus = new ItemStack(Material.DIAMOND, 1);
                bonusDescription = "1 Diamant";
                break;

            // Armor
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                bonus = new ItemStack(Material.LEATHER, 2);
                bonusDescription = "2 Cuirs";
                break;

            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
                bonus = new ItemStack(Material.IRON_INGOT, 3);
                bonusDescription = "3 Lingots de Fer";
                break;

            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                bonus = new ItemStack(Material.DIAMOND, 2);
                bonusDescription = "2 Diamants";
                break;

            // Special items
            case BOW:
                bonus = new ItemStack(Material.ARROW, 16);
                bonusDescription = "16 Flèches";
                break;

            case FISHING_ROD:
                bonus = new ItemStack(Material.RAW_FISH, 3);
                bonusDescription = "3 Poissons";
                break;

            case FLINT_AND_STEEL:
                bonus = new ItemStack(Material.FLINT, 3);
                bonusDescription = "3 Silex";
                break;

            case ENCHANTMENT_TABLE:
                bonus = new ItemStack(Material.EXP_BOTTLE, 5);
                bonusDescription = "5 Bouteilles d'XP";
                break;

            case ANVIL:
                bonus = new ItemStack(Material.IRON_BLOCK, 1);
                bonusDescription = "1 Bloc de Fer";
                break;

            case BREWING_STAND_ITEM:
                bonus = new ItemStack(Material.NETHER_WARTS, 8);
                bonusDescription = "8 Verrues du Nether";
                break;

            // Food
            case BREAD:
                bonus = new ItemStack(Material.WHEAT, 5);
                bonusDescription = "5 Blés";
                break;

            case CAKE:
                bonus = new ItemStack(Material.GOLDEN_APPLE, 1);
                bonusDescription = "1 Pomme d'Or";
                break;

            case GOLDEN_APPLE:
                bonus = new ItemStack(Material.GOLD_INGOT, 8);
                bonusDescription = "8 Lingots d'Or";
                break;

            // Default bonus
            default:
                bonus = new ItemStack(Material.GOLD_NUGGET, 3);
                bonusDescription = "3 Pépites d'Or";
                break;
        }

        // Give the bonus
        if (bonus != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(bonus);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), bonus);
                player.sendMessage("§e[Inventors] §fInventaire plein ! Bonus jeté au sol.");
            }

            player.sendMessage("§e[Inventors] §fBonus d'invention : §e" + bonusDescription + " §f!");
        }
    }

    private void giveMilestoneReward(Player player, int milestone) {
        ItemStack reward = null;

        if (milestone == 5) {
            // 5 inventions reward
            reward = new ItemStack(Material.ENCHANTED_BOOK, 1);
        } else if (milestone == 10) {
            // 10 inventions reward
            reward = new ItemStack(Material.NETHER_STAR, 1);
        }

        if (reward != null) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(reward);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), reward);
            }
        }
    }

    private String getItemDisplayName(Material material) {
        switch (material) {
            // Tools
            case WOOD_PICKAXE:
                return "Pioche en Bois";
            case STONE_PICKAXE:
                return "Pioche en Pierre";
            case IRON_PICKAXE:
                return "Pioche en Fer";
            case DIAMOND_PICKAXE:
                return "Pioche en Diamant";
            case WOOD_AXE:
                return "Hache en Bois";
            case STONE_AXE:
                return "Hache en Pierre";
            case IRON_AXE:
                return "Hache en Fer";
            case DIAMOND_AXE:
                return "Hache en Diamant";
            case WOOD_SWORD:
                return "Épée en Bois";
            case STONE_SWORD:
                return "Épée en Pierre";
            case IRON_SWORD:
                return "Épée en Fer";
            case DIAMOND_SWORD:
                return "Épée en Diamant";

            // Armor
            case LEATHER_HELMET:
                return "Casque en Cuir";
            case IRON_HELMET:
                return "Casque en Fer";
            case DIAMOND_HELMET:
                return "Casque en Diamant";
            case LEATHER_CHESTPLATE:
                return "Plastron en Cuir";
            case IRON_CHESTPLATE:
                return "Plastron en Fer";
            case DIAMOND_CHESTPLATE:
                return "Plastron en Diamant";

            // Special items
            case BOW:
                return "Arc";
            case FISHING_ROD:
                return "Canne à Pêche";
            case FLINT_AND_STEEL:
                return "Briquet";
            case ENCHANTMENT_TABLE:
                return "Table d'Enchantement";
            case ANVIL:
                return "Enclume";
            case BREWING_STAND_ITEM:
                return "Alambic";
            case WORKBENCH:
                return "Établi";
            case FURNACE:
                return "Four";
            case CHEST:
                return "Coffre";
            case BED:
                return "Lit";

            // Food
            case BREAD:
                return "Pain";
            case CAKE:
                return "Gâteau";
            case GOLDEN_APPLE:
                return "Pomme d'Or";
            case MUSHROOM_SOUP:
                return "Soupe aux Champignons";

            default:
                return material.name().replace("_", " ");
        }
    }

    // Get player's invention count
    public int getPlayerInventions(Player player) {
        return playerInventions.getOrDefault(player.getUniqueId(), 0);
    }

    // Get who first crafted an item
    public Player getFirstCrafter(Material material) {
        UUID crafterUuid = firstCrafters.get(material);
        return crafterUuid != null ? Bukkit.getPlayer(crafterUuid) : null;
    }

    // Check if an item has been invented yet
    public boolean hasBeenInvented(Material material) {
        return firstCrafters.containsKey(material);
    }

    // Get all inventions and their inventors
    public Map<Material, UUID> getAllInventions() {
        return new HashMap<>(firstCrafters);
    }

    // Reset inventions (admin command)
    public void resetInventions() {
        firstCrafters.clear();
        playerInventions.clear();
        Bukkit.broadcastMessage("§e[Inventors] §fToutes les inventions ont été réinitialisées !");
    }

    // Get leaderboard of inventors
    public Map<UUID, Integer> getInventorLeaderboard() {
        return new HashMap<>(playerInventions);
    }
}
