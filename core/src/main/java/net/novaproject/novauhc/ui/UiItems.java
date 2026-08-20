package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.item.ItemCreator.Heads;
import net.novaproject.novauhc.ui.item.StaticItem;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;

public final class UiItems {

    public static ItemCreator createCustomButon(String texture, String name, List<String> lore) {
        ItemCreator item = new ItemCreator(Heads.createCustomHead(texture));
        item.setName(name);
        if (lore != null) item.setLores(lore);
        return item;
    }

    public static String onOff(Player player, boolean value) {
        return LangManager.get().get(value ? UiLang.MAIN_STATUS_ENABLED : UiLang.MAIN_STATUS_DISABLED, player);
    }

    public static ItemCreator stateDye(boolean enabled) {
        return new ItemCreator(Material.INK_SACK)
                .setDurability((short) (enabled ? DyeColor.LIME : DyeColor.GRAY).getDyeData());
    }

    public static ItemCreator statePane(boolean enabled) {
        return pane(enabled ? DyeColor.LIME : DyeColor.RED);
    }

    public static ItemCreator pane(DyeColor color) {
        return new ItemCreator(Material.STAINED_GLASS_PANE)
                .setDurability((short) color.getData())
                .setName(" ");
    }

    public static void panes(CustomInventory ui, DyeColor color, int... slots) {
        ItemCreator icon = pane(color);
        for (int slot : slots) ui.addItem(new StaticItem(slot, icon));
    }
}
