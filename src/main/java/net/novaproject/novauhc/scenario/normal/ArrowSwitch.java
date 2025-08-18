package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ArrowSwitch extends Scenario {
    @Override
    public String getName() {
        return "SwitchArrow";
    }

    @Override
    public String getDescription() {
        return "Les joueurs touchés par une flèche échangent leur position avec le tireur.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ARROW);
    }

    @Override
    public void onHit(Entity entity, Entity dammager, EntityDamageByEntityEvent event) {
        if (entity.getType() != EntityType.PLAYER) return;

        if (dammager instanceof Arrow) {
            Player taper = (Player) event.getEntity();

            Arrow f = (Arrow) dammager;
            if (f.getShooter() instanceof Player) {
                Player shooter = (Player) f.getShooter();

                Location personne_taper = taper.getLocation();
                Location personne_tappant = shooter.getLocation();

                taper.teleport(personne_tappant);
                shooter.teleport(personne_taper);
            }
        }
    }
}
