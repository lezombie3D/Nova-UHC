package net.novaproject.ultimate.nuzlocke.roles.bug;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.ProjectileImpacting;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BugCobwebBow extends Ability implements ProjectileImpacting {

    @Var(name = "Nuzlocke Bug Web Chance", type = VariableType.PERCENTAGE)
    private double webChance = 0.75;

    @Var(name = "Nuzlocke Bug Web Radius", type = VariableType.INTEGER)
    private int radius = 1;

    @Var(name = "Nuzlocke Bug Web Dur", type = VariableType.INTEGER)
    private int durationTicks = 100;

    @Override public String getName() { return "Toile d'araignée"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;
        if (getOwner() == null || !shooter.equals(getOwner().getPlayer())) return;

        Block impact = arrow.getLocation().getBlock();
        List<Block> placed = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block b = impact.getRelative(dx, 0, dz);
                if (b.getType() != Material.AIR) continue;
                if (ThreadLocalRandom.current().nextDouble() >= webChance) continue;
                b.setType(Material.WEB);
                placed.add(b);
            }
        }
        for (Block b : placed) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(),
                    () -> { if (b.getType() == Material.WEB) b.setType(Material.AIR); },
                    durationTicks);
        }
    }
}

