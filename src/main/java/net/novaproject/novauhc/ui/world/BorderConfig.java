package net.novaproject.novauhc.ui.world;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class BorderConfig extends CustomInventory {
    public BorderConfig(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.border.color"));
        double border_size = Common.get().getArena().getWorldBorder().getSize();
        long borderspeed = UHCManager.get().getReducSpeed();
        double targetsize = UHCManager.get().getTargetSize();
        ItemCreator border = new ItemCreator(Material.STAINED_GLASS).setDurability((short) 3).setName("§8┃ §fBordure initiale §8(§c" + border_size + "§8)")
                .addLore("")
                .addLore("  §8┃ §fCliquez ici pour définir la taille")
                .addLore("  §8┃ §fde la bordure initiale de la partie.")
                .addLore("")
                .addLore(CommonString.CLICK_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator border_speed = (new ItemCreator(Material.WATCH)).setName("§8┃ §fVitesse de la bordure §8(§c" + borderspeed + " bloc(s)/s§8)")
                .addLore("")
                .addLore("  §8┃ §fCliquez ici pour définir la vitesse")
                .addLore("  §8┃ §fde réduction de la bordure.")
                .addLore("")
                .addLore(CommonString.CLICK_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator final_size = (new ItemCreator(Material.STAINED_GLASS)).setDurability((short) 14).setName("§8┃ §fBordure finale §8(" + Common.get().getMainColor() + targetsize + "§8)")
                .addLore("")
                .addLore("  §8┃ §fCliquez ici pour définir la taille")
                .addLore("  §8┃ §fde la bordure finale de la partie.")
                .addLore("")
                .addLore(CommonString.CLICK_TO_MODIFY.getMessage())
                .addLore("");
        addMenu(12, border, new ConfigVarUi(getPlayer(), 500, 250, 100, 500, 250, 100, (int) border_size, 250, 4000, this) {
            @Override
            public void onChange(int newValue) {
                Common.get().getArena().getWorldBorder().setSize(newValue);
            }
        });
        addMenu(13, border_speed, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, (int) borderspeed, 1, 15, this) {
            @Override
            public void onChange(int newValue) {
                UHCManager.get().setReducSpeed(newValue);
            }
        });
        addMenu(14, final_size, new ConfigVarUi(getPlayer(), 100, 50, 10, 100, 50, 10, (int) targetsize, 10, 500, this) {

            @Override
            public void onChange(int newValue) {
                UHCManager.get().setTargetSize(newValue);
            }
        });
        addReturn(22, new DefaultUi(getPlayer()));
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.border.title");
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
