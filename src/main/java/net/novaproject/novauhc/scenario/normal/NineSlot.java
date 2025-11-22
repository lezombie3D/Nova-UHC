package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NineSlot extends Scenario {

    private final Map<UUID, BukkitRunnable> inventoryTasks = new HashMap<>();

    @Override
    public String getName() {
        return "NineSlot";
    }

    @Override
    public String getDescription() {
        return "Seule la hotbar peut être utilisée. L'inventaire principal est inaccessible !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CHEST);
    }


    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            restrictAllPlayersInventory();
        } else {
            unrestrictAllPlayersInventory();
        }
    }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;

        restrictPlayerInventory(player);
        player.sendMessage("§8[NineSlot] §fVotre inventaire est maintenant limité à la hotbar !");
    }

    private void restrictAllPlayersInventory() {
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            restrictPlayerInventory(uhcPlayer.getPlayer());
        }

        Bukkit.broadcastMessage("§8[NineSlot] §fTous les inventaires sont maintenant limités à 9 slots !");
    }

    private void unrestrictAllPlayersInventory() {
        for (UUID playerUuid : inventoryTasks.keySet()) {
            BukkitRunnable task = inventoryTasks.get(playerUuid);
            if (task != null) {
                task.cancel();
            }
        }
        inventoryTasks.clear();

        Bukkit.broadcastMessage("§8[NineSlot] §fLes inventaires sont maintenant libres !");
    }

    private void restrictPlayerInventory(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Cancel existing task if any
        BukkitRunnable existingTask = inventoryTasks.get(playerUuid);
        if (existingTask != null) {
            existingTask.cancel();
        }

        // Clear main inventory (slots 9-35) and move items to hotbar or drop them
        moveMainInventoryToHotbar(player);

        // Start monitoring task
        BukkitRunnable monitorTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isActive()) {
                    cancel();
                    inventoryTasks.remove(playerUuid);
                    return;
                }

                // Check and clear main inventory slots
                clearMainInventorySlots(player);
            }
        };

        inventoryTasks.put(playerUuid, monitorTask);
        monitorTask.runTaskTimer(Main.get(), 0, 5); // Check every 5 ticks (0.25 seconds)
    }

    private void moveMainInventoryToHotbar(Player player) {
        ItemStack[] contents = player.getInventory().getContents();

        // Try to move items from main inventory (slots 9-35) to hotbar (slots 0-8)
        for (int i = 9; i < 36; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                // Try to find empty hotbar slot
                boolean moved = false;
                for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
                    if (contents[hotbarSlot] == null || contents[hotbarSlot].getType() == Material.AIR) {
                        contents[hotbarSlot] = item.clone();
                        contents[i] = null;
                        moved = true;
                        break;
                    }
                }

                // If couldn't move to hotbar, drop the item
                if (!moved) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                    contents[i] = null;
                    player.sendMessage("§8[NineSlot] §fObjet jeté : inventaire plein !");
                }
            }
        }

        player.getInventory().setContents(contents);
    }

    private void clearMainInventorySlots(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        boolean foundItems = false;

        // Check main inventory slots (9-35)
        for (int i = 9; i < 36; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                foundItems = true;

                // Try to move to hotbar first
                boolean moved = false;
                for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
                    ItemStack hotbarItem = contents[hotbarSlot];
                    if (hotbarItem == null || hotbarItem.getType() == Material.AIR) {
                        contents[hotbarSlot] = item.clone();
                        contents[i] = null;
                        moved = true;
                        break;
                    } else if (hotbarItem.getType() == item.getType() &&
                            hotbarItem.getDurability() == item.getDurability() &&
                            hotbarItem.getAmount() < hotbarItem.getMaxStackSize()) {
                        // Try to stack with existing item
                        int spaceLeft = hotbarItem.getMaxStackSize() - hotbarItem.getAmount();
                        int toMove = Math.min(spaceLeft, item.getAmount());

                        hotbarItem.setAmount(hotbarItem.getAmount() + toMove);
                        item.setAmount(item.getAmount() - toMove);

                        if (item.getAmount() <= 0) {
                            contents[i] = null;
                            moved = true;
                            break;
                        }
                    }
                }

                // If couldn't move to hotbar, drop the item
                if (!moved) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                    contents[i] = null;
                }
            }
        }

        if (foundItems) {
            player.getInventory().setContents(contents);
        }
    }

    // Handle inventory click events
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isActive()) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Block access to main inventory slots
        if (event.getSlotType() == InventoryType.SlotType.CONTAINER &&
                event.getSlot() >= 9 && event.getSlot() <= 35) {
            event.setCancelled(true);
            player.sendMessage("§8[NineSlot] §cVous ne pouvez pas utiliser l'inventaire principal !");
        }

        // Also block shift-clicking into main inventory
        if (event.isShiftClick() && event.getSlot() < 9) {
            event.setCancelled(true);
            player.sendMessage("§8[NineSlot] §cVous ne pouvez pas déplacer d'objets vers l'inventaire principal !");
        }
    }

    @Override
    public void onDrop(PlayerDropItemEvent event) {
        if (!isActive()) return;

        // Allow dropping items normally
        Player player = event.getPlayer();
        player.sendMessage("§8[NineSlot] §fObjet jeté pour faire de la place !");
    }

    // Clean up when player disconnects
    public void onPlayerDisconnect(Player player) {
        UUID playerUuid = player.getUniqueId();
        BukkitRunnable task = inventoryTasks.get(playerUuid);
        if (task != null) {
            task.cancel();
            inventoryTasks.remove(playerUuid);
        }
    }

    // Get available hotbar slots for a player
    public int getAvailableHotbarSlots(Player player) {
        int available = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                available++;
            }
        }
        return available;
    }

    // Check if player can pick up an item
    public boolean canPlayerPickupItem(Player player, ItemStack item) {
        // Check if there's space in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack hotbarItem = player.getInventory().getItem(i);
            if (hotbarItem == null || hotbarItem.getType() == Material.AIR) {
                return true;
            } else if (hotbarItem.getType() == item.getType() &&
                    hotbarItem.getDurability() == item.getDurability() &&
                    hotbarItem.getAmount() < hotbarItem.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    // Force clear a player's main inventory (admin command)
    public void forceClearMainInventory(Player player) {
        if (isActive()) {
            for (int i = 9; i < 36; i++) {
                player.getInventory().setItem(i, null);
            }
            player.sendMessage("§8[NineSlot] §fInventaire principal vidé par un administrateur !");
        }
    }

    // Get hotbar contents as string (for debugging)
    public String getHotbarContents(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append("§8[NineSlot] Hotbar de ").append(player.getName()).append(": ");

        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                sb.append(item.getType().name()).append("x").append(item.getAmount()).append(" ");
            } else {
                sb.append("VIDE ");
            }
        }

        return sb.toString();
    }
}
