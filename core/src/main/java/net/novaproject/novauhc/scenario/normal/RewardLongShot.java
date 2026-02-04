package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class RewardLongShot extends Scenario {

    @ScenarioVariable(
            name = "Minimum Distance",
            description = "Distance minimale (en blocs) pour qu'un tir soit considéré comme un long shot.",
            type = VariableType.INTEGER
    )
    private int min_distance = 75;

    @ScenarioVariable(
            name = "Damage Multiplier",
            description = "Multiplicateur de dégâts appliqué lors d'un long shot.",
            type = VariableType.DOUBLE
    )
    private double damage_multiplier = 1.5;

    @ScenarioVariable(
            name = "Heal Amount",
            description = "Nombre de points de vie rendus au tireur lors d'un long shot (2 = 1 cœur).",
            type = VariableType.DOUBLE
    )
    private double heal_amount = 2.0;

    @Override
    public String getName() {
        return "Rewarding LongShot";
    }

    @Override
    public String getDescription() {
        return "Les tirs à l'arc longue distance infligent plus de dégâts et soignent le tireur.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOW);
    }

    @Override
    public void onHit(Entity entity, Entity damager, EntityDamageByEntityEvent event) {
        if (!(damager instanceof Projectile projectile)) return;
        if (projectile.getType() != EntityType.ARROW) return;
        if (!(projectile.getShooter() instanceof Player shooter)) return;

        if (shooter.getLocation().distance(entity.getLocation()) < min_distance) return;

        event.setDamage(event.getDamage() * damage_multiplier);

        double newHealth = Math.min(
                shooter.getMaxHealth(),
                shooter.getHealth() + heal_amount
        );
        shooter.setHealth(newHealth);

        shooter.sendMessage(Common.get().getServertag() + " §aLong shot !");
    }
}
