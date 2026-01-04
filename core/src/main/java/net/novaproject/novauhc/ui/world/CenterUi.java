package net.novaproject.novauhc.ui.world;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.ui.ConfirmMenu;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import net.novaproject.novauhc.world.generation.CenterType;
import net.novaproject.novauhc.world.generation.WorldPopulator;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class CenterUi extends CustomInventory {
    private final String SOON = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE1MmQ1NzlhZmUyZmRmN2I4ZWNmYTc0NmNkMDE2MTUwZDk2YmViNzUwMDliYjI3MzNhZGUxNWQ0ODdjNDJhMSJ9fX0=";
    private final CustomInventory parent;
    public CenterUi(Player player,CustomInventory parent) {
        super(player);
        this.parent = parent;
    }

    @Override
    public void setup() {

        fillCorner(getConfig().getInt("menu.world.color"));
        addCenterType(CenterType.ROOFT,10,new ItemCreator(Material.RED_MUSHROOM).setName("§8┃ §fBiome : "+Common.get().getMainColor()+"Rooft"),Biome.ROOFED_FOREST);
        addCenterType(CenterType.TAIGA,11,new ItemCreator(Material.SNOW_BALL).setName("§8┃ §fBiome : "+Common.get().getMainColor()+"Taiga"),Biome.TAIGA_HILLS);
        addCenterType(CenterType.FOREST,12,new ItemCreator(Material.SAPLING).setName("§8┃ §fBiome : "+Common.get().getMainColor()+"Forest"),Biome.FOREST);
        addCenterType(CenterType.FLAT,13,new ItemCreator(Material.GRASS).setName("§8┃ §fBiome : "+Common.get().getMainColor()+"Flat"),Biome.PLAINS);
        addItem(new StaticItem(14, UHCUtils.createCustomButon(SOON," §8§l┃ §f§lSoon", Arrays.asList("", CommonString.CLICK_HERE_TO_ACCESS.getMessage()))));
        addItem(new StaticItem(15, UHCUtils.createCustomButon(SOON," §8§l┃ §f§lSoon", Arrays.asList("", CommonString.CLICK_HERE_TO_ACCESS.getMessage()))));
        addItem(new StaticItem(16, UHCUtils.createCustomButon(SOON," §8§l┃ §f§lSoon", Arrays.asList("", CommonString.CLICK_HERE_TO_ACCESS.getMessage()))));

        if(parent != null) addReturn(18,parent);

    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.world.color");
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    private void addCenterType(CenterType type, int slot, ItemCreator item, Biome biome){
        addItem(new ActionItem(slot,item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfirmMenu(getPlayer(),"Etes vous sur de votre choix ?",
                        ()-> new WorldPopulator(Common.get().getArena(),type,biome),null,CenterUi.this).open();
            }
        });
    }
}
