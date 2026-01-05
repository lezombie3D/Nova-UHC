package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ui.config.DropUi;
import net.novaproject.novauhc.ui.config.LimiteStuffUi;
import net.novaproject.novauhc.ui.config.PotionUi;
import net.novaproject.novauhc.ui.config.StuffUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GameUi extends CustomInventory {
    public GameUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.game.color"));
        int limit_diamss = UHCManager.get().getDimamondLimit();
        int border_time = UHCManager.get().getTimerborder();
        int pvp_time = UHCManager.get().getTimerpvp();
        ItemCreator potion = (new ItemCreator(Material.POTION)).setName("§8┃ §fLimite de §9§lpotions")
                .addLore("")
                .addLore("  §8┃ §fVous permet de limiter la")
                .addLore("  §8┃ §ffabrication de certaines potions")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        ItemCreator pvp = new ItemCreator(Material.DIAMOND_SWORD)
                .setName("§8┃ §fBordure (" + Common.get().getMainColor() + UHCUtils.getFormattedTime(pvp_time) + "§f)")
                .addLore("")
                .addLore("  §8┃ §fVous permet de §cmodifier")
                .addLore("  §8┃ §fle temps avant l'§aactivation.")
                .addLore("  §8┃ §fdu §ePvP §fdurant la " + Common.get().getMainColor() + "partie§f.")
                .addLore("");
        ItemCreator bordure = new ItemCreator(Material.STAINED_GLASS).setDurability((short) 4)
                .setName("§8┃ §fPvP (" + Common.get().getMainColor() + UHCUtils.getFormattedTime(border_time) + "§f)")
                .addLore("")
                .addLore("  §8┃ §fVous permet de " + Common.get().getMainColor() + "modifier")
                .addLore("  §8┃ §fle temps avant l'§aactivation")
                .addLore("  §8┃ §fde la reduction de la")
                .addLore("  §8┃ §ebordure §fdurant la partie.")
                .addLore("");
        ItemCreator diams = new ItemCreator(Material.DIAMOND)
                .setName("§8┃ §fLimite de §eDiamant§r (" + Common.get().getMainColor() + limit_diamss + "§f)")
                .addLore("")
                .addLore("  §8┃ §fVous permet de limiter le nombre")
                .addLore("  §8┃ §fde diamants minables.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        ItemCreator enchant = (new ItemCreator(Material.ENCHANTED_BOOK)).setName("§8┃ §fLimite d'§b§lenchantements")
                .addLore("")
                .addLore("  §8┃ §fVous permet de définir")
                .addLore("  §8┃ §fla limite des tous")
                .addLore("  §8┃ §fles enchantements.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator verif = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTkzMmI5Yzc0NGIxYTgwYjEzOTQwYjc0NmM4MTFjMTUwN2Y3YWMyMDJhYTI2OGRhMDFiMzU0ZjU0NmJlYWY0NCJ9fX0=",
                        "§8┃ §fVérifier les inventaire par défaut", null
                )
                .addLore("")
                .addLore("  §8┃ §fVous permet de vérifer")
                .addLore("  §8┃ §fl'inventaire par " + Common.get().getMainColor() + "défaut")
                .addLore("  §8┃ §fdonné en §6§ldébut §fde partie ou a la §8§lmort §fd'un joueur.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator def = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRiYmM1ODM2MDliNWYwMjUzN2NjM2NjMzZkNDBhNjBlMTM2NmEyMjJkYzU0ZjFlNzYxMTAwMGE4OTViMjMzNyJ9fX0=",
                        "§8┃ §fInventaire de"+Common.get().getMainColor()+" départ", null)
                .addLore("")
                .addLore("  §8┃ §fVous permet de définir")
                .addLore("  §8┃ §fl'inventaire par défaut")
                .addLore("  §8┃ §fdonné en début de partie.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator death = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4NDQ4MTljY2YyMDM1MDQ4M2Y5NDY5YjEwNTA3MmU2ZDQ1MjE0ZDdmMjZjYjg2N2YxODkxMGJjYzFkY2RiIn19fQ==",
                        "§8┃ §fInventaire de " + Common.get().getMainColor() + "mort", null)
                .addLore("")
                .addLore("  §8┃ §fVous permet de définir")
                .addLore("  §8┃ §fl'inventaire de " + Common.get().getMainColor() + "mort")
                .addLore("  §8┃ §fdonné lors d'une " + Common.get().getMainColor() + "mort§f.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator drop = (new ItemCreator(Material.APPLE)).setName("§8┃ §fTaux de §7§ldrop")
                .addLore("")
                .addLore("  §8┃ §fVous permet de modifier les")
                .addLore("  §8┃ §ftaux de drop de certains objets.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        addItem(new ActionItem(18, def) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().closeInventory();
                Bukkit.dispatchCommand(getPlayer(), "h stuff start modif ");
            }
        });
        addItem(new ActionItem(26, death) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().closeInventory();
                Bukkit.dispatchCommand(getPlayer(), "h stuff death modif ");
            }
        });

        addMenu(22,verif,new ChooseVerif(getPlayer()));
        addMenu(29, potion, new PotionUi(getPlayer()));
        addMenu(33, drop, new DropUi(getPlayer()));
        addMenu(15, diams, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, UHCManager.get().getDimamondLimit(), 0, 0, this) {

            @Override
            public void onChange(Number newValue) {
                UHCManager.get().setDimamondLimit((int) newValue);
            }
        });
        addMenu(10, pvp, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, UHCManager.get().getTimerborder() / 60, 1, 60, this) {

            @Override
            public void onChange(Number newValue) {
                UHCManager.get().setTimerpvp((int)newValue * 60);
            }
        });
        addMenu(11, bordure, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, UHCManager.get().getTimerpvp() / 60, 60, 120, this) {

            @Override
            public void onChange(Number newValue) {
                UHCManager.get().setTimerborder((int)newValue * 60);
            }
        });
        addMenu(16, enchant, new LimiteStuffUi(getPlayer()));
        addReturn(36, new DefaultUi(getPlayer()));
    }


    @Override
    public String getTitle() {
        return getConfig().getString("menu.game.title");
    }

    @Override
    public int getLines() {
        return 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }


}
