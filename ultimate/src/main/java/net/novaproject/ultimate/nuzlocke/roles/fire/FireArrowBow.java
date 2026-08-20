package net.novaproject.ultimate.nuzlocke.roles.fire;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.ability.AbilityHooks.ProjectileImpacting;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.concurrent.ThreadLocalRandom;

public class FireArrowBow extends Ability implements ProjectileImpacting, Attacking {

    @Var(name = "Nuzlocke Fire Arrow Ignite Chance", type = VariableType.PERCENTAGE)
    private double igniteChance = 0.15;

    @Var(name = "Nuzlocke Fire Arrow Ignite Ticks", type = VariableType.INTEGER)
    private int igniteTicks = 100;

    @Var(name = "Nuzlocke Fire Arrow Radius", type = VariableType.INTEGER)
    private int groundRadius = 1;

    @Override public String getName() { return "Flèche Enflammée"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;

        Block impact = arrow.getLocation().getBlock();
        for (int dx = -groundRadius; dx <= groundRadius; dx++) {
            for (int dz = -groundRadius; dz <= groundRadius; dz++) {
                Block above = impact.getRelative(dx, 0, dz);
                Block below = above.getRelative(0, -1, 0);
                if (above.getType() == Material.AIR && below.getType().isSolid()
                        && below.getType() != Material.WATER && below.getType() != Material.STATIONARY_WATER) {
                    above.setType(Material.FIRE);
                }
            }
        }
    }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;
        Entity victim = event.getEntity();
        if (!(victim instanceof LivingEntity living)) return;
        if (ThreadLocalRandom.current().nextDouble() < igniteChance) {
            living.setFireTicks(igniteTicks);
        }
    }
}

