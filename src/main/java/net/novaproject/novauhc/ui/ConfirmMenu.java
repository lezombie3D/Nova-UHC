package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Collections;

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
    public void setup() {
        addItem(new StaticItem(13, new ItemCreator(Material.PAPER)
                .setName(CommonString.CONFIRM_TITLE.getMessage())
                .setLores(Arrays.asList("", "§f" + message, ""))));

        addItem(new ActionItem(11, UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc5YTVjOTVlZTE3YWJmZWY0NWM4ZGMyMjQxODk5NjQ5NDRkNTYwZjE5YTQ0ZjE5ZjhhNDZhZWYzZmVlNDc1NiJ9fX0=", "§8┃ §aMonde", null)
                .setName(CommonString.CONFIRM_BUTTON.getMessage())
                .setLores(Collections.singletonList(CommonString.CONFIRM_BUTTON_LORE.getMessage()))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                onClose();
                if (onConfirm != null) onConfirm.run();
            }
        });

        addItem(new ActionItem(15, UHCUtils.createCustomButon("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NDgzNjJhMjRjMGZhODQ1M2U0ZDkzZTY4YzU5NjlkZGJkZTU3YmY2NjY2YzAzMTljMWVkMWU4NGQ4OTA2NSJ9fX0=", "§8┃ §cAnnuler", null)
                .setLores(Collections.singletonList(CommonString.CANCEL_BUTTON_LORE.getMessage()))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                e.setCancelled(true);
                onClose();
                if (onCancel != null) onCancel.run();
            }
        });
    }

    @Override
    public String getTitle() {
        return "§8┃ §fConfirmation";
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
    public void onClose() {
        super.onClose();
        parent.open();
    }
}
