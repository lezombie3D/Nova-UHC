package net.novaproject.novauhc.ui.world;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

public class OrePopulatorUi extends CustomInventory {


    UHCManager manager = UHCManager.get();
    private final double previous_Boost_diams = manager.getBoost_Diamond();
    private final double previous_Boost_gold = manager.getBoost_Gold();
    private final double previous_Boost_iron = manager.getBoost_Iron();
    private final double previous_Boost_lapis = manager.getBoost_Lapis();
    private boolean change = false;

    public OrePopulatorUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        change = false;
        fillLine(1, getConfig().getInt("menu.world.oreboost.color"));
        List<String> lore = Arrays.asList("", ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "+ 10% ", ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "- 10% ", "");
        ItemCreator lapis = UHCUtils.createCustomButon(getConfig().getString("menu.world.oreboost.lapis_texture"), "§bBoost : Lapis", lore);
        ItemCreator iron = UHCUtils.createCustomButon(getConfig().getString("menu.world.oreboost.iron_texture"), "§fBoost : Iron", lore);
        ItemCreator gold = UHCUtils.createCustomButon(getConfig().getString("menu.world.oreboost.gold_texture"), "§eBoost : Gold", lore);
        ItemCreator diam = UHCUtils.createCustomButon(getConfig().getString("menu.world.oreboost.diams_texture"), "§3Boost : Diamond", lore);
        addItem(new ActionItem(0, new ItemCreator(Material.ARROW).setName(ChatColor.GRAY + "Retour")
        ) {
            @Override
            public void onClick(InventoryClickEvent e) {
                onClose();
                new WorldUi(getPlayer()).open();
            }
        });
        addItem(new ActionItem(3, diam.addLore(ChatColor.GRAY + "Boost actuel : " + UHCUtils.calculPourcentage(manager.getBoost_Diamond(), 2.3))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick()) {
                    manager.setBoost_Diamond(manager.getBoost_Diamond() + 0.23);
                    openAll();
                } else {
                    manager.setBoost_Diamond(manager.getBoost_Diamond() - 0.23);
                    openAll();
                }
                if (manager.getBoost_Diamond() != previous_Boost_diams) {
                    change = true;
                }
            }
        });
        addItem(new ActionItem(4, gold.addLore(ChatColor.GRAY + "Boost actuel : " + UHCUtils.calculPourcentage(manager.getBoost_Gold(), 2.55))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick()) {
                    manager.setBoost_Gold(manager.getBoost_Gold() + 0.255);
                    openAll();
                } else {
                    manager.setBoost_Gold(manager.getBoost_Gold() - 0.255);
                    openAll();
                }
                if (manager.getBoost_Gold() != previous_Boost_gold) {
                    change = true;
                }
            }
        });
        addItem(new ActionItem(5, iron.addLore(ChatColor.GRAY + "Boost actuel : " + UHCUtils.calculPourcentage(manager.getBoost_Iron(), 2))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick()) {
                    manager.setBoost_Iron(manager.getBoost_Iron() + 0.2);
                    openAll();
                } else {
                    manager.setBoost_Iron(manager.getBoost_Iron() - 0.2);
                    openAll();
                }
                if (manager.getBoost_Iron() != previous_Boost_iron) {
                    change = true;
                }
            }
        });
        addItem(new ActionItem(6, lapis.addLore(ChatColor.GRAY + "Boost actuel : " + UHCUtils.calculPourcentage(manager.getBoost_Lapis(), 2.22))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick()) {
                    manager.setBoost_Lapis(manager.getBoost_Lapis() + 0.222);
                    openAll();
                } else {
                    manager.setBoost_Lapis(manager.getBoost_Lapis() - 0.222);
                    openAll();
                }
                if (manager.getBoost_Lapis() != previous_Boost_lapis) {
                    change = true;
                }
            }
        });
        addItem(new ActionItem(8, UHCUtils.getReset()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                manager.setBoost_Diamond(previous_Boost_diams);
                manager.setBoost_Gold(previous_Boost_gold);
                manager.setBoost_Iron(previous_Boost_iron);
                manager.setBoost_Lapis(previous_Boost_lapis);
                change = false;
                openAll();
            }
        });
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.world.oreboost.title");
    }

    @Override
    public int getLines() {
        return 1;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void onClose() {
        if (change) {
            getPlayer().sendMessage(getConfig().getString("message.oreboost.message"));
        }
        super.onClose();
    }
}
