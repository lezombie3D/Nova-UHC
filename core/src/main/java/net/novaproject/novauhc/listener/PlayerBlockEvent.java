package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcDiamondMinedEvent;
import net.novaproject.novauhc.event.UhcWorldEvents.UhcDiamondLimitReachedEvent;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.game.SpectatorNotifier;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.config.DropItemRate;
import org.bukkit.Bukkit;
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

public class PlayerBlockEvent implements Listener {

    private static final Map<UUID, Integer> FD_PENDING = new HashMap<>();
    private static final long FD_BATCH_TICKS = 160L;
    private static final int VANILLA_APPLE_PERCENT = 10;

    private void alertFoundDiamond(Player player, UHCPlayer uhcPlayer) {
        if (!UHCManager.get().isAlertFoundDiamond()) return;
        UUID uuid = player.getUniqueId();
        boolean alreadyScheduled = FD_PENDING.containsKey(uuid);
        FD_PENDING.merge(uuid, 1, Integer::sum);
        if (alreadyScheduled) return;
        Bukkit.getScheduler().runTaskLater(Main.get(), () -> {
            Integer count = FD_PENDING.remove(uuid);
            if (count == null) return;
            SpectatorNotifier.notifySpectators(CoreLang.COMMON_FOUND_DIAMOND_ALERT, Map.of(
                    "%player%", player.getName(),
                    "%count%", count,
                    "%total%", uhcPlayer.getMinedDiamond()), player);
        }, FD_BATCH_TICKS);
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().isLobby()){
            event.setCancelled(true);
            return;
        }

        ScenarioManager.get().getActiveScenarios()
                .forEach(scenario -> scenario.onBreak(player, block, event));

        if (event.isCancelled()) return;

        Material type = block.getType();

        int diamondLimit = uhcPlayer.getDiamondLimit() != 0
                ? uhcPlayer.getDiamondLimit()
                : UHCManager.get().getDiamondLimit();

        if (type == Material.DIAMOND_ORE) {
            if (diamondLimit == 0 || uhcPlayer.getDiamondmined() < diamondLimit) {
                uhcPlayer.setMinedDiamond(uhcPlayer.getDiamondmined() + 1);
                Bukkit.getPluginManager().callEvent(
                        new UhcDiamondMinedEvent(uhcPlayer, uhcPlayer.getDiamondmined()));
                if (diamondLimit != 0) {
                    DisplayService.actionBar(player, LangManager.get().get(CoreLang.COMMON_DIAMOND_LIMIT_INCREASED, uhcPlayer.getPlayer()));
                }
                alertFoundDiamond(player, uhcPlayer);
            } else {
                event.setCancelled(true);
                block.setType(Material.AIR);
                Location drop = dropCenter(block);
                drop.getWorld().dropItemNaturally(drop, new ItemStack(Material.GOLD_INGOT, 2));
                Bukkit.getPluginManager().callEvent(
                        new UhcDiamondLimitReachedEvent(uhcPlayer));
                LangManager.get().send(CoreLang.COMMON_DIAMOND_LIMIT_REACHED, player);
                return;
            }
        }

        countMinedBlock(uhcPlayer, type);

        switch (type) {
            case GRAVEL:
                DropItemRate.FLINT.tryDropAt(dropCenter(block));
                break;

            case LEAVES:
            case LEAVES_2:
                block.setType(Material.AIR);
                if (DropItemRate.APPLE.roll(VANILLA_APPLE_PERCENT)) {
                    DropItemRate.APPLE.dropAt(dropCenter(block));
                }
                break;

            default:
                break;
        }
    }

    private void countMinedBlock(UHCPlayer uhcPlayer, Material type) {
        if (uhcPlayer == null) return;
        switch (type) {
            case STONE -> uhcPlayer.setMinedStone(uhcPlayer.getMinedStone() + 1);
            case IRON_ORE -> uhcPlayer.setMinedIron(uhcPlayer.getMinedIron() + 1);
            case GOLD_ORE -> uhcPlayer.setMinedGold(uhcPlayer.getMinedGold() + 1);
            default -> {
            }
        }
    }

    private Location dropCenter(Block block) {
        return block.getLocation().add(0.5, 0.5, 0.5);
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
        if (!DropItemRate.APPLE.roll()) return;
        event.setCancelled(true);
        event.getBlock().setType(Material.AIR);
        DropItemRate.APPLE.dropAt(event.getBlock().getLocation());
    }

}