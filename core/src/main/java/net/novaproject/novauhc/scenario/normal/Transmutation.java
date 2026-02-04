package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.HashMap;
import java.util.Map;

public class Transmutation extends Scenario {

    private final Map<Material, TransmutationRecipe> transmutationRecipes = new HashMap<>();

    @ScenarioVariable(
            name = "Iron Input",
            description = "Nombre de minerais de fer nécessaires pour obtenir un lingot d'or.",
            type = VariableType.INTEGER
    )
    private int iron_input = 8;

    @ScenarioVariable(
            name = "Gold Input",
            description = "Nombre de lingots d'or nécessaires pour obtenir un diamant.",
            type = VariableType.INTEGER
    )
    private int gold_input = 16;

    @ScenarioVariable(
            name = "Coal Input",
            description = "Nombre de charbons nécessaires pour obtenir un lingot de fer.",
            type = VariableType.INTEGER
    )
    private int coal_input = 16;

    @ScenarioVariable(
            name = "Diamond Output",
            description = "Nombre de diamants obtenus lors d'une transmutation or → diamant.",
            type = VariableType.INTEGER
    )
    private int diamond_output = 1;

    @ScenarioVariable(
            name = "Gold Output",
            description = "Nombre de lingots d'or obtenus lors d'une transmutation fer → or.",
            type = VariableType.INTEGER
    )
    private int gold_output = 1;

    @ScenarioVariable(
            name = "Iron Output",
            description = "Nombre de lingots de fer obtenus lors d'une transmutation charbon → fer.",
            type = VariableType.INTEGER
    )
    private int iron_output = 1;

    @Override
    public String getName() {
        return "Transmutation";
    }

    @Override
    public String getDescription() {
        return "Transformez des ressources via l'alchimie (craft uniquement).";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BREWING_STAND_ITEM);
    }

    @Override
    public void onGameStart() {
        initializeTransmutationRecipes();
        registerRecipes();
    }

    @Override
    public void onCraft(ItemStack result, CraftItemEvent event) {
        if (!isActive()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Recipe recipe = event.getRecipe();
        if (!(recipe instanceof ShapelessRecipe shapeless)) return;

        TransmutationRecipe tr = getMatchingRecipe(shapeless);
        if (tr == null) return;

        event.setCancelled(true);

        ItemStack[] matrix = event.getInventory().getMatrix();
        for (int i = 0; i < matrix.length; i++) {
            ItemStack item = matrix[i];
            if (item != null && item.getType() == tr.inputMaterial()) {
                item.setAmount(item.getAmount() - tr.inputAmount());
                matrix[i] = item.getAmount() > 0 ? item : null;
                break;
            }
        }
        event.getInventory().setMatrix(matrix);

        player.getInventory().addItem(
                new ItemStack(tr.outputMaterial(), tr.outputAmount())
        );

        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1f, 1.5f);
        new ParticleBuilder(ParticleEffect.CLOUD)
                .setLocation(player.getLocation())
                .display();
    }

    private void initializeTransmutationRecipes() {
        addRecipe(Material.COAL, coal_input, Material.IRON_INGOT, iron_output);
        addRecipe(Material.IRON_INGOT, iron_input, Material.GOLD_INGOT, gold_output);
        addRecipe(Material.GOLD_INGOT, gold_input, Material.DIAMOND, diamond_output);
    }

    private void addRecipe(Material input, int inputAmount, Material output, int outputAmount) {
        transmutationRecipes.put(input,
                new TransmutationRecipe(input, inputAmount, output, outputAmount));
    }

    private void registerRecipes() {
        for (TransmutationRecipe tr : transmutationRecipes.values()) {
            ShapelessRecipe recipe = new ShapelessRecipe(
                    new ItemStack(tr.outputMaterial(), tr.outputAmount())
            );
            recipe.addIngredient(tr.inputAmount(), tr.inputMaterial());
            Bukkit.addRecipe(recipe);
        }
    }

    private TransmutationRecipe getMatchingRecipe(ShapelessRecipe recipe) {
        if (recipe.getIngredientList().size() != 1) return null;

        ItemStack ingredient = recipe.getIngredientList().get(0);
        if (ingredient == null) return null;

        TransmutationRecipe tr = transmutationRecipes.get(ingredient.getType());
        if (tr == null) return null;

        return ingredient.getAmount() == tr.inputAmount() ? tr : null;
    }

    public record TransmutationRecipe(
            Material inputMaterial,
            int inputAmount,
            Material outputMaterial,
            int outputAmount
    ) {}
}
