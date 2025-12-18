package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.inGameScenario;
import net.novaproject.novauhc.ui.player.inGameTeamUi;
import net.novaproject.novauhc.ui.world.CenterUi;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;


public class PlayerInteractEvent implements Listener {


    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (UHCManager.get().isLobby()) {
            ItemStack item = event.getItem();
            if (item != null) {
                if ((player.hasPermission("novauhc.host") || player.hasPermission("novauhc.cohost"))) {
                    if (item.isSimilar(Common.get().getConfigItem().getItemstack())) {
                        new DefaultUi(player).open();
                        return;
                    }
                    if (item.isSimilar(Common.get().getReglesItem().getItemstack())) {
                        if (UHCManager.get().getWaitState().equals(UHCManager.WaitState.WAIT_STATE)) {
                            UHCManager.get().setWaitState(UHCManager.WaitState.LOBBY_STATE);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.teleport(Common.get().getLobbySpawn());
                            }
                        } else {
                            UHCManager.get().setWaitState(UHCManager.WaitState.WAIT_STATE);
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                p.teleport(Common.get().getRulesSpawn());
                            }
                        }
                        return;
                    }
                    if(item.getType() == Material.WOOD_DOOR && player.getWorld().equals(Common.get().getArena())){
                        player.getInventory().clear();
                        player.teleport(Common.get().getLobbySpawn());
                        UHCUtils.giveLobbyItems(player);
                    }

                    if(item.isSimilar(Common.get().getChangeSpawn().getItemstack())){
                        new CenterUi(player.getPlayer(),null).open();
                    }
                    if(item.isSimilar(Common.get().getRegenArena().getItemstack())){
                        new WorldGenerator(Main.get(), Common.get().getArenaName());
                        UHCUtils.giveLobbyItems(player);
                    }

                }
                if (item.isSimilar(Common.get().getTeamItem().getItemstack())) {
                    new inGameTeamUi(player).open();
                    return;
                }
                if (item.isSimilar(Common.get().getActiveRole().getItemstack())) {
                    new inGameScenario(player, true).open();
                    return;
                }
                if (item.isSimilar(Common.get().getActiveScenario().getItemstack())) {
                    new inGameScenario(player).open();
                    return;
                }
            }
        }
        ItemStack item = event.getItem();
        if (isDiamondArmor(item)) {

            int currentDiamondPieces = countDiamondArmor(player);

            if (currentDiamondPieces >= UHCManager.get().getDiamondArmor()) {
                event.setCancelled(true);
                player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
                CommonString.EXEDED_LIMITE.send(player);
            }
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onPlayerInteract(player, event);
        });
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (UHCManager.get().isLobby()) {
            event.setCancelled(true);
            return;
        }
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onDrop(event);
        });
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (UHCManager.get().isLobby()) {
            if (!player.hasPermission("novauhc.host") || !player.hasPermission("novauhc.cohost")) {
                event.setCancelled(true);
                player.updateInventory();
            }
            return;
        }

        if (event.isShiftClick()) {
            ItemStack clicked = event.getCurrentItem();
            if (isDiamondArmor(clicked)) {
                if (exceedsDiamondLimit(player, uhcPlayer)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
                    CommonString.EXEDED_LIMITE.send(player);
                    player.updateInventory();
                }
            }
            return;
        }

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack cursor = event.getCursor();
            if (isDiamondArmor(cursor)) {
                ItemStack current = event.getCurrentItem();
                int currentPieces = countDiamondArmor(player);

                if (isDiamondArmor(current)) {
                    currentPieces--;
                }

                if (currentPieces >= uhcPlayer.getDiamondArmor()) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
                    CommonString.EXEDED_LIMITE.send(player);
                    player.updateInventory();
                }
            }
        }
    }

    private boolean isDiamondArmor(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.DIAMOND_HELMET ||
                type == Material.DIAMOND_CHESTPLATE ||
                type == Material.DIAMOND_LEGGINGS ||
                type == Material.DIAMOND_BOOTS;
    }

    private int countDiamondArmor(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (isDiamondArmor(item)) count++;
        }
        return count;
    }

    private boolean exceedsDiamondLimit(Player player, UHCPlayer uhcPlayer) {
        return countDiamondArmor(player) >= uhcPlayer.getDiamondArmor();
    }


    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onConsume(player, item, event);
        });
        GoldenHead goldenHeadScenario = ScenarioManager.get().getScenario(GoldenHead.class);
        if (goldenHeadScenario != null && !goldenHeadScenario.isActive()) {
            if (item.getType() == Material.GOLDEN_APPLE
                    && item.hasItemMeta()
                    && item.getItemMeta().hasDisplayName()
                    && item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Golden Head")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMountHorse(PlayerInteractEntityEvent event) {

        Player player = event.getPlayer();

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onPlayerInteractonEntity(player, event);
        });
    }

}
