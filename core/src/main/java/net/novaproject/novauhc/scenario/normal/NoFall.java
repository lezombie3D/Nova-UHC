package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class NoFall extends Scenario {
    @Override
    public String getName() {
        return "No Fall";
    }

    @Override
    public String getDescription() {
        return "Permet de ne pas prendre de dégât de chute";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_BOOTS);
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {

        if (entity instanceof org.bukkit.entity.Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }

    }

    
    
}
