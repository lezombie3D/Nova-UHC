package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.config.DropItemRate;
import net.novaproject.novauhc.utils.Titles;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class PlayerBlockEvent implements Listener {


    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().isLobby()) return;

        ScenarioManager.get().getActiveScenarios()
                .forEach(scenario -> scenario.onBreak(player, block, event));

        Material type = block.getType();

        if (type == Material.DIAMOND_ORE
                && (UHCManager.get().getDimamondLimit() != 0 || uhcPlayer.getDimamondLimit() != 0)) {
            if (uhcPlayer.getDiamondmined() < uhcPlayer.getDimamondLimit()) {
                uhcPlayer.setMinedDiamond(uhcPlayer.getDiamondmined() + 1);
                new Titles().sendActionText(player, CommonString.DIAMOND_LIMIT_INCREASED.getMessage(uhcPlayer.getPlayer()));
            } else {
                event.setCancelled(true);
                block.setType(Material.AIR);
                dropNaturally(block, new ItemStack(Material.GOLD_INGOT, 2));
                CommonString.DIAMOND_LIMIT_REACHED.send(player);
                return;
            }
        }


        Random random = new Random();

        switch (type) {
            case GRAVEL:
                if (DropItemRate.FLINT.getAmount() > 0 &&
                        random.nextInt(100) < DropItemRate.FLINT.getAmount()) {
                    dropNaturally(block, new ItemStack(Material.FLINT));
                }
                break;

            case LEAVES:
            case LEAVES_2:
                handleAppleDrop(player, block, random);
                break;

            default:
                break;
        }
    }

    private void handleAppleDrop(Player player, Block block, Random random) {

        block.setType(Material.AIR);

        int chance = DropItemRate.APPLE.getAmount() > 0
                ? DropItemRate.APPLE.getAmount()
                : 10;

        if (random.nextInt(100) < chance) {
            dropNaturally(block, new ItemStack(Material.APPLE));
        }
    }

    private void dropNaturally(Block block, ItemStack item) {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);
        block.getWorld().dropItemNaturally(loc, item);
    }


    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event){

        if (UHCManager.get().isLobby())
            return;

        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onPlace(player, block, event);
        });
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {

        Block block = event.getBlock();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onBlockIgnite(block, event);
        });
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onFurnaceBurn(event);
        });
    }

    @EventHandler
    public void onLeafDecay(LeavesDecayEvent event) {
        if (DropItemRate.APPLE.getAmount() != 0) {
            int r = new Random().nextInt(100);
            if (r <= DropItemRate.APPLE.getAmount()) {
                event.setCancelled(true);
                event.getBlock().setType(Material.AIR);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.APPLE, 1));
            }
        }
    }

}
