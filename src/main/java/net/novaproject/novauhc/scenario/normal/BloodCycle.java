package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BloodCycle extends Scenario {
    private final Material[] cache = new Material[6];
    int i = 0;

    @Override
    public String getName() {
        return "BloodCycle";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BONE);
    }

    @Override
    public void onStart(Player player) {
        cache[0] = Material.DIAMOND_ORE;
        cache[1] = Material.GOLD_ORE;
        cache[2] = Material.IRON_ORE;
        cache[3] = Material.COAL_ORE;
        cache[4] = Material.LAPIS_ORE;
        cache[5] = Material.REDSTONE_ORE;
        Bukkit.broadcastMessage(Common.get().getServertag() + "Blood Cycle = " + cache[i].name());
        new BukkitRunnable() {
            @Override
            public void run() {
                i++;
                if (i == 6)
                    i = 0;
                Bukkit.broadcastMessage(Common.get().getServertag() + "Blood Cycle = " + cache[i].name());

            }
        }.runTaskTimerAsynchronously(Main.get(), 10 * 20 * 60, 10 * 20 * 60);
    }

    @Override
    public void onBreak(Player player, Block block, BlockBreakEvent event) {
        if (block.getType().equals(cache[i])) {
            player.damage(1);
        }
    }
}
