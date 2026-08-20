package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.variable.Variables.VariableFormatter;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Map;

public abstract class ConfigVarUi extends CustomInventory {

    private static final String MINUS_MAX_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGU0YjhiOGQyMzYyYzg2NGUwNjIzMDE0ODdkOTRkMzI3MmE2YjU3MGFmYmY4MGMyYzViMTQ4Yzk1NDU3OWQ0NiJ9fX0=";
    private static final String MINUS_MID_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNmZjkxZGM5OWQ1ODI4MDIzZWVkZjg3Mzc5OWQyNTUzNWRhZGU2NGEyZTE2YTNiNDk4YjQxMTNlYWZkNDk2NiJ9fX0=";
    private static final String MINUS_MIN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQyNGRmYWYxZWUxN2Q3M2VlZWMyNDIyMTU4Y2EzM2FkMTg3ZWU3MjdhYmI3OTZmMjEzMmRlZGZkMDFmYzQ5ZSJ9fX0=";
    private static final String PLUS_MID_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjA1NmJjMTI0NGZjZmY5OTM0NGYxMmFiYTQyYWMyM2ZlZTZlZjZlMzM1MWQyN2QyNzNjMTU3MjUzMWYifX19";
    private static final String PLUS_MAX_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19";

    private final Number max_minus, mid_minus, min_minus;
    private final Number max_plus, mid_plus, min_plus;
    private final double limitMin, limitMax;
    private final CustomInventory parent;
    private final VariableType type;
    private Number change;
    private final Class<?> numberType;

    public ConfigVarUi(Player player,
                       Number max_minus, Number mid_minus, Number min_minus,
                       Number max_plus, Number mid_plus, Number min_plus,
                       Number change, double limitMin, double limitMax,
                       CustomInventory parent) {
        this(player, max_minus, mid_minus, min_minus, max_plus, mid_plus, min_plus,
                change, limitMin, limitMax, parent, VariableType.INTEGER);
    }

    public ConfigVarUi(Player player,
                       Number max_minus, Number mid_minus, Number min_minus,
                       Number max_plus, Number mid_plus, Number min_plus,
                       Number change, double limitMin, double limitMax,
                       CustomInventory parent, VariableType type) {
        super(player);
        this.max_minus = max_minus; this.mid_minus = mid_minus; this.min_minus = min_minus;
        this.max_plus  = max_plus;  this.mid_plus  = mid_plus;  this.min_plus  = min_plus;
        this.change    = change;
        this.limitMin  = limitMin;  this.limitMax  = limitMax;
        this.parent    = parent;
        this.numberType = change.getClass();
        this.type      = type == null ? VariableType.INTEGER : type;
    }

    public abstract void onChange(Number newValue);

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.configuration.title", "» Configuration✓"));
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
        UiItems.panes(this, DyeColor.BLACK, 0, 8);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17);

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.BLACK),
                t(DynamicLang.of("menu.configuration.btn-18.name", "§8┃ §7Retour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (parent != null) parent.open();
            }
        });

        UiItems.panes(this, DyeColor.WHITE, 19, 25);

        step(10, MINUS_MAX_TEXTURE, "menu.configuration.btn-10.name", "§8┃ §c%Minus%10", "%Minus%10", max_minus, false);
        step(11, MINUS_MID_TEXTURE, "menu.configuration.btn-11.name", "§8┃ §6%Minus%5", "%Minus%5", mid_minus, false);
        step(12, MINUS_MIN_TEXTURE, "menu.configuration.btn-12.name", "§8┃ §e%Minus%1", "%Minus%1", min_minus, false);

        Map<String, Object> value = Map.of("%valeur%", VariableFormatter.format(type, change));
        addItem(new ActionItem(13, UiItems.createCustomButon(MenuHeads.QUESTION,
                t(DynamicLang.of("menu.configuration.btn-13.name", "§8┃ §f%valeur%"), value),
                Arrays.asList(t(DynamicLang.of("menu.configuration.btn-13.lore",
                        "§7» Accès §f§lHost\n\n §f§l┃ §fEn cliquant sur cette item, vous pourrez entrée\n"
                                + " §f§l┃ §fvous pourrez entrée la valeur souhaite directement\n\n"
                                + "§8» §fCliquez pour y §f§laccéder§f."), value).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                openDirectInput();
            }
        });

        step(14, MenuHeads.of(MenuHeads.Shape.PLUS, DyeColor.YELLOW), "menu.configuration.btn-14.name", "§8┃ §e%Plus%1", "%Plus%1", min_plus, true);
        step(15, PLUS_MID_TEXTURE, "menu.configuration.btn-15.name", "§8┃ §a%Plus%5", "%Plus%5", mid_plus, true);
        step(16, PLUS_MAX_TEXTURE, "menu.configuration.btn-16.name", "§8┃ §2%Plus%10", "%Plus%10", max_plus, true);

        UiItems.panes(this, DyeColor.BLACK, 26);
    }

    private void step(int slot, String texture, String key, String defaultName,
                      String token, Number value, boolean plus) {
        addItem(new ActionItem(slot, UiItems.createCustomButon(texture,
                t(DynamicLang.of(key, defaultName),
                        Map.of(token, VariableFormatter.formatDelta(type, value, plus))), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                updateValue(plus ? value : -value.doubleValue());
            }
        });
    }

    private void updateValue(Number delta) {
        double result = change.doubleValue() + delta.doubleValue();
        if (!inBounds(result)) return;
        applyResult(result);
    }

    private boolean inBounds(double result) {
        if (!Double.isNaN(limitMin) && result < limitMin) return false;
        return Double.isNaN(limitMax) || !(result > limitMax);
    }

    private void applyResult(double result) {
        if      (numberType == Integer.class) change = (int) Math.round(result);
        else if (numberType == Double.class)  change = result;
        else if (numberType == Float.class)   change = (float) result;
        else if (numberType == Long.class)    change = (long) result;
        else                                  change = result;
        onChange(change);
        open();
        openAll();
    }

    private void openDirectInput() {
        Player p = getPlayer();
        new AnvilUi(p, this, event -> {
            if (event.getSlot() != AnvilUi.AnvilSlot.OUTPUT) return;
            String raw = event.getName();
            if (raw == null) return;
            String clean = ChatColor.stripColor(raw).trim().replace(',', '.');
            double parsed;
            try {
                parsed = Double.parseDouble(clean);
            } catch (NumberFormatException ex) {
                LangManager.get().send(UiLang.CONFIGVAR_DIRECT_INPUT_INVALID, p);
                return;
            }
            if (!inBounds(parsed)) {
                LangManager.get().send(UiLang.CONFIGVAR_DIRECT_INPUT_OUT_OF_BOUNDS, p,
                        Map.of("%min%", boundDisplay(limitMin, false),
                                "%max%", boundDisplay(limitMax, true)));
                return;
            }
            applyResult(parsed);
        }).setSlot(String.valueOf(change)).open();
    }

    private String boundDisplay(double bound, boolean upper) {
        if (Double.isNaN(bound)) return upper ? "§a§l∞" : "§c§l-∞";
        return VariableFormatter.format(type, bound);
    }
}
