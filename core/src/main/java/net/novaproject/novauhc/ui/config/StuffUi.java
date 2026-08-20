package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.ui.MenuHeads;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class StuffUi extends CustomInventory {

    private static final DyeColor FRAME = DyeColor.PURPLE;
    private static final Material[] ARMOR_MARKERS = {
            Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET
    };
    private static final Color ARMOR_MARKER_COLOR = Color.fromRGB(60, 60, 60);

    private final Map<String, ItemStack[]> savedInventory;
    private final String type;

    public StuffUi(Player player, Map<String, ItemStack[]> savedInventory, String type) {
        super(player);
        this.savedInventory = savedInventory;
        this.type = type;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName() + ":" + type;
    }

    @Override
    public void setup() {
        UiItems.panes(this, FRAME, 9, 10, 11, 12, 13, 14, 15, 16, 17);
        UiItems.panes(this, DyeColor.WHITE, 52);

        placeContents();
        placeArmor();

        addItem(tab(50, "start", UHCManager.get().start, Material.DIAMOND_SWORD, UiLang.STUFF_TAB_START));
        addItem(tab(51, "death", UHCManager.get().death, Material.SKULL_ITEM, UiLang.STUFF_TAB_DEATH));

        addItem(new ActionItem(49, new ItemCreator(Material.ANVIL)
                .setName(t(UiLang.STUFF_EDIT_NAME))
                .addLore(t(UiLang.STUFF_EDIT_DESC))) {
            @Override
            public void onClick(InventoryClickEvent event) {
                getPlayer().closeInventory();
                getPlayer().performCommand("h stuff " + type + " modif");
            }
        });

        addItem(new ActionItem(53, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, FRAME),
                t(UiLang.BACK_BUTTON), null)) {
            @Override
            public void onClick(InventoryClickEvent event) {
                new GameUi(getPlayer()).open();
            }
        });
    }

    private void placeContents() {
        ItemStack[] contents = savedInventory.get("inventory");
        if (contents == null) return;

        for (int i = 0; i < 9 && i < contents.length; i++) {
            if (isEmpty(contents[i])) continue;
            addItem(new StaticItem(i, contents[i]));
        }
        for (int i = 9; i < contents.length && i + 9 < 45; i++) {
            if (isEmpty(contents[i])) continue;
            addItem(new StaticItem(i + 9, contents[i]));
        }
    }

    private void placeArmor() {
        ItemStack[] armor = savedInventory.get("armor");

        for (int i = 0; i < ARMOR_MARKERS.length; i++) {
            ItemStack piece = armor != null && i < armor.length ? armor[i] : null;

            if (isEmpty(piece)) {
                addItem(new StaticItem(45 + i, new ItemCreator(ARMOR_MARKERS[i])
                        .setName(t(UiLang.STUFF_ARMOR_SLOT))
                        .addLore(t(UiLang.STUFF_ARMOR_SLOT_DESC))
                        .setLeatherColor(ARMOR_MARKER_COLOR)));
            } else {
                addItem(new StaticItem(45 + i, piece));
            }
        }
    }

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private ActionItem tab(int slot, String target, Map<String, ItemStack[]> inventory,
                           Material icon, UiLang label) {
        ItemCreator item = new ItemCreator(icon)
                .setName(t(label))
                .setGlow(target.equals(type));

        return new ActionItem(slot, item) {
            @Override
            public void onClick(InventoryClickEvent event) {
                if (target.equals(type)) return;
                new StuffUi(getPlayer(), inventory, target).open();
            }
        };
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(UiLang.TITLE_STUFF_TITLE, getPlayer());
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
