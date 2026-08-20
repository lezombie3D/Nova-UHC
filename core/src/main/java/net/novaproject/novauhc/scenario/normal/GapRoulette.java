package net.novaproject.novauhc.scenario.normal;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.scenario.Scenario;

import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.novaproject.novauhc.lang.lang.ScenarioVarLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.LangManager;

public class GapRoulette extends Scenario {

    @Override
    public Family getFamily() { return Family.NOURRITURE; }

    private final Random random = new Random();

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_SPEED_NAME", descKey = "GAPROULETTE_VAR_ENABLE_SPEED_DESC", type = VariableType.BOOLEAN)
    private boolean enableSpeed = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_STRENGTH_NAME", descKey = "GAPROULETTE_VAR_ENABLE_STRENGTH_DESC", type = VariableType.BOOLEAN)
    private boolean enableStrength = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_RESISTANCE_NAME", descKey = "GAPROULETTE_VAR_ENABLE_RESISTANCE_DESC", type = VariableType.BOOLEAN)
    private boolean enableResistance = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_JUMP_NAME", descKey = "GAPROULETTE_VAR_ENABLE_JUMP_DESC", type = VariableType.BOOLEAN)
    private boolean enableJump = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_FIRE_RESISTANCE_NAME", descKey = "GAPROULETTE_VAR_ENABLE_FIRE_RESISTANCE_DESC", type = VariableType.BOOLEAN)
    private boolean enableFireResistance = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_WEAKNESS_NAME", descKey = "GAPROULETTE_VAR_ENABLE_WEAKNESS_DESC", type = VariableType.BOOLEAN)
    private boolean enableWeakness = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_POISON_NAME", descKey = "GAPROULETTE_VAR_ENABLE_POISON_DESC", type = VariableType.BOOLEAN)
    private boolean enablePoison = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_SLOWNESS_NAME", descKey = "GAPROULETTE_VAR_ENABLE_SLOWNESS_DESC", type = VariableType.BOOLEAN)
    private boolean enableSlowness = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_HUNGER_NAME", descKey = "GAPROULETTE_VAR_ENABLE_HUNGER_DESC", type = VariableType.BOOLEAN)
    private boolean enableHunger = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_ENABLE_WITHER_NAME", descKey = "GAPROULETTE_VAR_ENABLE_WITHER_DESC", type = VariableType.BOOLEAN)
    private boolean enableWither = true;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_MIN_DURATION_NAME", descKey = "GAPROULETTE_VAR_MIN_DURATION_DESC", type = VariableType.INTEGER)
    private int minDuration = 5;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_MAX_DURATION_NAME", descKey = "GAPROULETTE_VAR_MAX_DURATION_DESC", type = VariableType.INTEGER)
    private int maxDuration = 15;

    @Var(lang = ScenarioVarLang.class, nameKey = "GAPROULETTE_VAR_MAX_AMPLIFIER_NAME", descKey = "GAPROULETTE_VAR_MAX_AMPLIFIER_DESC", type = VariableType.INTEGER)
    private int maxAmplifier = 1;

    @Override
    public String getName() {
        return "GapRoulette";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.GAP_ROULETTE, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLDEN_APPLE);
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if (item.getType() != Material.GOLDEN_APPLE) return;

        List<PotionEffectType> enabledEffects = new ArrayList<>();
        if (enableSpeed) enabledEffects.add(PotionEffectType.SPEED);
        if (enableStrength) enabledEffects.add(PotionEffectType.INCREASE_DAMAGE);
        if (enableResistance) enabledEffects.add(PotionEffectType.DAMAGE_RESISTANCE);
        if (enableJump) enabledEffects.add(PotionEffectType.JUMP);
        if (enableFireResistance) enabledEffects.add(PotionEffectType.FIRE_RESISTANCE);
        if (enableWeakness) enabledEffects.add(PotionEffectType.WEAKNESS);
        if (enablePoison) enabledEffects.add(PotionEffectType.POISON);
        if (enableSlowness) enabledEffects.add(PotionEffectType.SLOW);
        if (enableHunger) enabledEffects.add(PotionEffectType.HUNGER);
        if (enableWither) enabledEffects.add(PotionEffectType.WITHER);

        if (enabledEffects.isEmpty()) return;

        PotionEffectType randomEffect = enabledEffects.get(random.nextInt(enabledEffects.size()));
        int low = Math.min(minDuration, maxDuration);
        int spread = Math.abs(maxDuration - minDuration) + 1;
        int duration = 20 * Math.max(1, low + random.nextInt(spread));
        int amplifier = random.nextInt(Math.max(1, maxAmplifier + 1));

        player.addPotionEffect(new PotionEffect(randomEffect, duration, amplifier));
    }
}

