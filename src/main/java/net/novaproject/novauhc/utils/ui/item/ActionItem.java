package net.novaproject.novauhc.utils.ui.item;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ActionItem {

    private final ItemStack itemStack;
    private final int category;
    private final int slot;
    private final boolean all;
	
	public ActionItem(int slot, ItemStack itemStack){
		this(1, true, slot, itemStack);
	}

	public ActionItem(int slot, ItemCreator itemCreator){
		this(1, true, slot, itemCreator.getItemstack());
	}
	
	public ActionItem(int category, int slot, ItemStack itemStack) {
		this(category, false, slot, itemStack);
	}

	public ActionItem(int category, int slot, ItemCreator itemCreator) {
		this(category, false, slot, itemCreator.getItemstack());
	}

	public ActionItem(int category, boolean all, int slot, ItemCreator itemCreator) {
		this(category, all, slot, itemCreator.getItemstack());
	}
	
	public ActionItem(int category, boolean all, int slot, ItemStack itemStack) {
		this.itemStack = itemStack;
		this.category = category;
		this.all = all;
		this.slot = slot;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public int getCategory() {
		return category;
	}
	
	public int getSlot() {
		return slot;
	}
	
	public boolean isAllCategories() {
		return all;
	}
	
	public abstract void onClick(InventoryClickEvent e);

}