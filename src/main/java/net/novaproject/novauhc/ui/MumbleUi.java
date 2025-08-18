package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class MumbleUi extends CustomInventory {
    public MumbleUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillDesign(getConfig().getInt("menu.mumble.corner"));
        addClose(18);
        ItemCreator item = new ItemCreator(Material.JUKEBOX).setName(CommonString.MUMBLE_TITLE.getMessage()).setLores(Collections.singletonList(CommonString.MUMBLE_CLICK_TO_JOIN.getMessage()));
        addItem(new ActionItem(13, item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                Map<String, Object> placeholders = new HashMap<>();
                placeholders.put("%ip%", Common.get().getMbip());
                placeholders.put("%port%", Common.get().getMbport());
                getPlayer().sendMessage(CommonString.MUMBLE_SERVER_INFO.getMessage(getPlayer()));
            }
        });
        Map<String, Object> portPlaceholders = new HashMap<>();
        portPlaceholders.put("%port%", Common.get().getMbport());
        String portLabel = CommonString.MUMBLE_PORT_LABEL.getMessage();
        for (Map.Entry<String, Object> entry : portPlaceholders.entrySet()) {
            portLabel = portLabel.replace(entry.getKey(), entry.getValue().toString());
        }
        addItem(new StaticItem(11, new ItemCreator(Material.PAPER).setName(portLabel)));

        Map<String, Object> ipPlaceholders = new HashMap<>();
        ipPlaceholders.put("%ip%", Common.get().getMbip());
        String ipLabel = CommonString.MUMBLE_IP_LABEL.getMessage();
        for (Map.Entry<String, Object> entry : ipPlaceholders.entrySet()) {
            ipLabel = ipLabel.replace(entry.getKey(), entry.getValue().toString());
        }
        addItem(new StaticItem(15, new ItemCreator(Material.MAP).setName(ipLabel)));
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
