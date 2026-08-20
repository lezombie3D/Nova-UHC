package net.novaproject.ultimate.nuzlocke.roles.water;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Attacking;
import net.novaproject.novauhc.ability.AbilityHooks.Shooting;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class WaterMuddyBow extends Ability implements Attacking, Shooting {

    @Var(name = "Nuzlocke Water Muddy Chance", type = VariableType.PERCENTAGE)
    private double slipApplyChance = 0.50;

    @Var(name = "Nuzlocke Water Muddy Dur", type = VariableType.TIME)
    private int slipDurationSeconds = 45;

    @Var(name = "Nuzlocke Water Muddy Slip Chance", type = VariableType.PERCENTAGE)
    private double slipFizzleChance = 0.10;

    @Var(name = "Nuzlocke Water Muddy Slow Mult", type = VariableType.DOUBLE)
    private double arrowSlowMultiplier = 0.4;

    public static final Map<UUID, Long> SLIPPED_UNTIL = new HashMap<>();

    @Override public String getName() { return "Muddy Water"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onAttack(UHCPlayer victimP, EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;
        Material at = shooter.getLocation().getBlock().getType();
        if (at != Material.WATER && at != Material.STATIONARY_WATER) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (ThreadLocalRandom.current().nextDouble() < slipApplyChance) {
            SLIPPED_UNTIL.put(victim.getUniqueId(),
                    System.currentTimeMillis() + slipDurationSeconds * 1000L);
            victim.sendMessage("§9§lMuddy Water §8│ §7Vos mains glissent, vos flèches sont moins efficaces.");
        }
    }

    @Override
    public void onBow(Entity shooter, Player target, EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        Long until = SLIPPED_UNTIL.get(p.getUniqueId());
        if (until == null || System.currentTimeMillis() >= until) return;
        if (ThreadLocalRandom.current().nextDouble() < slipFizzleChance) {
            event.setCancelled(true);
            p.sendMessage("§9§lMuddy Water §8│ §7Slip ! Votre flèche tombe à vos pieds.");
            return;
        }
        if (event.getProjectile().getVelocity() != null) {
            event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(arrowSlowMultiplier));
        }
    }
}

