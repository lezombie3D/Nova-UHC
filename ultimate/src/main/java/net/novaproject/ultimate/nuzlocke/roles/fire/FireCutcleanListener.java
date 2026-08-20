package net.novaproject.ultimate.nuzlocke.roles.fire;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityHooks.Mining;
import net.novaproject.novauhc.ability.AbilityHooks.MobKilling;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FireCutcleanListener extends Ability implements Mining, MobKilling {

    @Override public String getName() { return "Cutclean"; }
    @Override public Material getMaterial() { return null; }
    @Override public boolean onEnable(Player player) { return true; }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (getOwner() == null || !event.getPlayer().equals(getOwner().getPlayer())) return;
        Material type = event.getBlock().getType();
        ItemStack drop = smeltOre(type);
        if (drop == null) return;
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        event.getBlock().getWorld().dropItemNaturally(loc, drop);
        event.getPlayer().giveExp(experienceOre(type));
    }

    @Override
    public void onMobDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        if (getOwner() == null || !event.getEntity().getKiller().equals(getOwner().getPlayer())) return;
        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            ItemStack cooked = cookFood(drops.get(i));
            if (cooked != null) drops.set(i, cooked);
        }
    }

    private ItemStack smeltOre(Material ore) {
        return switch (ore) {
            case IRON_ORE -> new ItemStack(Material.IRON_INGOT, 1);
            case GOLD_ORE -> new ItemStack(Material.GOLD_INGOT, 1);
            default -> null;
        };
    }

    private int experienceOre(Material ore) {
        return switch (ore) {
            case IRON_ORE, GOLD_ORE -> 1;
            default -> 0;
        };
    }

    private ItemStack cookFood(ItemStack raw) {
        if (raw == null) return null;
        Material cooked = switch (raw.getType()) {
            case RAW_BEEF -> Material.COOKED_BEEF;
            case PORK -> Material.GRILLED_PORK;
            case RAW_CHICKEN -> Material.COOKED_CHICKEN;
            case MUTTON -> Material.COOKED_MUTTON;
            case RABBIT -> Material.COOKED_RABBIT;
            case RAW_FISH -> Material.COOKED_FISH;
            default -> null;
        };
        if (cooked == null) return null;
        return new ItemStack(cooked, raw.getAmount());
    }
}

