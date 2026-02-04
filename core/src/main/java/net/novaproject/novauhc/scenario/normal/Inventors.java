package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
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

    @ScenarioVariable(
            name = "Bonus pour outils en bois",
            description = "Nombre de pommes donné pour le premier à crafter un outil en bois",
            type = VariableType.INTEGER
    )
    private int woodToolBonus = 2;

    @ScenarioVariable(
            name = "Bonus pour outils en pierre",
            description = "Nombre de pains donné pour le premier à crafter un outil en pierre",
            type = VariableType.INTEGER
    )
    private int stoneToolBonus = 3;

    @ScenarioVariable(
            name = "Bonus pour outils en fer",
            description = "Nombre de lingots de fer donné pour le premier à crafter un outil en fer",
            type = VariableType.INTEGER
    )
    private int ironToolBonus = 2;

    @ScenarioVariable(
            name = "Bonus pour outils en diamant",
            description = "Nombre de diamants donné pour le premier à crafter un outil en diamant",
            type = VariableType.INTEGER
    )
    private int diamondToolBonus = 1;

    @ScenarioVariable(
            name = "Récompense milestone 5",
            description = "Type d'objet donné au joueur ayant inventé 5 objets",
            type = VariableType.STRING
    )
    private String milestone5Reward = "ENCHANTED_BOOK";

    @ScenarioVariable(
            name = "Récompense milestone 10",
            description = "Type d'objet donné au joueur ayant inventé 10 objets",
            type = VariableType.STRING
    )
    private String milestone10Reward = "NETHER_STAR";

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

        if (!firstCrafters.containsKey(craftedItem)) {
            firstCrafters.put(craftedItem, playerUuid);
            playerInventions.put(playerUuid, playerInventions.getOrDefault(playerUuid, 0) + 1);

            String itemName = getItemDisplayName(craftedItem);
            Bukkit.broadcastMessage("§e§l[Inventors] §f" + player.getName() +
                    " §fest le premier à crafter §e" + itemName + " §f!");

            giveInventorBonus(player, craftedItem);

            int inventionCount = playerInventions.get(playerUuid);
            if (inventionCount == 5) {
                Bukkit.broadcastMessage("§e§l[Inventors] §f" + player.getName() +
                        " §fa inventé 5 objets ! Récompense spéciale !");
                giveMilestoneReward(player, milestone5Reward);
            } else if (inventionCount == 10) {
                Bukkit.broadcastMessage("§e§l[Inventors] §f" + player.getName() +
                        " §fa inventé 10 objets ! Maître inventeur !");
                giveMilestoneReward(player, milestone10Reward);
            }
        }
    }

    private void giveInventorBonus(Player player, Material craftedItem) {
        ItemStack bonus;
        String bonusDescription;

        switch (craftedItem) {
            case WOOD_PICKAXE, WOOD_AXE, WOOD_SWORD, WOOD_SPADE, WOOD_HOE -> {
                bonus = new ItemStack(Material.APPLE, woodToolBonus);
                bonusDescription = woodToolBonus + " Pommes";
            }
            case STONE_PICKAXE, STONE_AXE, STONE_SWORD, STONE_SPADE, STONE_HOE -> {
                bonus = new ItemStack(Material.BREAD, stoneToolBonus);
                bonusDescription = stoneToolBonus + " Pains";
            }
            case IRON_PICKAXE, IRON_AXE, IRON_SWORD, IRON_SPADE, IRON_HOE -> {
                bonus = new ItemStack(Material.IRON_INGOT, ironToolBonus);
                bonusDescription = ironToolBonus + " Lingots de Fer";
            }
            case DIAMOND_PICKAXE, DIAMOND_AXE, DIAMOND_SWORD, DIAMOND_SPADE, DIAMOND_HOE -> {
                bonus = new ItemStack(Material.DIAMOND, diamondToolBonus);
                bonusDescription = diamondToolBonus + " Diamants";
            }
            default -> {
                bonus = new ItemStack(Material.GOLD_NUGGET, 3);
                bonusDescription = "3 Pépites d'Or";
            }
        }

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(bonus);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), bonus);
            player.sendMessage("§e[Inventors] §fInventaire plein ! Bonus jeté au sol.");
        }

        player.sendMessage("§e[Inventors] §fBonus d'invention : §e" + bonusDescription + " §f!");
    }

    private void giveMilestoneReward(Player player, String rewardType) {
        Material material = Material.getMaterial(rewardType);
        if (material == null) return;

        ItemStack reward = new ItemStack(material, 1);

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(reward);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), reward);
        }
    }

    private String getItemDisplayName(Material material) {
        return material.name().replace("_", " ");
    }

    public int getPlayerInventions(Player player) {
        return playerInventions.getOrDefault(player.getUniqueId(), 0);
    }

    public Player getFirstCrafter(Material material) {
        UUID crafterUuid = firstCrafters.get(material);
        return crafterUuid != null ? Bukkit.getPlayer(crafterUuid) : null;
    }

    public boolean hasBeenInvented(Material material) {
        return firstCrafters.containsKey(material);
    }

    public Map<Material, UUID> getAllInventions() {
        return new HashMap<>(firstCrafters);
    }

    public void resetInventions() {
        firstCrafters.clear();
        playerInventions.clear();
        Bukkit.broadcastMessage("§e[Inventors] §fToutes les inventions ont été réinitialisées !");
    }

    public Map<UUID, Integer> getInventorLeaderboard() {
        return new HashMap<>(playerInventions);
    }
}
