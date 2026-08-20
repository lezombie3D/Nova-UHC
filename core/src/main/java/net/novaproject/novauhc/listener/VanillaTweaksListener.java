package net.novaproject.novauhc.listener;

import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class VanillaTweaksListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        double damage = UHCManager.get().getPearlDamage();
        if (damage < 0) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        if (damage > 0) {
            player.damage(damage);
        }
        if (player.isOnline() && !player.isDead() && event.getTo() != null) {
            player.teleport(event.getTo());
        }
    }

    @EventHandler
    public void onAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDeathDropAbilityItems(PlayerDeathEvent event) {
        event.getDrops().removeIf(ItemCreator::isAbilityItem);
    }

    @EventHandler(ignoreCancelled = true)
    public void onContainerStash(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Inventory top = event.getView().getTopInventory();
        if (top == null || top.getType() == InventoryType.CRAFTING
                || top.getType() == InventoryType.PLAYER) return;

        if (event.getRawSlot() < top.getSize() && ItemCreator.isAbilityItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }
        if (event.isShiftClick() && event.getRawSlot() >= top.getSize()
                && ItemCreator.isAbilityItem(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAbilityCraft(CraftItemEvent event) {
        for (ItemStack item : event.getInventory().getMatrix()) {
            if (ItemCreator.isAbilityItem(item)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getFoodLevel() >= player.getFoodLevel()) return;
        double slowdown = UHCManager.get().getHungerSlowdown();
        if (slowdown <= 0) return;
        if (ThreadLocalRandom.current().nextDouble() < slowdown) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAbilityDrop(PlayerDropItemEvent event) {
        if (!ItemCreator.isAbilityItem(event.getItemDrop().getItemStack())) return;
        if (!UHCManager.get().isAbilityItemsBlockDrop()) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onLavaPlace(PlayerBucketEmptyEvent event) {
        if (event.getBucket() != Material.LAVA_BUCKET) return;
        if (!UHCManager.get().isGame()) return;
        if (UHCManager.get().getTimer() >= UHCManager.get().getPvpTimer()) return;
        if (!UHCManager.get().isBlockLavaBeforePvp()) return;

        Player player = event.getPlayer();
        if (RankManager.isStaff(player)) return;

        event.setCancelled(true);
        LangManager.get().send(CoreLang.COMMON_LAVA_BLOCKED_PREPVP, player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVanillaCommand(PlayerCommandPreprocessEvent event) {
        FileConfiguration config = ConfigUtils.getGeneralConfig();
        if (!config.getBoolean("commands.block-vanilla", true)) return;

        Player player = event.getPlayer();
        String label = extractLabel(event.getMessage());
        if (label.isEmpty()) return;
        String base = label.contains(":") ? label.substring(label.indexOf(':') + 1) : label;

        if (RankManager.isStaffOrOp(player) && config.getBoolean("commands.staff-bypass", true)) return;

        if (containsIgnoreCase(config.getStringList("commands.blacklist"), label, base)) {
            blockCommand(event, player);
            return;
        }

        if (containsIgnoreCase(config.getStringList("commands.whitelist"), label, base)) return;

        if (label.contains(":")) {
            blockCommand(event, player);
            return;
        }

        if (CommandManager.get().resolve(label) instanceof PluginCommand) return;

        blockCommand(event, player);
    }

    private void blockCommand(PlayerCommandPreprocessEvent event, Player player) {
        event.setCancelled(true);
        LangManager.get().send(CoreLang.COMMON_COMMAND_BLOCKED, player);
    }

    private String extractLabel(String message) {
        if (message == null || message.length() <= 1) return "";
        String label = message.substring(1).trim();
        int space = label.indexOf(' ');
        if (space != -1) label = label.substring(0, space);
        return label.toLowerCase(Locale.ROOT);
    }

    private boolean containsIgnoreCase(List<String> list, String label, String base) {
        if (list == null || list.isEmpty()) return false;
        for (String entry : list) {
            if (entry == null) continue;
            String e = entry.toLowerCase(Locale.ROOT).replace("/", "");
            if (e.equals(label) || e.equals(base)) return true;
        }
        return false;
    }
}
