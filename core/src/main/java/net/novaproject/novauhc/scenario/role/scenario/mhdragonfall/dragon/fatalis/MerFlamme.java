package net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.dragon.fatalis;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.ability.UseAbiliy;
import net.novaproject.novauhc.ability.utils.AbilityVariable;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.DragonFall;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.DragonRole;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.ElementType;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusEffect;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusFactory;
import net.novaproject.novauhc.scenario.role.scenario.mhdragonfall.status.StatusManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.xenondevs.particle.ParticleBuilder;
import xyz.xenondevs.particle.ParticleEffect;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class MerFlamme extends UseAbiliy {

    @AbilityVariable(name = "Dégâts", description = "Dégâts infligés", type = net.novaproject.novauhc.utils.VariableType.DOUBLE)
    private int DAMAGE = 1000;

    @AbilityVariable(name = "Durée feu", description = "Durée du statut Feu (s)", type = net.novaproject.novauhc.utils.VariableType.INTEGER)
    private int FIRE_DURATION = 15;

    @AbilityVariable(name = "Rayon", description = "Rayon de la vague", type = net.novaproject.novauhc.utils.VariableType.DOUBLE)
    private double RADIUS = 25;

    private static final int PARTICLES = 60;
    private static final int TICKS_DURATION = 50;
    private static final double HEIGHT = 1.0;

    public MerFlamme(){
        setCooldown(300);
        setMaxUse(5);
    }

    @Override
    public String getName() {
        return "Inferno Wave";
    }

    @Override
    public Material getMaterial() {
        return Material.BLAZE_POWDER;
    }

    @Override
    public boolean onEnable(Player caster) {
        UHCPlayer casterUhc = getUHCPlayer(caster);
        ScenarioRole<DragonRole> scenario = ScenarioManager.get().getScenario(DragonFall.class);
        DragonRole casterRole = scenario.getRoleByUHCPlayer(casterUhc);

        Location center = caster.getLocation();

        // Set pour tracker les joueurs déjà touchés
        Set<UUID> hitPlayers = new HashSet<>();

        Random random = new Random();

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (tick++ > TICKS_DURATION) {
                    cancel();
                    return;
                }

                double currentRadius = RADIUS * ((double) tick / TICKS_DURATION);

                // Cercle de particules animé
                for (int i = 0; i < PARTICLES; i++) {
                    double angle = 2 * Math.PI * i / PARTICLES;
                    double x = currentRadius * Math.cos(angle);
                    double z = currentRadius * Math.sin(angle);
                    Location particleLoc = center.clone().add(x, HEIGHT, z);
                    new ParticleBuilder(ParticleEffect.REDSTONE)
                            .setColor(Color.BLACK)
                            .setLocation(particleLoc)
                            .display();
                }

                // Vérifier les joueurs dans le rayon actuel
                for (Player target : center.getWorld().getPlayers()) {
                    // Si le joueur a déjà été touché, on le skip
                    if (hitPlayers.contains(target.getUniqueId())) {
                        continue;
                    }

                    // Le lanceur est invincible à sa propre attaque
                    if (target.getUniqueId().equals(caster.getUniqueId())) {
                        continue;
                    }

                    if (target.getLocation().distance(center) <= currentRadius + 1) {
                        // Marquer le joueur comme touché
                        hitPlayers.add(target.getUniqueId());

                        UHCPlayer targetUhc = getUHCPlayer(target);
                        DragonRole targetRole = scenario.getRoleByUHCPlayer(targetUhc);

                        // Dégâts
                        targetRole.damage(DAMAGE, caster);

                        // Statut Feu
                        StatusEffect effect = StatusFactory.create(
                                ElementType.FIRE,
                                target,
                                FIRE_DURATION,
                                targetRole
                        );
                        if (effect != null) StatusManager.get().applyEffect(target, effect);

                        target.playSound(target.getLocation(), Sound.BLAZE_HIT, 1f, 1.2f);
                    }
                }

                for (int dx = (int) -currentRadius; dx <= currentRadius; dx++) {
                    for (int dz = (int) -currentRadius; dz <= currentRadius; dz++) {
                        double distance = Math.sqrt(dx * dx + dz * dz);

                        if (distance <= currentRadius) {
                            double chance = 0.7 + 0.3 * (1 - distance / currentRadius);

                            for (int yOffset = -3; yOffset <= 3; yOffset++) {
                                Location blockLoc = center.clone().add(dx, yOffset, dz);
                                if (blockLoc.getBlock().getType().isSolid() && random.nextDouble() < chance) {
                                    blockLoc.getBlock().setType(Material.NETHERRACK);
                                }
                            }
                        }
                    }
                }

            }
        }.runTaskTimer(Main.get(), 0L, 1L);

        return true;
    }
}