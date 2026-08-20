package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.DropItemRate;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public final class EntityListeners {

    private EntityListeners() {
    }

    public static class EntityProjectileHitEvent implements Listener {

        @EventHandler
        public void onProjectileHit(ProjectileHitEvent event) {
            ScenarioManager.get().getActiveScenarios()
                    .forEach(scenario -> scenario.onProjectileHit(event));
        }
    }

    public static class EntityBowEvent implements Listener {

        @EventHandler
        public void onBow(EntityShootBowEvent event) {

            Entity entity = event.getEntity();
            if (!(entity instanceof Player)) {
                return;
            }
            Player player = (Player) event.getEntity();

            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onBow(entity, player, event);
            });

        }

        @EventHandler
        public void onMobSpawn(EntitySpawnEvent event) {
            if(UHCManager.get().isLobby()){
                event.setCancelled(true);
            }
        }
    }

    public static class EntityDamageEntity implements Listener {

        @EventHandler(priority = EventPriority.HIGH)
        public void EntityDamageEntityEVent(EntityDamageByEntityEvent event) {

            Entity entity = event.getEntity();
            Entity damager = event.getDamager();

            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onHit(entity, damager, event);
            });

        }

        @EventHandler
        public void EntityTakeDamage(EntityDamageEvent event) {

            if (!(event.getEntity() instanceof Player player)) {
                return;
            }

            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onDamage(player, event);
            });
        }
    }

    public static class EntityDeathListener implements Listener {

        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {

            if (UHCManager.get().isLobby())
                return;

            Entity entity = event.getEntity();
            Player killer = event.getEntity().getKiller();

            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onEntityDeath(entity, killer, event);
            });

            DropItemRate bonus = null;
            if (entity instanceof Cow) bonus = DropItemRate.LEATHER;
            else if (entity instanceof Enderman) bonus = DropItemRate.ENDERPEARL;
            else if (entity instanceof Skeleton) bonus = DropItemRate.ARROW;
            else if (entity instanceof Chicken) bonus = DropItemRate.FEATHER;
            else if (entity instanceof Spider) bonus = DropItemRate.STRING;
            else if (entity instanceof Creeper) bonus = DropItemRate.GUNPOWDER;
            else if (entity instanceof Blaze) bonus = DropItemRate.BLAZE_ROD;
            else if (entity instanceof MagmaCube) bonus = DropItemRate.MAGMA_CREAM;
            if (bonus != null) bonus.tryDropAt(entity.getLocation());
        }

        @EventHandler
        public void onEntityExplose(EntityExplodeEvent event) {

            Entity entity = event.getEntity();

            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onEntityExplode(entity, event);
            });
        }

    }
}
