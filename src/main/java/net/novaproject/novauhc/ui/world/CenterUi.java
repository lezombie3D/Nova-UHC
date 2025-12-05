package net.novaproject.novauhc.ui.world;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.ui.ConfirmMenu;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.world.generation.CenterType;
import net.novaproject.novauhc.world.generation.WorldPopulator;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CenterUi extends CustomInventory {

    public CenterUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.world.color"));
        addCenterType(CenterType.ROOFT,0,new ItemCreator(Material.ACACIA_DOOR_ITEM),Biome.ROOFED_FOREST);
        addCenterType(CenterType.TAIGA,1,new ItemCreator(Material.CARROT_ITEM),Biome.TAIGA);
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.world.color");
    }

    @Override
    public int getLines() {
        return 0;
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
                        ()-> new WorldPopulator(Common.get().getArena(),type,biome),null,CenterUi.this);
            }
        });
    }
}
