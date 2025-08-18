package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SafeMiner extends Scenario {

    private boolean actived = true;

    @Override
    public String getName() {
        return "SafeMiner";
    }

    @Override
    public String getDescription() {
        return "Protège les joueurs des dégâts de lave et d'étouffement en minant.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_PICKAXE);
    }

    @Override
    public void onPlayerTakeDamage(Entity entity, EntityDamageEvent event) {
        if (actived) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (player.getLocation().getY() <= 60) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        int timerpvp = UHCManager.get().getTimerpvp();

        if (timer == timerpvp) {
            actived = false;
        }
    }
}
