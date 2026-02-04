package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;

import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GapRoulette extends Scenario {

    private final List<PotionEffectType> allEffects = new ArrayList<>();
    private final Random random = new Random();

    @ScenarioVariable(
            name = "Effet Vitesse",
            description = "Activer l'effet Vitesse pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableSpeed = true;

    @ScenarioVariable(
            name = "Effet Strength",
            description = "Activer l'effet Strength pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableStrength = true;

    @ScenarioVariable(
            name = "Effet Resistance",
            description = "Activer l'effet Resistance pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableResistance = true;

    @ScenarioVariable(
            name = "Effet Jump",
            description = "Activer l'effet Jump pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableJump = true;

    @ScenarioVariable(
            name = "Effet Fire Resistance",
            description = "Activer l'effet Fire Resistance pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableFireResistance = true;

    @ScenarioVariable(
            name = "Effet Weakness",
            description = "Activer l'effet Weakness pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableWeakness = true;

    @ScenarioVariable(
            name = "Effet Poison",
            description = "Activer l'effet Poison pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enablePoison = true;

    @ScenarioVariable(
            name = "Effet Slowness",
            description = "Activer l'effet Slowness pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableSlowness = true;

    @ScenarioVariable(
            name = "Effet Hunger",
            description = "Activer l'effet Hunger pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableHunger = true;

    @ScenarioVariable(
            name = "Effet Wither",
            description = "Activer l'effet Wither pour les pommes",
            type = VariableType.BOOLEAN
    )
    private boolean enableWither = true;

    @ScenarioVariable(
            name = "Durée minimum (secondes)",
            description = "Durée minimale des effets appliqués",
            type = VariableType.INTEGER
    )
    private int minDuration = 5;

    @ScenarioVariable(
            name = "Durée maximum (secondes)",
            description = "Durée maximale des effets appliqués",
            type = VariableType.INTEGER
    )
    private int maxDuration = 15;

    @ScenarioVariable(
            name = "Amplificateur maximum",
            description = "Amplificateur maximal des effets appliqués",
            type = VariableType.INTEGER
    )
    private int maxAmplifier = 1;

    public GapRoulette() {
        allEffects.add(PotionEffectType.SPEED);
        allEffects.add(PotionEffectType.INCREASE_DAMAGE);
        allEffects.add(PotionEffectType.DAMAGE_RESISTANCE);
        allEffects.add(PotionEffectType.JUMP);
        allEffects.add(PotionEffectType.FIRE_RESISTANCE);
        allEffects.add(PotionEffectType.WEAKNESS);
        allEffects.add(PotionEffectType.POISON);
        allEffects.add(PotionEffectType.SLOW);
        allEffects.add(PotionEffectType.HUNGER);
        allEffects.add(PotionEffectType.WITHER);
    }

    @Override
    public String getName() {
        return "GapRoulette";
    }

    @Override
    public String getDescription() {
        return "Les pommes dorées donnent des effets aléatoires configurables au lieu de leurs effets normaux.";
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
        int duration = 20 * (minDuration + random.nextInt(maxDuration - minDuration + 1));
        int amplifier = random.nextInt(maxAmplifier + 1);

        player.addPotionEffect(new PotionEffect(randomEffect, duration, amplifier));
        player.sendMessage(ChatColor.GOLD + "Vous avez reçu l'effet : " + ChatColor.AQUA + randomEffect.getName());
    }
}
