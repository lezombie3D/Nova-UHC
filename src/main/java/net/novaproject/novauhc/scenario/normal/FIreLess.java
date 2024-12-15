package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent;

public class FIreLess extends Scenario {
    @Override
    public String getName() {
        return "FireLess";
    }

    @Override
    public String getDescription() {
        return "Emepeche les joeur de prendre des degat de feu";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.MAGMA_CREAM);
    }
    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {

        if (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) {
            event.setCancelled(true);
        }
    }
}
