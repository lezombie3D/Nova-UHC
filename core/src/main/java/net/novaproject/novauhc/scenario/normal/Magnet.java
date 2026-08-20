package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.Map;

public class Magnet extends Scenario {

    @Override
    public Family getFamily() { return Family.MINAGE; }

    private static final double PICKUP_REACH = 1.5D;
    private static final int FRESH_DROP_TICKS = 2;

    @Override
    public String getName() {
        return "Magnet";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.MAGNET, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.IRON_INGOT);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (!isActive()) return;

        Material type = block.getType();
        if (type != Material.GLOWING_REDSTONE_ORE && !MiningScenarios.ORE_TYPES.contains(type)) return;

        Location center = block.getLocation().clone().add(0.5D, 0.5D, 0.5D);
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> collectDrops(player, center), 1L);
    }

    public static void giveOrDrop(Player player, Location center, ItemStack stack) {
        World world = center.getWorld();
        if (world == null) return;

        if (!ScenarioManager.get().isScenarioActive("Magnet") || !player.isOnline()) {
            world.dropItem(center, stack);
            return;
        }

        if (deliver(player, world, center, stack)) {
            world.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.3f, 1.5f);
        }
    }

    private static boolean deliver(Player player, World world, Location center, ItemStack stack) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
        for (ItemStack leftover : leftovers.values()) {
            world.dropItem(center, leftover);
        }
        return leftovers.isEmpty();
    }

    private void collectDrops(Player player, Location center) {
        World world = center.getWorld();
        if (world == null || !player.isOnline()) return;

        boolean collected = false;
        for (Entity entity : world.getNearbyEntities(center, PICKUP_REACH, PICKUP_REACH, PICKUP_REACH)) {
            if (!(entity instanceof Item item) || item.isDead()) continue;
            if (item.getTicksLived() > FRESH_DROP_TICKS) continue;

            ItemStack stack = item.getItemStack();
            item.remove();
            collected |= deliver(player, world, center, stack);
        }

        if (collected) {
            world.playSound(player.getLocation(), Sound.ITEM_PICKUP, 0.3f, 1.5f);
        }
    }
}
