package net.novaproject.ultimate.nuzlocke.roles.dragon;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.ability.AbilityHooks.MobKilling;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class DragonDanceListener extends Ability implements MobKilling, Mining {

    @Var(name = "Nuzlocke Dragon Dance Chance", type = VariableType.PERCENTAGE)
    private double dupChance = 0.5;

    @Override public String getName() { return "Dragon Dance"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onMobDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (getOwner() == null || !killer.equals(getOwner().getPlayer())) return;
        if (ThreadLocalRandom.current().nextDouble() >= dupChance) return;
        for (int i = event.getDrops().size() - 1; i >= 0; i--) {
            event.getDrops().add(event.getDrops().get(i).clone());
        }
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        if (!isOre(event.getBlock().getType())) return;
        if (ThreadLocalRandom.current().nextDouble() >= dupChance) return;
        for (ItemStack drop : event.getBlock().getDrops(event.getPlayer().getItemInHand())) {
            event.getBlock().getWorld().dropItemNaturally(
                    event.getBlock().getLocation().add(0.5, 0.5, 0.5), drop.clone());
        }
    }

    private boolean isOre(Material m) {
        return switch (m) {
            case COAL_ORE, IRON_ORE, GOLD_ORE, DIAMOND_ORE, EMERALD_ORE, LAPIS_ORE,
                 REDSTONE_ORE, GLOWING_REDSTONE_ORE, QUARTZ_ORE -> true;
            default -> false;
        };
    }
}

