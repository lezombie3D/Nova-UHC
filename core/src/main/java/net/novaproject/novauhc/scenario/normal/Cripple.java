package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Cripple extends Scenario {

    @ScenarioVariable(
            name = "weakness_duration",
            description = "Durée de l'effet Weakness en secondes",
            type = VariableType.TIME
    )
    private int weaknessDuration = 30;

    @ScenarioVariable(
            name = "weakness_level",
            description = "Niveau de l'effet Weakness",
            type = VariableType.INTEGER
    )
    private int weaknessLevel = 0;

    @Override
    public String getName() {
        return "Cripple";
    }

    @Override
    public String getDescription() {
        return "Rend tous les joueurs faibles pendant " + weaknessDuration + " secondes";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BONE);
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (entity instanceof Player player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.WEAKNESS,
                        weaknessDuration * 20,
                        weaknessLevel,
                        false,
                        false
                ));
            }
        }
    }
}
