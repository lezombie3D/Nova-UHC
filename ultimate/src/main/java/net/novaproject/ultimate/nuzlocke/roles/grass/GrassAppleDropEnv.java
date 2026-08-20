package net.novaproject.ultimate.nuzlocke.roles.grass;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class GrassAppleDropEnv extends Ability implements Mining {

    @Var(name = "Nuzlocke Grass Apple Chance", type = VariableType.PERCENTAGE)
    private double appleChance = 0.05;

    @Override public String getName() { return "Bonus Pomme"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        Material t = event.getBlock().getType();
        if (t != Material.LOG && t != Material.LOG_2 && t != Material.LEAVES && t != Material.LEAVES_2) return;
        if (ThreadLocalRandom.current().nextDouble() < appleChance) {
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation().add(0.5, 0.5, 0.5),
                    new ItemStack(Material.APPLE, 1));
        }
    }
}

