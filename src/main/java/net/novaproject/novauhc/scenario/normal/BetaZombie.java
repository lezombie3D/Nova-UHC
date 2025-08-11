package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class BetaZombie extends Scenario {
    @Override
    public String getName() {
        return "BetaZombie";
    }

    @Override
    public String getDescription() {
        return "Fait en sorte que les zombie drop des plume";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SKULL_ITEM);

    }

    @Override
    public void onEntityDeath(Entity entity, Player killer, EntityDeathEvent e) {

        if (e.getEntityType() == EntityType.ZOMBIE) {
            e.getDrops().clear();
            e.getDrops().add(new ItemStack(Material.FEATHER, 3));
            e.setDroppedExp(10);
        }
    }
}
