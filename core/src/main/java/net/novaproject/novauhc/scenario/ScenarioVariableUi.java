package net.novaproject.novauhc.scenario;

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

public class ScenarioVariableUi extends CustomInventory {

    private final Scenario scenario;
    private final CustomInventory parent;

    public ScenarioVariableUi(Player player, Scenario scenario, CustomInventory parent) {
        super(player);
        this.scenario = scenario;
        this.parent = parent;
    }

    @Override
    public void setup() {
        fillCadre(7);
        int slot = 10;

        for (Field field : scenario.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(ScenarioVariable.class)) continue;

            ScenarioVariable annotation = field.getAnnotation(ScenarioVariable.class);
            field.setAccessible(true);

            try {
                Object rawValue = field.get(scenario);
                Object displayValue = rawValue;

                switch (annotation.type()) {
                    case TIME -> {
                        if (rawValue instanceof Integer i)
                            displayValue = UHCUtils.getFormattedTime(i);
                    }
                    case PERCENTAGE -> {
                        if (rawValue instanceof Double d)
                            displayValue = String.format("%.2f%%", d * 100);
                        else if (rawValue instanceof Integer i)
                            displayValue = i + "%";
                    }
                }

                ItemCreator icon = new ItemCreator(Material.PAPER)
                        .setName("§e" + annotation.name())
                        .addLore("§7" + annotation.description())
                        .addLore("")
                        .addLore("§7Valeur actuelle: §b" + displayValue)
                        .addLore("")
                        .addLore("§a▶ Clic pour changer");

                if (rawValue instanceof Boolean) {
                    addItem(new ActionItem(slot, icon) {
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
                    addItem(new ActionItem(slot, icon) {
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
                            }).setSlot("Nouvelle valeur").open();
                        }
                    });

                } else if (rawValue instanceof Number number) {
                    addMenu(slot, icon,
                            new ConfigVarUi(
                                    getPlayer(),
                                    10, 5, 1,
                                    10, 5, 1,
                                    number,
                                    0,
                                    0,
                                    ScenarioVariableUi.this
                            ) {
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

                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }

                slot++;
                if ((slot + 1) % 9 == 0) slot += 2;
                if (slot >= 44) break;

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        addReturn(49,parent);
    }

    @Override
    public String getTitle() {
        return "§8Config: §6" + scenario.getName();
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
