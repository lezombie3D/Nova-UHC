package net.novaproject.novauhc.scenario.special.taupegun;


import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class TaupeGunUI extends CustomInventory {

    public TaupeGunUI(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        TaupeGun taupeGun = TaupeGun.getInstance();
        fillLine(1, 14);
        ItemCreator taupe = new ItemCreator(Material.BONE).setName(ChatColor.GOLD + "Nombre de Taupe par Team ")
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer").addLore("").addLore(ChatColor.YELLOW + "Nombre :" + ChatColor.AQUA + taupeGun.getMole());
        addMenu(2, taupe, new ConfigVarUi(getPlayer(), 3, 2, 1, 3, 2, 1, taupeGun.getMole(), 1, 6, this) {
            @Override
            public void onChange(int newValue) {
                taupeGun.setMole(newValue);
            }
        });

        ItemCreator team = new ItemCreator(Material.BOOK).setName("Nombre de taupe par equips de taupe")
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer").addLore("").addLore(ChatColor.YELLOW + "Taille :" + ChatColor.AQUA + taupeGun.getMolesize());
        addMenu(4, team, new ConfigVarUi(getPlayer(), 3, 2, 1, 3, 2, 1, taupeGun.getMolesize(), 1, 6, this) {
            @Override
            public void onChange(int newValue) {
                taupeGun.setMolesize(newValue);
            }
        });

    }

    @Override
    public String getTitle() {
        return "taupeGun";
    }

    @Override
    public int getLines() {
        return 1;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
