package net.novaproject.ultimate.nuzlocke.roles.steel;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class SteelDoubleOreListener extends Ability implements Mining {

    @Var(name = "Nuzlocke Steel Double Chance", type = VariableType.PERCENTAGE)
    private double doubleChance = 0.20;

    @Override public String getName() { return "Double Ore"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        if (!isOre(event.getBlock().getType())) return;
        if (ThreadLocalRandom.current().nextDouble() >= doubleChance) return;
        for (ItemStack drop : event.getBlock().getDrops(event.getPlayer().getItemInHand())) {
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation().add(0.5, 0.5, 0.5), drop.clone());
        }
    }

    private boolean isOre(Material m) {
        return switch (m) {
            case COAL_ORE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, EMERALD_ORE, LAPIS_ORE, REDSTONE_ORE, GLOWING_REDSTONE_ORE, QUARTZ_ORE -> true;
            default -> false;
        };
    }
}

