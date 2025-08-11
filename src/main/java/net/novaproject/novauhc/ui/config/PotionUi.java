package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;


public class PotionUi extends CustomInventory {


    public PotionUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {

        fillCorner(getConfig().getInt("menu.potion.color"));
        int i = 0;
        for (Potions potions : Potions.values()) {
            addItem(new ActionItem(i, potions.getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    potions.toggleEnabled();
                    openAll();
                }
            });
            if (i == 8) {
                i = 11;
            }
            i++;
        }
        addReturn(22, new GameUi(getPlayer()));
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.potion.title");
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }


}
