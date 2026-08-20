package net.novaproject.ultimate.nuzlocke.roles.dragon;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.ProjectileImpacting;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

public class DragonBreathBow extends Ability implements ProjectileImpacting {

    @Var(name = "Nuzlocke Dragon Breath Radius", type = VariableType.INTEGER)
    private int proximityRadius = 3;

    @Var(name = "Nuzlocke Dragon Breath Damage", type = VariableType.DOUBLE)
    private double damage = 4.0;

    @Override public String getName() { return "Souffle du Dragon"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;

        double velocityFactor = Math.max(0.5, arrow.getVelocity().length() / 3.0);
        double scaledDamage = damage * velocityFactor;
        for (Player target : arrow.getWorld().getPlayers()) {
            if (target.equals(shooter)) continue;
            if (target.getLocation().distance(arrow.getLocation()) > proximityRadius) continue;
            target.damage(scaledDamage, shooter);
        }
    }
}

