package net.novaproject.novauhc.utils.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;


public class CustomInventoryEvent implements Listener {
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent e) {
		if(e.getWhoClicked() == null || e.getInventory() == null ||
				e.getCurrentItem() == null)
			return;

		Player player = (Player) e.getWhoClicked();

		if (CustomInventory.cache.containsKey(player.getUniqueId())) {
			e.setCancelled(true);

			if(e.getSlot() <= -1 || e.getSlot() > 54
					|| e.getClickedInventory() != Bukkit.getPlayer(player.getUniqueId()).getOpenInventory().getTopInventory())
				return;

			CustomInventory inv = CustomInventory.cache.get(player.getUniqueId());
			if (inv.getActionItem(e.getSlot()) != null)
				inv.getActionItem(e.getSlot()).onClick(e);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClose(InventoryCloseEvent e) {
		if(e.getPlayer() == null || e.getInventory() == null)
			return;

		if(CustomInventory.cache.containsKey(e.getPlayer().getUniqueId())) {
			CustomInventory inventory = CustomInventory.cache.remove(e.getPlayer().getUniqueId());
			if(inventory.getTask() != null)
				inventory.getTask().cancel();
			inventory.onClose();
		}
	}
}
