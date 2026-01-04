package net.novaproject.novauhc.utils.ui;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CustomInventory {

    public static Map<UUID, CustomInventory> cache = new ConcurrentHashMap<>();

    private final Player player;
    private final List<StaticItem> staticItems = new ArrayList<>();
    private final List<ActionItem> actionItems = new ArrayList<>();
    private int actual_category = 1;
    private Inventory inventory = null;
    private BukkitTask task = null;

    private final UHCPlayer uhcPlayer;
    public CustomInventory(Player player){
        this.player = player;
        this.uhcPlayer = UHCPlayerManager.get().getPlayer(player);
    }


    public FileConfiguration getConfig() {
        return ConfigUtils.getMenuConfig();
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

        CustomInventory.this.inventory = Bukkit.createInventory(null, getLines() * 9, getTitle());
        refresh();

        if (isRefreshAuto()){
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

    public void openAll(){
        List<CustomInventory> customUI = new ArrayList<>(cache.values());
        customUI.forEach(cui -> {
            if(cui.getTitle().equals(getTitle()))
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

}
