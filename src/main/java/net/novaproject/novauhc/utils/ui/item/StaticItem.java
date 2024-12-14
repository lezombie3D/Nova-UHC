package net.novaproject.novauhc.utils.ui.item;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.inventory.ItemStack;

public class StaticItem {
	
	private ItemStack itemStack;
	private int category, slot;
	private boolean all;
	
	public StaticItem(int slot, ItemStack itemStack){
		this(1, true, slot, itemStack);
	}

	public StaticItem(int slot, ItemCreator itemCreator){
		this(slot, itemCreator.getItemstack());
	}
	
	public StaticItem(int category, int slot, ItemStack itemStack) {
		this(category, false, slot, itemStack);
	}

	public StaticItem(int category, int slot, ItemCreator itemCreator) {
		this(category, slot, itemCreator.getItemstack());
	}

	public StaticItem(int category, boolean all, int slot, ItemCreator itemCreator) {
		this(category, all, slot, itemCreator.getItemstack());
	}
	
	public StaticItem(int category, boolean all, int slot, ItemStack itemStack) {
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

}
