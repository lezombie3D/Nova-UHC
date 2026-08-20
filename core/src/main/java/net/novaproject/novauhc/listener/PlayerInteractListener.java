package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.world.ArenaMirrorManager;
import net.novaproject.novauhc.lobby.HotbarManager;
import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.player.utils.InventorySnapshots;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.inGameScenario;
import net.novaproject.novauhc.ui.inGameTeamUi;
import net.novaproject.novauhc.ui.CenterUi;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (UHCManager.get().isLobby()) {
            ItemStack item = event.getItem();
            if (item != null) {
                if (RankManager.isStaff(player)) {
                    if (item.isSimilar(Common.get().getConfigItem().getItemstack())) {
                        new DefaultUi(player).open();
                        return;
                    }
                    if (item.getType() == Material.WOOD_DOOR && ArenaMirrorManager.get().isArenaOrMirror(player.getWorld())) {
                        InventorySnapshots.clearPlayerInventory(player);
                        player.teleport(Common.get().getLobbySpawn());
                        HotbarManager.get().giveHotbar(player);
                    }
                    if (item.isSimilar(Common.get().getChangeSpawn().getItemstack())) {
                        new CenterUi(player.getPlayer(), null).open();
                    }
                    if (item.isSimilar(Common.get().getRegenArena().getItemstack())) {
                        new WorldGenerator(Main.get(), Common.get().getArenaName());
                        HotbarManager.get().giveHotbar(player);
                    }
                }
                if (item.isSimilar(Common.get().getTeamItem().getItemstack())) {
                    new inGameTeamUi(player).open();
                    return;
                }
                if (item.isSimilar(Common.get().getActiveScenario().getItemstack())) {
                    new inGameScenario(player).open();
                    return;
                }
                if (item.isSimilar(Common.get().getQuitItem().getItemstack())) {
                    player.kickPlayer(LangManager.get().get(CoreLang.COMMON_QUIT_GAME, player));
                    return;
                }
            }
        }

        ItemStack item = event.getItem();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (exceedsDiamondLimit(player, uhcPlayer, item)) {
            event.setCancelled(true);
            denyDiamondArmor(player, false);
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

            if (!RankManager.isStaff(player)) {
                event.setCancelled(true);
                player.updateInventory();
            }
            return;
        }

        if (event.isShiftClick()) {
            if (exceedsDiamondLimit(player, uhcPlayer, event.getCurrentItem())) {
                event.setCancelled(true);
                denyDiamondArmor(player, true);
            }
            return;
        }

        if (event.getSlotType() == InventoryType.SlotType.ARMOR
                && exceedsDiamondLimit(player, uhcPlayer, event.getCursor())) {
            event.setCancelled(true);
            denyDiamondArmor(player, true);
        }
    }

    private void denyDiamondArmor(Player player, boolean refreshInventory) {
        player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
        LangManager.get().send(CoreLang.COMMON_EXEDED_LIMITE, player);
        if (refreshInventory) player.updateInventory();
    }

    private boolean exceedsDiamondLimit(Player player, UHCPlayer uhcPlayer, ItemStack item) {
        if (uhcPlayer == null || !ItemCreator.isDiamondArmor(item)) return false;
        int targetSlot = getArmorIndex(item);
        ItemStack[] armor = player.getInventory().getArmorContents();
        int worn = 0;
        for (int i = 0; i < armor.length; i++) {
            if (i == targetSlot) continue;
            if (ItemCreator.isDiamondArmor(armor[i])) worn++;
        }
        return worn + 1 > uhcPlayer.getDiamondArmor();
    }

    private int getArmorIndex(ItemStack item) {
        if (item == null) return -1;
        return switch (item.getType()) {
            case DIAMOND_BOOTS -> 0;
            case DIAMOND_LEGGINGS -> 1;
            case DIAMOND_CHESTPLATE -> 2;
            case DIAMOND_HELMET -> 3;
            default -> -1;
        };
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.GOLDEN_APPLE) {
            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
            if (uhcPlayer != null) uhcPlayer.setGoldenApplesEaten(uhcPlayer.getGoldenApplesEaten() + 1);
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onConsume(player, item, event);
        });

        GoldenHead goldenHeadScenario = ScenarioManager.get().getScenario(GoldenHead.class);
        if (goldenHeadScenario != null && !goldenHeadScenario.isActive()
                && PlayerCraftEvent.isGoldenHead(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMountHorse(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onPlayerInteractEntity(player, event);
        });
    }

}