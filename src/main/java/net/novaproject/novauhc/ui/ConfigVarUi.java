package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
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

public abstract class ConfigVarUi extends CustomInventory {

    private final int max_minus, mid_minus, min_minus;
    private final int max_plus, mid_plus, min_plus;
    private final int limitMin, limitMax;
    private final CustomInventory parent;
    private int change;

    public ConfigVarUi(Player player, int max_minus, int mid_minus, int min_minus,
                       int max_plus, int mid_plus, int min_plus,
                       int change, int limitMin, int limitMax,
                       CustomInventory parent) {
        super(player);
        this.max_minus = max_minus;
        this.mid_minus = mid_minus;
        this.min_minus = min_minus;
        this.max_plus = max_plus;
        this.mid_plus = mid_plus;
        this.min_plus = min_plus;
        this.change = change;
        this.limitMin = limitMin;
        this.limitMax = limitMax;
        this.parent = parent;
    }

    public abstract void onChange(int newValue);

    private void updateValue(int delta) {
        int newValue = change + delta;
        if (newValue < limitMin || newValue > limitMax) return;
        change = newValue;
        onChange(change);
        refresh();
    }

    private ActionItem plusButton(int slot, int value, String label) {
        return new ActionItem(slot, UHCUtils.greenPlus(Arrays.asList(
                "§8┃ §fAjouter §a" + value,
                "",
                "  §8┃ §fPermet d'augmenter la valeur",
                "  §8┃ §fde §a" + value + "§f.",
                "",
                CommonString.CLICK_HERE_TO_MODIFY.getMessage()
        ), label)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                updateValue(value);
                openAll();
            }
        };
    }

    private ActionItem minusButton(int slot, int value, String label) {
        return new ActionItem(slot, UHCUtils.redMinus(Arrays.asList(
                "§8┃ §fRetirer " + Common.get().getMainColor() + value,
                "",
                "  §8┃ §fPermet de diminuer la valeur",
                "  §8┃ §fde " + Common.get().getMainColor() + value + "§f.",
                "",
                CommonString.CLICK_HERE_TO_MODIFY.getMessage()
        ), label)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                updateValue(-value);
                openAll();
            }
        };
    }

    @Override
    public void setup() {
        addItem(plusButton(10, min_plus, "§a+" + min_plus));
        addItem(plusButton(11, mid_plus, "§a+" + mid_plus));
        addItem(plusButton(12, max_plus, "§a+" + max_plus));
        addItem(new StaticItem(13, new ItemCreator(Material.PAPER)
                .setName("§8┃ §fValeur actuelle : §e" + change)
                .setLores(Arrays.asList(
                        "",
                        "  §8┃ §fAffiche la valeur actuelle",
                        "  §8┃ §fde la configuration.",
                        "",
                        "  §7Limites : " + Common.get().getMainColor() + limitMin + " §7→ §a" + limitMax
                ))));
        if (limitMax == 0) {
            addItem(new StaticItem(13, new ItemCreator(Material.PAPER)
                    .setName("§8┃ §fValeur actuelle : §e" + change)
                    .setLores(Arrays.asList(
                            "",
                            "  §8┃ §fAffiche la valeur actuelle",
                            "  §8┃ §fde la configuration.",
                            "",
                            "  §7Limites : " + Common.get().getMainColor() + limitMin + " §7→ §a§l∞"
                    ))));
        }

        addItem(minusButton(14, max_minus, "§c-" + max_minus));
        addItem(minusButton(15, mid_minus, "§c-" + mid_minus));
        addItem(minusButton(16, min_minus, "§c-" + min_minus));

        addReturn(18, parent);
        fillCorner(0);
    }

    @Override
    public String getTitle() {
        return "§8┃ §fConfiguration";
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
