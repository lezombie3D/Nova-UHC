package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BuffKiller extends Scenario {

    private final Random random = new Random();

    @ScenarioVariable(
            name = "speed_active",
            description = "Activer l'effet Speed pour le tueur",
            type = VariableType.BOOLEAN
    )
    private boolean speedActive = true;

    @ScenarioVariable(
            name = "strength_active",
            description = "Activer l'effet Strength pour le tueur",
            type = VariableType.BOOLEAN
    )
    private boolean strengthActive = true;

    @ScenarioVariable(
            name = "resistance_active",
            description = "Activer l'effet Resistance pour le tueur",
            type = VariableType.BOOLEAN
    )
    private boolean resistanceActive = true;

    @ScenarioVariable(
            name = "fire_resistance_active",
            description = "Activer l'effet Fire Resistance pour le tueur",
            type = VariableType.BOOLEAN
    )
    private boolean fireResistanceActive = true;

    @ScenarioVariable(
            name = "min_duration",
            description = "Durée minimale des effets en secondes",
            type = VariableType.TIME
    )
    private int minDuration = 5;

    @ScenarioVariable(
            name = "max_duration",
            description = "Durée maximale des effets en secondes",
            type = VariableType.TIME
    )
    private int maxDuration = 15;

    @ScenarioVariable(
            name = "effect_level",
            description = "Niveau des effets de potion appliqués",
            type = VariableType.INTEGER
    )
    private int effectLevel = 0;

    @Override
    public String getName() {
        return "Buff Killer";
    }

    @Override
    public String getDescription() {
        return "Tuer un joueur donne des effets de potion bénéfiques au tueur.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOD_SWORD);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (killer == null) return;

        List<PotionEffectType> activeEffects = new ArrayList<>();
        if (speedActive) activeEffects.add(PotionEffectType.SPEED);
        if (strengthActive) activeEffects.add(PotionEffectType.INCREASE_DAMAGE);
        if (resistanceActive) activeEffects.add(PotionEffectType.DAMAGE_RESISTANCE);
        if (fireResistanceActive) activeEffects.add(PotionEffectType.FIRE_RESISTANCE);

        if (activeEffects.isEmpty()) return;

        PotionEffectType randomEffect = activeEffects.get(random.nextInt(activeEffects.size()));
        int duration = 20 * (minDuration + random.nextInt(maxDuration - minDuration + 1));
        killer.getPlayer().addPotionEffect(new PotionEffect(randomEffect, duration, effectLevel));
    }
}
