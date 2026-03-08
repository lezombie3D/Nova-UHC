package net.novaproject.novauhc.scenario;

import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.LangResolver;
import net.novaproject.novauhc.lang.ui.ScenarioVariableUiLang;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScenarioVariableUi extends CustomInventory {

    private final Scenario scenario;
    private final CustomInventory parent;
    private static final int VARS_PER_PAGE = 28;
    private static final int SLOT_PREV     = 46;
    private static final int SLOT_NEXT     = 52;
    private static final String NEXT     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0=";
    private static final String PREVIOUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjMwYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI4ODFhYWNjNyJ9fX0=";

    public ScenarioVariableUi(Player player, Scenario scenario, CustomInventory parent) {
        super(player);
        this.scenario = scenario;
        this.parent = parent;
    }

    private String t(ScenarioVariableUiLang key) {
        return LangManager.get().get(key, getPlayer());
    }

    private String t(ScenarioVariableUiLang key, Map<String, Object> p) {
        return LangManager.get().get(key, getPlayer(), p);
    }

    @Override
    public void setup() {
        fillCadre(7);
        addReturn(49, parent);

        ItemCreator prevButton = UHCUtils.createCustomButon(PREVIOUS, LangManager.get().get(CommonLang.PAGE_PREVIOUS, getPlayer()), null);
        ItemCreator nextButton = UHCUtils.createCustomButon(NEXT,     LangManager.get().get(CommonLang.PAGE_NEXT,     getPlayer()), null);

        if (getCategories() > 1) {
            for (int page = 1; page <= getCategories(); page++) {
                if (page > 1) {
                    addItem(new ActionItem(page, SLOT_PREV, prevButton) {
                        @Override public void onClick(InventoryClickEvent e) { previousCategory(); refresh(); }
                    });
                }
                if (page < getCategories()) {
                    addItem(new ActionItem(page, SLOT_NEXT, nextButton) {
                        @Override public void onClick(InventoryClickEvent e) { nextCategory(); refresh(); }
                    });
                }
            }
        }

        List<Field> variableFields = getVariableFields();
        for (int i = 0; i < variableFields.size(); i++) {
            Field field   = variableFields.get(i);
            int page      = i / VARS_PER_PAGE + 1;
            int posInPage = i % VARS_PER_PAGE;
            int pageSize  = Math.min(VARS_PER_PAGE, variableFields.size() - (page - 1) * VARS_PER_PAGE);
            int slot      = computeVariableSlots(pageSize)[posInPage];

            ScenarioVariable annotation = field.getAnnotation(ScenarioVariable.class);
            field.setAccessible(true);

            try {
                Object rawValue     = field.get(scenario);
                Object displayValue = rawValue;

                switch (annotation.type()) {
                    case TIME -> {
                        if (rawValue instanceof Integer iv)
                            displayValue = UHCUtils.getFormattedTime(iv);
                    }
                    case PERCENTAGE -> {
                        if (rawValue instanceof Double d)
                            displayValue = String.format("%.2f%%", d * 100);
                        else if (rawValue instanceof Integer iv)
                            displayValue = iv + "%";
                    }
                }

                String varName = LangResolver.resolve(annotation.lang(), annotation.nameKey(), getPlayer());
                String varDesc = LangResolver.resolve(annotation.lang(), annotation.descKey(), getPlayer());

                ItemCreator icon = new ItemCreator(Material.PAPER)
                        .setName("§e" + varName)
                        .addLore("§7" + varDesc)
                        .addLore("")
                        .addLore(t(ScenarioVariableUiLang.CURRENT_VALUE, Map.of("%value%", displayValue)))
                        .addLore("")
                        .addLore(t(ScenarioVariableUiLang.CLICK_CHANGE));

                if (rawValue instanceof Boolean) {
                    addItem(new ActionItem(page, slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            try {
                                field.set(scenario, !(boolean) field.get(scenario));
                                openAll();
                            } catch (IllegalAccessException ex) {
                                ex.printStackTrace();
                            }
                        }
                    });

                } else if (rawValue instanceof String) {
                    addItem(new ActionItem(page, slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            new AnvilUi(getPlayer(), event -> {
                                if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                                    try {
                                        field.set(scenario, event.getName());
                                        openAll();
                                    } catch (IllegalAccessException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }).setSlot(t(ScenarioVariableUiLang.ANVIL_NEW_VALUE)).open();
                        }
                    });

                } else if (rawValue instanceof Number number) {
                    addMenu(page, slot, icon,
                            new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, number, 0, 0, ScenarioVariableUi.this) {
                                @Override
                                public void onChange(Number newValue) {
                                    try {
                                        Class<?> type = field.getType();
                                        if (type == int.class || type == Integer.class)
                                            field.set(scenario, newValue.intValue());
                                        else if (type == double.class || type == Double.class)
                                            field.set(scenario, newValue.doubleValue());
                                        else if (type == float.class || type == Float.class)
                                            field.set(scenario, newValue.floatValue());
                                        else if (type == long.class || type == Long.class)
                                            field.set(scenario, newValue.longValue());
                                    } catch (IllegalAccessException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            });
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Field> getVariableFields() {
        List<Field> fields = new ArrayList<>();
        for (Field field : scenario.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(ScenarioVariable.class))
                fields.add(field);
        }
        return fields;
    }

    private int[] computeVariableSlots(int count) {
        int total = Math.min(count, VARS_PER_PAGE);
        int[] slots = new int[total];
        int idx = 0;
        for (int row = 1; row <= 4 && idx < total; row++) {
            int inRow    = Math.min(7, total - idx);
            int startCol = 1 + (7 - inRow) / 2;
            for (int j = 0; j < inRow; j++) slots[idx++] = row * 9 + startCol + j;
        }
        return slots;
    }

    @Override
    public int getCategories() {
        return Math.max(1, (int) Math.ceil((double) getVariableFields().size() / VARS_PER_PAGE));
    }

    @Override
    public String getTitle() {
        return t(ScenarioVariableUiLang.CONFIG_TITLE, Map.of("%name%", scenario.getName()));
    }

    @Override public int getLines() { return 6; }
    @Override public boolean isRefreshAuto() { return false; }
}