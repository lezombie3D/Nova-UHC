package net.novaproject.novauhc.ability;

import net.novaproject.novauhc.player.UHCPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public final class AbilityHooks {

    public interface Attacking {
        void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event);
    }

    public interface Building {
        void onBlockPlace(BlockPlaceEvent event);
    }

    public interface Clicking {
        void onClick(PlayerInteractEvent event, ItemStack item);
    }

    public interface Consuming {
        void onConsume(PlayerItemConsumeEvent event);
    }

    public interface Dropping {
        void onDrop(UHCPlayer uhcPlayer, PlayerDropItemEvent event);
    }

    public interface Dying {
        void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event);
    }

    public interface EntityInteracting {
        void onInteractEntity(PlayerInteractEntityEvent event);
    }

    public interface Given {
        void onGive(UHCPlayer uhcPlayer);
    }

    public interface Hurtable {
        void onTakeDamage(EntityDamageEvent event);
    }

    public interface Killing {
        void onKill(UHCPlayer killedP);
    }

    public interface Mining {
        void onBlockBreak(BlockBreakEvent event);
    }

    public interface MobKilling {
        void onMobDeath(EntityDeathEvent event);
    }

    public interface Moving {
        void onMove(PlayerMoveEvent event);
    }

    public interface ProjectileImpacting {
        void onProjectileHit(ProjectileHitEvent event);
    }

    public interface Shooting {
        void onBow(Entity shooter, Player target, EntityShootBowEvent event);
    }

    public interface Ticking {
        void onSec(Player player);
    }
}
