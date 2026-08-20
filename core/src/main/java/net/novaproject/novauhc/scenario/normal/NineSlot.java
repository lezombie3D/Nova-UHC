package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.lang.ScenarioLang;

public class NineSlot extends Scenario implements Listener {

    @Override
    public Family getFamily() { return Family.RESTRICTIONS; }

    private final Map<UUID, BukkitRunnable> inventoryTasks = new HashMap<>();


    @Override public String getName() { return "NineSlot"; }
    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.NINE_SLOT, player);
    }
    @Override public ItemCreator getItem() { return new ItemCreator(Material.CHEST); }

    @Override
    public void onGameStart() { restrictAllPlayersInventory(); }

    @Override
    public void onStart(Player player) {
        if (!isActive()) return;
        restrictPlayerInventory(player);
        player.sendMessage(t(ScenarioLang.NINESLOT_START, player));
    }

    private void restrictAllPlayersInventory() {
        for (UHCPlayer p : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) restrictPlayerInventory(p.getPlayer());
        Bukkit.broadcastMessage(LangManager.get().get(ScenarioLang.NINESLOT_START_ALL));
    }

    private void restrictPlayerInventory(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable existing = inventoryTasks.get(uuid);
        if (existing != null) existing.cancel();
        moveMainInventoryToHotbar(player);
        BukkitRunnable task = new BukkitRunnable() {
            @Override public void run() {
                if (!player.isOnline() || !isActive()) { cancel(); inventoryTasks.remove(uuid); return; }
                clearMainInventorySlots(player);
            }
        };
        inventoryTasks.put(uuid, task);
        task.runTaskTimer(Main.get(), 0, 5);
    }

    private void moveMainInventoryToHotbar(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 9; i < 36; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            boolean moved = false;
            for (int h = 0; h < 9; h++) {
                if (contents[h] == null || contents[h].getType() == Material.AIR) { contents[h] = item.clone(); contents[i] = null; moved = true; break; }
            }
            if (!moved) { player.getWorld().dropItemNaturally(player.getLocation(), item); contents[i] = null; player.sendMessage(t(ScenarioLang.NINESLOT_ITEM_DROPPED, player)); }
        }
        player.getInventory().setContents(contents);
    }

    private void clearMainInventorySlots(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        boolean found = false;
        for (int i = 9; i < 36; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR) continue;
            found = true;
            boolean moved = false;
            for (int h = 0; h < 9; h++) {
                ItemStack hi = contents[h];
                if (hi == null || hi.getType() == Material.AIR) { contents[h] = item.clone(); contents[i] = null; moved = true; break; }
                else if (hi.getType() == item.getType() && hi.getDurability() == item.getDurability() && hi.getAmount() < hi.getMaxStackSize()) {
                    int space = hi.getMaxStackSize() - hi.getAmount(); int toMove = Math.min(space, item.getAmount());
                    hi.setAmount(hi.getAmount() + toMove); item.setAmount(item.getAmount() - toMove);
                    if (item.getAmount() <= 0) { contents[i] = null; moved = true; break; }
                }
            }
            if (!moved) { player.getWorld().dropItemNaturally(player.getLocation(), item); contents[i] = null; }
        }
        if (found) player.getInventory().setContents(contents);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isActive() || !(event.getWhoClicked() instanceof Player player)) return;
        if (event.getSlotType() == InventoryType.SlotType.CONTAINER && event.getSlot() >= 9 && event.getSlot() <= 35) {
            event.setCancelled(true); player.sendMessage(t(ScenarioLang.NINESLOT_CANNOT_USE_INV, player));
        }
        if (event.isShiftClick() && event.getSlot() < 9) {
            event.setCancelled(true); player.sendMessage(t(ScenarioLang.NINESLOT_CANNOT_MOVE, player));
        }
    }

    @Override
    public void onStop() {
        for (BukkitRunnable task : inventoryTasks.values()) task.cancel();
        inventoryTasks.clear();
    }
}

