package net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.fatalis;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.UseAbiliy;
import net.novaproject.novauhc.ability.utils.AbilityVariable;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.DragonFall;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.ElementType;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.FireBlight;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusEffect;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusFactory;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlameNoir extends UseAbiliy {



    @AbilityVariable(name = "Distance", description = "Distance parcourue", type = VariableType.DOUBLE)
    private double MAX_DISTANCE = 15.0;

    @AbilityVariable(name = "Degat", description = "Dégâts", type = VariableType.DOUBLE)
    private int degat = 300;

    private static final double STEP = 1.0;
    private static final int PARTICLES_PER_TICK = 9;
    private static final double ARC_WIDTH_DEGREES = 90.0;
    private static final double HIT_RADIUS = 0.5;

    @Override
    public String getName() {
        return "Flame Noir";
    }

    public FlameNoir(){
        setCooldown(150);
        setMaxUse(-1);
    }

    @Override
    public Material getMaterial() {
        return Material.BLAZE_POWDER;
    }

    @Override
    public boolean onEnable(Player shooter) {

        final Location origin = shooter.getLocation().clone().add(0, 1.0, 0);
        final Vector forward = origin.getDirection().normalize();
        final UHCPlayer owner = getUHCPlayer(shooter);

        final ScenarioRole<DragonRole> scenario =
                ScenarioManager.get().getScenario(DragonFall.class);
        final DragonRole shooterRole = scenario.getRoleByUHCPlayer(owner);

        final Set<Player> alreadyHit = new HashSet<>();

        new BukkitRunnable() {
            double distance = 0;

            @Override
            public void run() {
                if (distance >= MAX_DISTANCE) {
                    cancel();
                    return;
                }

                double radius = distance;
                double halfArc = Math.toRadians(ARC_WIDTH_DEGREES / 2);

                Vector baseDir = forward.clone().multiply(radius);

                for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                    double t = (double) i / (PARTICLES_PER_TICK - 1);
                    double angle = (t - 0.5) * 2 * halfArc;

                    Vector dir = rotateAroundY(baseDir.clone(), angle);
                    Location point = origin.clone().add(dir).add(0, 0.1, 0);

                    if (point.getBlock().getType().isSolid()) {
                        point.getWorld().playSound(point, Sound.GLASS, 0.8f, 1.4f);
                        cancel();
                        return;
                    }

                    new ParticleBuilder(ParticleEffect.REDSTONE)
                            .setColor(Color.BLACK)
                            .setLocation(point)
                            .display();

                    for (Entity entity : point.getWorld()
                            .getNearbyEntities(point, HIT_RADIUS, HIT_RADIUS, HIT_RADIUS)) {

                        if (!(entity instanceof Player target)) continue;
                        if (target == shooter) continue;
                        if (alreadyHit.contains(target)) continue;

                        UHCPlayer targetUhc = UHCPlayerManager.get().getPlayer(target);
                        if (targetUhc == null) continue;

                        if (owner.getTeam().isPresent()
                                && owner.getTeam().get().getPlayers().contains(targetUhc))
                            continue;

                        DragonRole targetRole = scenario.getRoleByUHCPlayer(targetUhc);

                        targetRole.damage(degat, shooter);

                        StatusEffect effect = StatusFactory.create(
                                ElementType.FIRE,
                                target,
                                shooterRole.getBlightDuration(ElementType.FIRE),
                                targetRole
                        );
                        if (effect != null) {
                            StatusManager.get().applyEffect(target, effect);
                        }

                        Vector kb = target.getLocation().toVector()
                                .subtract(shooter.getLocation().toVector())
                                .normalize()
                                .multiply(1.2)
                                .setY(0.1);
                        target.setVelocity(kb);

                        ParticleEffect.CRIT_MAGIC.display(
                                target.getLocation().add(0, 1, 0),
                                0.3f, 0.3f, 0.3f, 0f, 10, null
                        );
                        target.playSound(target.getLocation(), Sound.HURT_FLESH, 0.8f, 1.2f);

                        alreadyHit.add(target);
                    }
                }

                distance += STEP;
            }
        }.runTaskTimer(Main.get(), 0L, 1L);

        return true;
    }

    public static Vector rotateAroundY(Vector vec, double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        return new Vector(
                vec.getX() * cos - vec.getZ() * sin,
                vec.getY(),
                vec.getX() * sin + vec.getZ() * cos
        );
    }
}

