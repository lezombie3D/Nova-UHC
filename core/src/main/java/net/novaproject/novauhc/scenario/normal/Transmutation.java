package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
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
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.LangManager;

public class Transmutation extends Scenario {

    @Override
    public Family getFamily() { return Family.CRAFT; }

    private final Map<Material, TransmutationRecipe> transmutationRecipes = new HashMap<>();

    @Var(lang = ScenarioVarLang.class, nameKey = "TRANSMUTATION_VAR_IRON_INPUT_NAME", descKey = "TRANSMUTATION_VAR_IRON_INPUT_DESC", type = VariableType.INTEGER)
    private int iron_input = 8;

    @Var(lang = ScenarioVarLang.class, nameKey = "TRANSMUTATION_VAR_GOLD_INPUT_NAME", descKey = "TRANSMUTATION_VAR_GOLD_INPUT_DESC", type = VariableType.INTEGER)
    private int gold_input = 16;

    @Var(lang = ScenarioVarLang.class, nameKey = "TRANSMUTATION_VAR_COAL_INPUT_NAME", descKey = "TRANSMUTATION_VAR_COAL_INPUT_DESC", type = VariableType.INTEGER)
    private int coal_input = 16;

    @Var(lang = ScenarioVarLang.class, nameKey = "TRANSMUTATION_VAR_DIAMOND_OUTPUT_NAME", descKey = "TRANSMUTATION_VAR_DIAMOND_OUTPUT_DESC", type = VariableType.INTEGER)
    private int diamond_output = 1;

    @Var(lang = ScenarioVarLang.class, nameKey = "TRANSMUTATION_VAR_GOLD_OUTPUT_NAME", descKey = "TRANSMUTATION_VAR_GOLD_OUTPUT_DESC", type = VariableType.INTEGER)
    private int gold_output = 1;

    @Var(lang = ScenarioVarLang.class, nameKey = "TRANSMUTATION_VAR_IRON_OUTPUT_NAME", descKey = "TRANSMUTATION_VAR_IRON_OUTPUT_DESC", type = VariableType.INTEGER)
    private int iron_output = 1;

    @Override
    public String getName() {
        return "Transmutation";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.TRANSMUTATION, player);
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

