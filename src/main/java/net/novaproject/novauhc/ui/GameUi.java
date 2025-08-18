package net.novaproject.novauhc.ui;

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

import java.util.HashMap;
import java.util.Map;

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
        ItemCreator potion = (new ItemCreator(Material.POTION)).setName(CommonString.GAME_POTION_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.GAME_POTION_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_POTION_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        Map<String, Object> pvpPlaceholders = new HashMap<>();
        pvpPlaceholders.put("%time%", UHCUtils.getFormattedTime(pvp_time));
        String pvpTitle = CommonString.GAME_PVP_TITLE.getMessage();
        for (Map.Entry<String, Object> entry : pvpPlaceholders.entrySet()) {
            pvpTitle = pvpTitle.replace(entry.getKey(), entry.getValue().toString());
        }
        ItemCreator pvp = new ItemCreator(Material.DIAMOND_SWORD)
                .setName(pvpTitle)
                .addLore("")
                .addLore(CommonString.GAME_PVP_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_PVP_DESCRIPTION_2.getMessage())
                .addLore(CommonString.GAME_PVP_DESCRIPTION_3.getMessage())
                .addLore("");
        Map<String, Object> borderPlaceholders = new HashMap<>();
        borderPlaceholders.put("%time%", UHCUtils.getFormattedTime(border_time));
        String borderTitle = CommonString.GAME_BORDER_TITLE.getMessage();
        for (Map.Entry<String, Object> entry : borderPlaceholders.entrySet()) {
            borderTitle = borderTitle.replace(entry.getKey(), entry.getValue().toString());
        }
        ItemCreator bordure = new ItemCreator(Material.STAINED_GLASS).setDurability((short) 4)
                .setName(borderTitle)
                .addLore("")
                .addLore(CommonString.GAME_BORDER_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_BORDER_DESCRIPTION_2.getMessage())
                .addLore(CommonString.GAME_BORDER_DESCRIPTION_3.getMessage())
                .addLore(CommonString.GAME_BORDER_DESCRIPTION_4.getMessage())
                .addLore("");
        Map<String, Object> diamondPlaceholders = new HashMap<>();
        diamondPlaceholders.put("%limit%", String.valueOf(limit_diamss));
        String diamondTitle = CommonString.GAME_DIAMOND_TITLE.getMessage();
        for (Map.Entry<String, Object> entry : diamondPlaceholders.entrySet()) {
            diamondTitle = diamondTitle.replace(entry.getKey(), entry.getValue().toString());
        }
        ItemCreator diams = new ItemCreator(Material.DIAMOND)
                .setName(diamondTitle)
                .addLore("")
                .addLore(CommonString.GAME_DIAMOND_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_DIAMOND_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        ItemCreator enchant = (new ItemCreator(Material.ENCHANTED_BOOK)).setName(CommonString.GAME_ENCHANT_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.GAME_ENCHANT_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_ENCHANT_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator verif = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTkzMmI5Yzc0NGIxYTgwYjEzOTQwYjc0NmM4MTFjMTUwN2Y3YWMyMDJhYTI2OGRhMDFiMzU0ZjU0NmJlYWY0NCJ9fX0=",
                        CommonString.GAME_VERIFY_TITLE.getMessage(), null
                )
                .addLore("")
                .addLore(CommonString.GAME_VERIFY_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_VERIFY_DESCRIPTION_2.getMessage())
                .addLore(CommonString.GAME_VERIFY_DESCRIPTION_3.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator def = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmRiYmM1ODM2MDliNWYwMjUzN2NjM2NjMzZkNDBhNjBlMTM2NmEyMjJkYzU0ZjFlNzYxMTAwMGE4OTViMjMzNyJ9fX0=",
                        CommonString.GAME_DEFAULT_TITLE.getMessage(), null)
                .addLore("")
                .addLore(CommonString.GAME_DEFAULT_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_DEFAULT_DESCRIPTION_2.getMessage())
                .addLore(CommonString.GAME_DEFAULT_DESCRIPTION_3.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator death = UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc4NDQ4MTljY2YyMDM1MDQ4M2Y5NDY5YjEwNTA3MmU2ZDQ1MjE0ZDdmMjZjYjg2N2YxODkxMGJjYzFkY2RiIn19fQ==",
                        CommonString.GAME_DEATH_TITLE.getMessage(), null)
                .addLore("")
                .addLore(CommonString.GAME_DEATH_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_DEATH_DESCRIPTION_2.getMessage())
                .addLore(CommonString.GAME_DEATH_DESCRIPTION_3.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");
        ItemCreator drop = (new ItemCreator(Material.APPLE)).setName(CommonString.GAME_DROP_TITLE.getMessage())
                .addLore("")
                .addLore(CommonString.GAME_DROP_DESCRIPTION_1.getMessage())
                .addLore(CommonString.GAME_DROP_DESCRIPTION_2.getMessage())
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_ACCESS.getMessage())
                .addLore("");
        addItem(new ActionItem(22, def) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().closeInventory();
                Bukkit.dispatchCommand(getPlayer(), "h stuff modif");
            }
        });
        addMenu(18, verif, new StuffUi(getPlayer()));
        addItem(new StaticItem(26, death));
        addMenu(29, potion, new PotionUi(getPlayer()));
        addMenu(33, drop, new DropUi(getPlayer()));
        addMenu(15, diams, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, limit_diamss, 0, 0, this) {
            @Override
            public void onChange(int newValue) {
                UHCManager.get().setDimamondLimit(newValue);
            }
        });
        addMenu(10, pvp, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, pvp_time / 60, 1, 60, this) {
            @Override
            public void onChange(int newValue) {
                UHCManager.get().setTimerpvp(newValue * 60);
            }
        });
        addMenu(11, bordure, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, border_time / 60, 60, 120, this) {
            @Override
            public void onChange(int newValue) {
                UHCManager.get().setTimerborder(newValue * 60);
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
