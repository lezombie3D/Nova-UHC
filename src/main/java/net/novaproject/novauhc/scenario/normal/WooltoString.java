package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.material.MaterialData;

public class WooltoString extends Scenario {
    @Override
    public String getName() {
        return "WooltoString";
    }

    @Override
    public String getDescription() {
        return "Convertit automatiquement la laine en ficelle.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOL);
    }

    @Override
    public void setup() {
        for (byte i = 0; i < 16; i++) { // Les variantes de laine vont de 0 à 15
            MaterialData woolData = new MaterialData(Material.WOOL, i);

            ShapedRecipe r1 = new ShapedRecipe(new ItemStack(Material.STRING, 1));
            r1.shape("WW ", "WW ", "   ");
            r1.setIngredient('W', woolData);
            Bukkit.getServer().addRecipe(r1);

            ShapedRecipe r2 = new ShapedRecipe(new ItemStack(Material.STRING, 1));
            r2.shape(" WW", " WW", "   ");
            r2.setIngredient('W', woolData);
            Bukkit.getServer().addRecipe(r2);

            ShapedRecipe r3 = new ShapedRecipe(new ItemStack(Material.STRING, 1));
            r3.shape("   ", "WW ", "WW ");
            r3.setIngredient('W', woolData);
            Bukkit.getServer().addRecipe(r3);

            ShapedRecipe r4 = new ShapedRecipe(new ItemStack(Material.STRING, 1));
            r4.shape("   ", " WW", " WW");
            r4.setIngredient('W', woolData);
            Bukkit.getServer().addRecipe(r4);
        }
    }
}
