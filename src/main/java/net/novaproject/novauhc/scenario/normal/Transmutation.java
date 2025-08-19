package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.HashMap;
import java.util.Map;

public class Transmutation extends Scenario {

    private final Map<Material, TransmutationRecipe> transmutationRecipes = new HashMap<>();

    public Transmutation() {
        initializeTransmutationRecipes();
    }

    @Override
    public String getName() {
        return "Transmutation";
    }

    @Override
    public String getDescription() {
        return "Transformez 8 fer en 1 or, 8 or en 1 diamant grâce à l'alchimie.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BREWING_STAND_ITEM);
    }

    @Override
    public void onPlayerInteract(Player player, PlayerInteractEvent event) {
        if (!isActive()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        // Check if player is holding a transmutation ingredient
        if (item.getType() == Material.IRON_INGOT && item.getAmount() >= 8) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                performTransmutation(player, Material.IRON_INGOT, Material.GOLD_INGOT, 8, 1);
                event.setCancelled(true);
            }
        } else if (item.getType() == Material.GOLD_INGOT && item.getAmount() >= 8) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                performTransmutation(player, Material.GOLD_INGOT, Material.DIAMOND, 8, 1);
                event.setCancelled(true);
            }
        } else if (item.getType() == Material.COAL && item.getAmount() >= 16) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                performTransmutation(player, Material.COAL, Material.IRON_INGOT, 16, 1);
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {
        if (!isActive()) return;

        // Check for transmutation crafting recipes
        Recipe recipe = event.getRecipe();
        if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;

            // Check if this is a transmutation recipe
            if (isTransmutationRecipe(shapelessRecipe)) {
                Player player = (Player) event.getWhoClicked();
                player.sendMessage("§5[Transmutation] §fTransmutation réussie !");
            }
        }
    }

    private void performTransmutation(Player player, Material inputMaterial, Material outputMaterial,
                                      int inputAmount, int outputAmount) {
        ItemStack inputItem = new ItemStack(inputMaterial, inputAmount);

        // Check if player has enough materials
        if (!player.getInventory().containsAtLeast(inputItem, inputAmount)) {
            player.sendMessage("§5[Transmutation] §cVous n'avez pas assez de " +
                    getMaterialName(inputMaterial) + " ! (Besoin de " + inputAmount + ")");
            return;
        }

        // Remove input materials
        player.getInventory().removeItem(inputItem);

        // Add output materials
        ItemStack outputItem = new ItemStack(outputMaterial, outputAmount);
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(outputItem);
        } else {
            player.getWorld().dropItemNaturally(player.getLocation(), outputItem);
            player.sendMessage("§5[Transmutation] §fVotre inventaire est plein ! L'objet a été jeté au sol.");
        }

        // Send success message
        player.sendMessage("§5[Transmutation] §fTransmutation réussie ! " +
                inputAmount + "x " + getMaterialName(inputMaterial) +
                " → " + outputAmount + "x " + getMaterialName(outputMaterial));

        // Visual and sound effects
        player.getWorld().playSound(player.getLocation(),
                org.bukkit.Sound.LEVEL_UP, 1.0f, 1.5f);

        // Create particle effect (if available)
        try {
            player.getWorld().spawnParticle(
                    org.bukkit.Particle.SPELL_WITCH,
                    player.getLocation().add(0, 1, 0),
                    20, 0.5, 0.5, 0.5, 0.1
            );
        } catch (Exception e) {
            // Particle effects not available in this version
        }
    }

    private void initializeTransmutationRecipes() {
        // Coal to Iron (16:1)
        transmutationRecipes.put(Material.COAL,
                new TransmutationRecipe(Material.COAL, 16, Material.IRON_INGOT, 1));

        // Iron to Gold (8:1)
        transmutationRecipes.put(Material.IRON_INGOT,
                new TransmutationRecipe(Material.IRON_INGOT, 8, Material.GOLD_INGOT, 1));

        // Gold to Diamond (8:1)
        transmutationRecipes.put(Material.GOLD_INGOT,
                new TransmutationRecipe(Material.GOLD_INGOT, 8, Material.DIAMOND, 1));
    }

    private boolean isTransmutationRecipe(ShapelessRecipe recipe) {
        // This would need to be implemented based on how you register custom recipes
        // For now, we'll use the interact method instead
        return false;
    }

    private String getMaterialName(Material material) {
        switch (material) {
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
            default:
                return material.name();
        }
    }

    // Get available transmutations for a player
    public String getAvailableTransmutations(Player player) {
        String sb = "§5[Transmutation] §fTransmutations disponibles :\n" +
                "§7- §f16x Charbon → 1x Lingot de Fer (Clic droit)\n" +
                "§7- §f8x Lingot de Fer → 1x Lingot d'Or (Clic droit)\n" +
                "§7- §f8x Lingot d'Or → 1x Diamant (Clic droit)\n" +
                "§7Tenez l'objet en main et faites clic droit pour transmuter !";

        return sb;
    }

    // Check if player can perform a specific transmutation
    public boolean canTransmute(Player player, Material inputMaterial) {
        TransmutationRecipe recipe = transmutationRecipes.get(inputMaterial);
        if (recipe == null) return false;

        ItemStack requiredItem = new ItemStack(inputMaterial, recipe.inputAmount);
        return player.getInventory().containsAtLeast(requiredItem, recipe.inputAmount);
    }

    // Get transmutation recipe for a material
    public TransmutationRecipe getTransmutationRecipe(Material material) {
        return transmutationRecipes.get(material);
    }

    // Admin command to perform transmutation for a player
    public void forceTransmutation(Player player, Material inputMaterial) {
        TransmutationRecipe recipe = transmutationRecipes.get(inputMaterial);
        if (recipe != null) {
            performTransmutation(player, recipe.inputMaterial, recipe.outputMaterial,
                    recipe.inputAmount, recipe.outputAmount);
        }
    }

    // Inner class for transmutation recipes
    public static class TransmutationRecipe {
        public final Material inputMaterial;
        public final int inputAmount;
        public final Material outputMaterial;
        public final int outputAmount;

        public TransmutationRecipe(Material inputMaterial, int inputAmount,
                                   Material outputMaterial, int outputAmount) {
            this.inputMaterial = inputMaterial;
            this.inputAmount = inputAmount;
            this.outputMaterial = outputMaterial;
            this.outputAmount = outputAmount;
        }
    }
}
