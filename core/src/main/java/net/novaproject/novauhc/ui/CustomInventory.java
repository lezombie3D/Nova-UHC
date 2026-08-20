package net.novaproject.novauhc.ui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import java.util.Map;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;
import java.util.function.ObjIntConsumer;

public abstract class CustomInventory {

    public static Map<UUID, CustomInventory> cache = new ConcurrentHashMap<>();

    private final Player player;
    private final List<StaticItem> staticItems = new ArrayList<>();
    private final List<ActionItem> actionItems = new ArrayList<>();
    private int actual_category = 1;
    private Inventory inventory = null;
    private String renderedTitle = null;
    private BukkitTask task = null;

    private final UHCPlayer uhcPlayer;
    public CustomInventory(Player player){
        this.player = player;
        this.uhcPlayer = UHCPlayerManager.get().getPlayer(player);
    }

    public abstract void setup();

    public abstract String getTitle();

    public abstract int getLines();

    public abstract boolean isRefreshAuto();

    public int getCategories(){
        return 1;
    }

    public Player getPlayer() {
        return player;
    }

    public UHCPlayer getUHCPlayer() {
        return uhcPlayer;
    }

    public String t(Lang key) {
        return LangManager.get().get(key, player);
    }

    public String t(Lang key, Map<String, Object> extra) {
        return LangManager.get().get(key, player, extra);
    }

    public void open(int tick){
        new BukkitRunnable() {
            @Override
            public void run() {
                open();
            }
        }.runTaskLater(Main.get(), tick);
    }

    public void open(){

        staticItems.clear();
        actionItems.clear();

        setup();

        String title = getTitle();
        boolean reuse = inventory != null
                && inventory.getSize() == getLines() * 9
                && Objects.equals(renderedTitle, title)
                && player.getOpenInventory().getTopInventory() == inventory;

        if (reuse) {
            inventory.clear();
            setItems();
            cache.put(player.getUniqueId(), this);
        } else {
            CustomInventory.this.renderedTitle = title;
            CustomInventory.this.inventory = Bukkit.createInventory(null, getLines() * 9, title);
            refresh();
        }

        if (isRefreshAuto() && task == null){
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || !cache.containsKey(player.getUniqueId()) || cache.get(player.getUniqueId()) != CustomInventory.this) {
                        cancel();
                        return;
                    }
                    refreshMenu();
                }
            }.runTaskTimer(Main.get(), 20, 20);
        }
    }

    public String broadcastKey() {
        return getClass().getName();
    }

    public void openAll(){
        List<CustomInventory> customUI = new ArrayList<>(cache.values());
        customUI.forEach(cui -> {
            if(cui.broadcastKey().equals(broadcastKey()))
                cui.open();
        });
    }

    public void refresh(){
        cache.remove(player.getUniqueId());

        if (!player.isOnline()) return;

        inventory.clear();
        setItems();

        player.openInventory(inventory);
        cache.put(player.getUniqueId(), this);
    }

    private void refreshMenu(){
        staticItems.clear();
        actionItems.clear();

        setup();

        inventory.clear();
        setItems();

    }

    public void onClose(){

    }

    public <T> int paginate(List<T> content, int perPage, IntFunction<int[]> slotsFor, ObjIntConsumer<T> place) {
        int pages = MenuGrid.pages(content.size(), perPage);
        int page = Math.min(Math.max(1, actual_category), pages);
        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, content.size());
        int[] slots = slotsFor.apply(end - start);
        for (int i = 0; i < end - start && i < slots.length; i++) {
            place.accept(content.get(start + i), slots[i]);
        }
        return pages;
    }

    public void addItem(StaticItem staticItem){
        staticItems.add(staticItem);
    }

    public void addItem(ActionItem actionItem){
        actionItems.add(actionItem);
    }

    public void addPage(int slot){
        ItemCreator page = new ItemCreator(Material.MAP).setName(ChatColor.YELLOW + "Page: " + ChatColor.LIGHT_PURPLE + actual_category + ChatColor.GOLD + "/" + ChatColor.LIGHT_PURPLE + getCategories());
        page.setLores(Arrays.asList("", ChatColor.GREEN + "Suivant", ChatColor.RED + "Precedent"));
        page.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        addItem(new ActionItem(slot, page.addItemFlags(ItemFlag.HIDE_ATTRIBUTES).getItemstack()){
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isRightClick()) {
                    previousCategory();

                }
                if (e.isLeftClick()) {
                    nextCategory();
                }
                open();
            }
        });
    }

    public void setActual_category(int actual_category) {
        this.actual_category = actual_category;
    }

    public int getActual_category() {
        return actual_category;
    }

    public StaticItem getStaticItem(int slot) {
        StaticItem item = null;
        for (StaticItem staticItem : staticItems) {
            if(staticItem.getCategory() != actual_category && !staticItem.isAllCategories()) continue;

            if(staticItem.getSlot() == slot)
                item = staticItem;

        }
        return item;
    }

    public ActionItem getActionItem(int slot) {
        ActionItem item = null;
        for (ActionItem actionItem : actionItems) {
            if(actionItem.getCategory() != actual_category && !actionItem.isAllCategories()) continue;

            if(actionItem.getSlot() == slot)
                item = actionItem;

        }
        return item;
    }

    public BukkitTask getTask() {
        return task;
    }

    public void nextCategory(){
        if(actual_category != getCategories())
            actual_category++;
    }

    public void previousCategory(){
        if(actual_category != 1)
            actual_category--;
    }

    public void setCategory(int category){
        if(category > getCategories() || category < 1) return;
        actual_category = category;
    }

    public void addMenu(int slot, ItemCreator itemCreator, CustomInventory customInventory){
        addItem(new ActionItem(slot, itemCreator) {
            @Override
            public void onClick(InventoryClickEvent e) {
                customInventory.open();
            }
        });
    }

    public void addMenu(int page, int slot, ItemCreator itemCreator, CustomInventory customInventory){
        addItem(new ActionItem(page, slot, itemCreator) {
            @Override
            public void onClick(InventoryClickEvent e) {
                customInventory.open();
            }
        });
    }

    public void addReturn(int slot, CustomInventory customInventory){
        addMenu(slot, new ItemCreator(Material.ARROW).setName(ChatColor.GRAY + "Retour"), customInventory);
    }

    public void addClose(int slot){
        addClose(slot, new ItemCreator(Material.BARRIER).setName(ChatColor.RED + "Fermer"));
    }

    public void addClose(int slot, ItemCreator itemCreator){
        addItem(new ActionItem(slot, itemCreator) {
            @Override
            public void onClick(InventoryClickEvent e) {
                player.closeInventory();
            }
        });
    }

    public static int nextInnerSlot(int slot) {
        return switch (slot) {
            case 16 -> 19;
            case 25 -> 28;
            case 34 -> 37;
            case 43 -> 46;
            default -> slot + 1;
        };
    }

    public void fillLine(int line, int durability){
        for(int i = (9*(line-1)); i < line*9; i++){
            addItem(new StaticItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setDurability((short) durability).setName(" ")));
        }
    }

    public void fillCorner(int durability) {
        ItemCreator item = new ItemCreator(Material.STAINED_GLASS_PANE).setDurability((short) durability).setName(" ");
        Arrays.asList(0, 1, 7, 8, 9, 17).forEach(i -> {
            if(i < getLines()*9) addItem(new StaticItem(i, item));
        });

        int lastItem = getLines()*9 - 1;
        Arrays.asList(lastItem, lastItem-1, lastItem-7, lastItem-8, lastItem-9, lastItem-17).forEach(i -> {
            if(i < getLines()*9) addItem(new StaticItem(i, item));
        });
    }

    public void fillCadre(int durability){
        fillLine(1, durability); fillLine(getLines(), durability);
        ItemCreator item = new ItemCreator(Material.STAINED_GLASS_PANE).setDurability((short) durability).setName(" ");
        Arrays.asList(9, 17, 18, 26, 27, 35, 36, 44).forEach(i -> {
            if(i < getLines()*9) addItem(new StaticItem(i, item));
        });
    }

    public void fillDesign(int durability){
        ItemCreator item = new ItemCreator(Material.STAINED_GLASS_PANE).setDurability((short) durability).setName(" ");
        Arrays.asList(0, 1, 7, 8, 9, 17, 18, 26, 27, 35, 36, 44, 45, 53, (getLines()-1)*9+1, getLines()*9-2).forEach(i -> {
            if(i < getLines()*9) addItem(new StaticItem(i, item));
        });
    }

    private void setItems(){
        for (StaticItem staticItem : staticItems) {
            if(staticItem.getCategory() != actual_category && !staticItem.isAllCategories()) continue;

            inventory.setItem(staticItem.getSlot(), staticItem.getItemStack());
        }

        for (ActionItem actionItem : actionItems) {
            if(actionItem.getCategory() != actual_category && !actionItem.isAllCategories()) continue;

            inventory.setItem(actionItem.getSlot(), actionItem.getItemStack());
        }

    }

    public static class CustomInventoryEvent implements Listener {

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
}
