package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;


public class MumbleUi extends CustomInventory {
    public MumbleUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillDesign(getConfig().getInt("menu.mumble.corner"));
        addClose(18);
        ItemCreator item = new ItemCreator(Material.JUKEBOX).setName(ChatColor.GRAY + "Mumble").setLores(Collections.singletonList(ChatColor.GRAY + "Cliquez pour rejoindre le mumble"));
        addItem(new ActionItem(13, item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().sendMessage(ChatColor.GRAY + "Mumble serveurIp : " + ChatColor.GOLD + Common.get().getMbip() + "\n" + ChatColor.GRAY + "Mumble port : " + ChatColor.GOLD + Common.get().getMbport());
            }
        });
        addItem(new StaticItem(11, new ItemCreator(Material.PAPER).setName("§7Port : " + ChatColor.GOLD + Common.get().getMbport())));
        addItem(new StaticItem(15, new ItemCreator(Material.MAP).setName("§7Ip : " + ChatColor.GOLD + Common.get().getMbip())));
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.mumble.title");
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
