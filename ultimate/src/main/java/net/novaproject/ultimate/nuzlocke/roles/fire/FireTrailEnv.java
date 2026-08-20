package net.novaproject.ultimate.nuzlocke.roles.fire;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.template.PassiveAbility;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FireTrailEnv extends PassiveAbility {

    @Var(name = "Nuzlocke Fire Trail Ticks", type = VariableType.INTEGER)
    private int fireTicks = 40;

    @Override public String getName() { return "Sillage de Feu"; }
    @Override public Material getMaterial() { return null; }

    @Override
    public boolean onEnable(Player player) {
        if (getOwner() == null) return false;
        Player owner = getOwner().getPlayer();
        if (owner == null) return false;
        if (owner.getFireTicks() <= 0) return false;

        Block below = owner.getLocation().getBlock().getRelative(0, -1, 0);
        if (below.getType() != Material.GRASS) return false;

        Block at = owner.getLocation().getBlock();
        if (at.getType() == Material.AIR) {
            at.setType(Material.FIRE);
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                    net.novaproject.novauhc.Main.get(),
                    () -> { if (at.getType() == Material.FIRE) at.setType(Material.AIR); },
                    fireTicks
            );
        }
        return true;
    }
}

