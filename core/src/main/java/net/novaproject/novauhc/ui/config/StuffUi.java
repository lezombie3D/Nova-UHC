package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class StuffUi extends CustomInventory {
    private final Map<String, ItemStack[]> savedInventory;
    public StuffUi(Player player, Map<String, ItemStack[]> savedInventory) {
        super(player);
        this.savedInventory = savedInventory;
    }

    @Override
    public void setup() {

        addReturn(53, new GameUi(getPlayer()));
        ItemCreator glassPane = new ItemCreator(Material.STAINED_GLASS_PANE).setName(" ").setDurability((short) 7);

        for (int i = 9; i < 18; i++) {
            addItem(new StaticItem(i, glassPane.getItemstack()));
        }

        if (savedInventory.containsKey("inventory")) {
            ItemStack[] contents = savedInventory.get("inventory");

            for (int i = 0; i < 9; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    addItem(new StaticItem(i, contents[i]));
                }
            }

            for (int i = 9; i < contents.length && i + 9 < 45; i++) {
                if (contents[i] != null && contents[i].getType() != Material.AIR) {
                    addItem(new StaticItem(i + 9, contents[i]));
                }
            }
        }

        if (savedInventory.containsKey("armor")) {
            ItemStack[] armor = savedInventory.get("armor");
            for (int i = 0; i < armor.length; i++) {
                if (armor[i] != null && armor[i].getType() != Material.AIR) {
                    addItem(new StaticItem(i + 45, armor[i]));
                }
            }
        }

        for (int i = 49; i < 54; i++) {
            addItem(new StaticItem(i, glassPane.getItemstack()));
        }
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.stuff.title");
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
