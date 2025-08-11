package net.novaproject.novauhc.listener.entity;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.DropItemRate;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class EntityDeathEvent implements Listener{

    @EventHandler
    public void onEntityDeath(org.bukkit.event.entity.EntityDeathEvent event) {

        if (UHCManager.get().isLobby())
            return;

        Entity entity = event.getEntity();
        Player killer = event.getEntity().getKiller();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onEntityDeath(entity, killer, event);
        });
        if (event.getEntity() instanceof org.bukkit.entity.Cow) {
            if (DropItemRate.LEATHER.getAmount() != 0) {
                int r = new Random().nextInt(100);
                if (r <= DropItemRate.LEATHER.getAmount())
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.LEATHER, 1));
            }
        } else if (event.getEntity() instanceof org.bukkit.entity.Enderman) {
            if (DropItemRate.ENDERPEARL.getAmount() != 0) {
                int r = new Random().nextInt(100);
                if (r <= DropItemRate.ENDERPEARL.getAmount())
                    event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.ENDER_PEARL, 1));
            }
        } else if (event.getEntity() instanceof org.bukkit.entity.Skeleton && DropItemRate.ARROW
                .getAmount() != 0) {
            int r = new Random().nextInt(100);
            if (r <= DropItemRate.ARROW.getAmount())
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.ARROW, 1));
        }
    }

    @EventHandler
    public void onEntityExplose(EntityExplodeEvent event) {

        Entity entity = event.getEntity();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onEtityExplose(entity, event);
        });
    }

}