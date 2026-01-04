package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class DropUi extends CustomInventory {
    public DropUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.drop.color"));
        int i = 11;
        for (DropItemRate dropItemRate : DropItemRate.values()) {
            addItem(new ActionItem(i, dropItemRate.getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    dropItemRate.toggleAmount(e.getClick());
                    openAll();
                }
            });
            i++;
        }
        addReturn(22, new GameUi(getPlayer()));
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.drop.title");
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
