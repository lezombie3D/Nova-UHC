package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class GoldenHead extends Scenario {

    @Override
    public Family getFamily() { return Family.NOURRITURE; }

    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_ABSORPTION_DURATION_GOLDEN_HEAD_NAME", descKey = "GOLDENHEAD_VAR_ABSORPTION_DURATION_GOLDEN_HEAD_DESC", type = VariableType.TIME)
    private int absorptionDurationGoldenHead = 20 * 60 * 2;
    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_GOLDEN_HEAD_NAME", descKey = "GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_GOLDEN_HEAD_DESC", type = VariableType.INTEGER)
    private int absorptionAmplifierGoldenHead = 1;
    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_REGENERATION_DURATION_GOLDEN_HEAD_NAME", descKey = "GOLDENHEAD_VAR_REGENERATION_DURATION_GOLDEN_HEAD_DESC", type = VariableType.TIME)
    private int regenerationDurationGoldenHead = 20 * 6;
    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_GOLDEN_HEAD_NAME", descKey = "GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_GOLDEN_HEAD_DESC", type = VariableType.INTEGER)
    private int regenerationAmplifierGoldenHead = 1;
    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_ABSORPTION_DURATION_APPLE_NAME", descKey = "GOLDENHEAD_VAR_ABSORPTION_DURATION_APPLE_DESC", type = VariableType.TIME)
    private int absorptionDurationApple = 20 * 60 * 2;
    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_APPLE_NAME", descKey = "GOLDENHEAD_VAR_ABSORPTION_AMPLIFIER_APPLE_DESC", type = VariableType.INTEGER)
    private int absorptionAmplifierApple = 0;
    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_REGENERATION_DURATION_APPLE_NAME", descKey = "GOLDENHEAD_VAR_REGENERATION_DURATION_APPLE_DESC", type = VariableType.TIME)
    private int regenerationDurationApple = 20 * 4;
    @Var(lang = ScenarioVarLang.class, nameKey = "GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_APPLE_NAME", descKey = "GOLDENHEAD_VAR_REGENERATION_AMPLIFIER_APPLE_DESC", type = VariableType.INTEGER)
    private int regenerationAmplifierApple = 1;

    @Var(name = "Chance de drop de la tête", desc = "Chance en pourcentage qu'un joueur tué laisse tomber sa tête.", type = VariableType.PERCENTAGE, min = 0, max = 100)
    private int headDropChance = 100;

    private final Random random = new Random();

    private boolean recipeAdded = false;


    @Override public String getName() { return "Golden Head"; }
    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.GOLDEN_HEAD, player);
    }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.GOLDEN_APPLE); }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive() && !recipeAdded) {
            ShapedRecipe goldenHead = new ShapedRecipe(new ItemCreator(Material.GOLDEN_APPLE)
                    .setName(t(ScenarioLang.GOLDENHEAD_ITEM_NAME)).getItemstack());
            goldenHead.shape("GGG", "GHG", "GGG");
            goldenHead.setIngredient('G', Material.GOLD_INGOT);
            goldenHead.setIngredient('H', new MaterialData(Material.SKULL_ITEM, (byte) 3));
            Bukkit.addRecipe(goldenHead);
            recipeAdded = true;
        } else if (!isActive() && recipeAdded) {
            removeRecipe();
        }
    }

    @Override
    public void onStop() {
        if (recipeAdded) removeRecipe();
    }

    private void removeRecipe() {
        ItemStack head = new ItemCreator(Material.GOLDEN_APPLE)
                .setName(t(ScenarioLang.GOLDENHEAD_ITEM_NAME)).getItemstack();
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();
            if (recipe.getResult().isSimilar(head)) it.remove();
        }
        recipeAdded = false;
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if (item.getType() == Material.GOLDEN_APPLE && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(t(ScenarioLang.GOLDENHEAD_ITEM_NAME))) {
            for (PotionEffect e : player.getActivePotionEffects()) {
                if (e.getType().equals(PotionEffectType.REGENERATION) || e.getType().equals(PotionEffectType.ABSORPTION))
                    player.removePotionEffect(e.getType());
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, absorptionDurationGoldenHead, absorptionAmplifierGoldenHead, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenerationDurationGoldenHead, regenerationAmplifierGoldenHead, false, true));
        } else if (item.getType() == Material.GOLDEN_APPLE) {
            for (PotionEffect e : player.getActivePotionEffects()) {
                if (e.getType().equals(PotionEffectType.REGENERATION) || e.getType().equals(PotionEffectType.ABSORPTION))
                    player.removePotionEffect(e.getType());
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, absorptionDurationApple, absorptionAmplifierApple, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, regenerationDurationApple, regenerationAmplifierApple, false, true));
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (random.nextInt(100) >= headDropChance) return;
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        meta.setOwner(uhcPlayer.getPlayer().getName());
        meta.setDisplayName(t(ScenarioLang.GOLDENHEAD_SKULL_NAME, Map.of("%player%", uhcPlayer.getPlayer().getName())));
        skull.setItemMeta(meta);
        uhcPlayer.getPlayer().getWorld().dropItemNaturally(uhcPlayer.getPlayer().getLocation(), skull);
    }
}

