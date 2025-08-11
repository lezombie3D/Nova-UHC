package net.novaproject.novauhc.listener.player;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.GoldenHead;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.player.inGameTeamUi;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public class PlayerInteractEvent implements Listener {


    @EventHandler
    public void onPlayerInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (UHCManager.get().isLobby()) {
            ItemStack item = event.getItem();
            if (item != null) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null || !meta.hasDisplayName()) return;

                if (item.getType() == Material.REDSTONE_COMPARATOR &&
                        meta.getDisplayName().equals(ChatColor.YELLOW + "Configurer") &&
                        (player.hasPermission("novauhc.host") || player.hasPermission("novauhc.cohost"))) {
                    new DefaultUi(player).open();
                    return;
                }
                if (item.getType() == Material.BANNER &&
                        meta.getDisplayName().equals(ChatColor.DARK_PURPLE + "Team")) {
                    new inGameTeamUi(player).open();
                    return;
                }
                if (item.getType() == Material.NETHER_STAR && meta.getDisplayName().equals(ChatColor.GOLD + "Salle des règles") && (player.hasPermission("novauhc.host") || player.hasPermission("novauhc.cohost"))) {

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        Location loc = new Location(p.getWorld(), 39, 61, -13);
                        p.teleport(loc);
                    }
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
                player.sendMessage("§cVous ne pouvez pas équiper plus de " + UHCManager.get().getDiamondArmor() + " pièces en diamant !");
            }
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onPlayerInteract(player, event);
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (event.isShiftClick()) {
            ItemStack clicked = event.getCurrentItem();
            if (isDiamondArmor(clicked)) {
                if (exceedsDiamondLimit(player)) {
                    event.setCancelled(true);
                    player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1, 1);
                    player.sendMessage("§cVous ne pouvez pas équiper plus de " + uhcPlayer.getDiamondArmor() + " pièces en diamant !");
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
                    player.sendMessage("§cVous ne pouvez pas équiper plus de " + uhcPlayer.getDiamondArmor() + " pièces en diamant !");
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
        ItemStack[] armor = player.getInventory().getArmorContents();

        for (ItemStack item : armor) {
            if (isDiamondArmor(item)) {
                count++;
            }
        }
        return count;
    }

    private boolean exceedsDiamondLimit(Player player) {
        UHCPlayer p = UHCPlayerManager.get().getPlayer(player);
        return countDiamondArmor(player) >= p.getDiamondArmor();
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
