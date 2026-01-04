package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

public class BatRoulette extends Scenario {

    @Override
    public String getName() {
        return "BatRoulette";
    }

    @Override
    public String getDescription() {
        return "Tuer une chauve-souris donne un effet aléatoire (positif ou négatif).";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.MONSTER_EGG);
    }

    @Override
    public String getPath() {
        return "batroulette";
    }


    @Override
    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent event) {
        Location loc = entity.getLocation();
        if (entity instanceof Bat && killer != null) {
            int random = new Random().nextInt(100);
            if (random <= getConfig().getInt("pourcentage")) {
                killer.teleport(new Location(Common.get().getArena(), loc.getX(), 300, loc.getZ()));
            } else {
                event.getEntity().getWorld().dropItem(loc, new ItemCreator(Material.GOLDEN_APPLE).getItemstack());
            }
        }
    }
}
