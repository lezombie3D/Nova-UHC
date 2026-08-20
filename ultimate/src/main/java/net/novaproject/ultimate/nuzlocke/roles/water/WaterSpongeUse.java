package net.novaproject.ultimate.nuzlocke.roles.water;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class WaterSpongeUse extends UseAbility {

    @Var(name = "Nuzlocke Water Sponge Cd", type = VariableType.TIME)
    private int cdSeconds = 300;

    @Var(name = "Nuzlocke Water Sponge Radius", type = VariableType.INTEGER)
    private int radius = 4;

    @Var(name = "Nuzlocke Water Sponge Push", type = VariableType.DOUBLE)
    private double pushStrength = 1.6;

    @Var(name = "Nuzlocke Water Sponge Wall Dur", type = VariableType.INTEGER)
    private int wallDurationTicks = 40;

    public WaterSpongeUse() {
        setCooldown(cdSeconds);
        setMaxUse(-1);
    }

    @Override public String getName() { return "Mur d'Éponge"; }
    @Override public Material getMaterial() { return Material.SPONGE; }

    @Override
    public boolean onEnable(Player player) {
        Set<Block> placed = new HashSet<>();
        Block center = player.getLocation().getBlock();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 < (radius - 1) * (radius - 1) || d2 > radius * radius) continue;
                for (int dy = 0; dy <= 2; dy++) {
                    Block b = center.getRelative(dx, dy, dz);
                    if (b.getType() == Material.AIR) {
                        b.setType(Material.WATER);
                        placed.add(b);
                    }
                }
            }
        }
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue;
            double dist = other.getLocation().distance(player.getLocation());
            if (dist > radius + 1) continue;
            Vector push = other.getLocation().toVector().subtract(player.getLocation().toVector())
                    .normalize().multiply(pushStrength);
            push.setY(0.5);
            other.setVelocity(other.getVelocity().add(push));
        }
        for (Block b : placed) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(net.novaproject.novauhc.Main.get(),
                    () -> { if (b.getType() == Material.WATER) b.setType(Material.AIR); },
                    wallDurationTicks);
        }
        return true;
    }
}

