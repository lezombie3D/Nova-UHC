package net.novaproject.ultimate.nuzlocke.roles.fire;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class FireWoodPenaltyListener extends Ability implements Mining {

    @Var(name = "Nuzlocke Fire Charcoal Chance", type = VariableType.PERCENTAGE)
    private double charcoalChance = 0.50;

    @Override public String getName() { return "Bois Charbon"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        Material type = event.getBlock().getType();
        if (type != Material.LOG && type != Material.LOG_2) return;
        if (ThreadLocalRandom.current().nextDouble() >= charcoalChance) return;
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        event.getBlock().getWorld().dropItemNaturally(loc, new ItemStack(Material.COAL, 1, (short) 1));
    }
}

