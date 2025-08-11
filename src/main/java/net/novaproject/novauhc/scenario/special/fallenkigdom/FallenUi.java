package net.novaproject.novauhc.scenario.special.fallenkigdom;


import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class FallenUi extends CustomInventory {

    private final FallenKingdom kingdom = FallenKingdom.get();

    public FallenUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillLine(1, 0);
        addReturn(0, new ScenariosUi(getPlayer(), true));
        ItemCreator assaut = new ItemCreator(Material.DIAMOND_SWORD).setName(ChatColor.RED + "Episodes des assauts : " + ChatColor.GOLD + kingdom.getAssaut())
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("");
        addItem(new ActionItem(2, assaut) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick()) {
                    kingdom.setAssaut(kingdom.getAssaut() + 1);
                    openAll();
                }
                if (e.isRightClick()) {
                    if (kingdom.getAssaut() > 0) {
                        kingdom.setAssaut(kingdom.getAssaut() - 1);
                        openAll();
                    }
                }
            }
        });
        ItemCreator nether = new ItemCreator(Material.NETHERRACK).setName(ChatColor.RED + "Episodes des Nether : " + ChatColor.GOLD + kingdom.getNether())
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("");
        addItem(new ActionItem(4, nether) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick()) {
                    kingdom.setNether(kingdom.getNether() + 1);
                    openAll();
                }
                if (e.isRightClick()) {
                    if (kingdom.getNether() > 0) {
                        kingdom.setNether(kingdom.getNether() - 1);
                        openAll();
                    }
                }
            }
        });
        ItemCreator end = new ItemCreator(Material.ENDER_PEARL).setName(ChatColor.RED + "Episodes des End : " + ChatColor.GOLD + kingdom.getEnd())
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("");
        addItem(new ActionItem(6, end) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick()) {
                    kingdom.setEnd(kingdom.getEnd() + 1);
                    openAll();
                }
                if (e.isRightClick()) {
                    if (kingdom.getEnd() > 0) {
                        kingdom.setEnd(kingdom.getEnd() - 1);
                        openAll();
                    }
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return ChatColor.GOLD + "FallenKingdom : Configuration";
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
