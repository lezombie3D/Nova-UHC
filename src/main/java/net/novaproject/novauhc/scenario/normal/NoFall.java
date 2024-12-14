package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent;

public class NoFall extends Scenario {
    @Override
    public String getName() {
        return "No Fall";
    }

    @Override
    public String getDescription() {
        return "Permet de ne pas predre de degat";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FEATHER);
    }
    @Override
    public void onPlayerTakeDamage(UHCPlayer uhcplayer, EntityDamageEvent event) {

        if (uhcplayer instanceof org.bukkit.entity.Player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }

    
    
}
