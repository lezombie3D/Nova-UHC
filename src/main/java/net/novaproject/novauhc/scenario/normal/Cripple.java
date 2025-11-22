package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Cripple extends Scenario {
    @Override
    public String getName() {
        return "Cripple";
    }

    @Override
    public String getDescription() {
        return "Rend Tout les joueur faibles";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BONE);
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (entity instanceof Player player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20 * 30, 0, false, false));
            }
        }
    }
}
