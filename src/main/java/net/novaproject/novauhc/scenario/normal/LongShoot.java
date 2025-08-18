package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LongShoot extends Scenario {
    @Override
    public String getName() {
        return "LongShot";
    }

    @Override
    public String getDescription() {
        return "Les tirs à l'arc de plus de 75 blocs infligent 50% de dégâts supplémentaires.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOW);
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (dammager instanceof Projectile && dammager.getType().equals(EntityType.ARROW)) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                Player shooter = (Player) projectile.getShooter();
                if (shooter.getLocation().distance(event.getEntity().getLocation()) >= 75) {
                    event.setDamage(event.getDamage() * 1.5);
                    shooter.sendMessage(Common.get().getServertag() + "Long shot");
                }
            }
        }
    }
}
