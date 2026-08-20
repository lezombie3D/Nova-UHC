package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ConfirmMenu extends CustomInventory {

    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final String message;
    CustomInventory parent;

    public ConfirmMenu(Player player, String message, Runnable onConfirm, Runnable onCancel, CustomInventory parent) {
        super(player);
        this.message = message;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.parent = parent;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.validation.title", "» Validation✓"));
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.LIME, 0, 1, 2, 9, 11, 18, 19, 20);
        UiItems.panes(this, DyeColor.RED, 6, 7, 8, 15, 17, 24, 25, 26);
        UiItems.panes(this, DyeColor.WHITE, 3, 4, 5, 12, 14, 21, 22, 23);

        addItem(new StaticItem(13, UiItems.createCustomButon(MenuHeads.QUESTION,
                t(DynamicLang.of("menu.validation.btn-13.name", "§8┃ §f%question%"))
                        .replace("%question%", message), null)));

        addItem(new ActionItem(10, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.ACCEPT, DyeColor.LIME),
                t(DynamicLang.of("menu.validation.btn-10.name", "§8┃ §aAccepter")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                onClose();
                if (onConfirm != null) onConfirm.run();
            }
        });

        addItem(new ActionItem(16, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.DENY, DyeColor.RED),
                t(DynamicLang.of("menu.validation.btn-16.name", "§8┃ §cRefusé")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                onClose();
                if (onCancel != null) onCancel.run();
            }
        });
    }

    @Override
    public void onClose() {
        super.onClose();
        if (parent != null) parent.open();
    }
}
