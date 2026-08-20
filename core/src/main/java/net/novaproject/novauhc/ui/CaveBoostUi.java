package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class CaveBoostUi extends CustomInventory {

    private static final int MIN_MULT = 1;
    private static final int MAX_MULT = 5;

    private final UHCManager manager = UHCManager.get();
    private final int previousCave;
    private final int previousRavine;

    public CaveBoostUi(Player player) {
        super(player);
        this.previousCave = manager.getCaveMultiplier();
        this.previousRavine = manager.getRavineMultiplier();
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.cave-ravine-boost.title", "» Caves & Ravines"));
    }

    @Override
    public int getLines() {
        return 1;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.ORANGE, 8);
        UiItems.panes(this, DyeColor.WHITE, 1, 7);

        ItemCreator caves = new ItemCreator(Material.IRON_PICKAXE)
                .setName(t(DynamicLang.of("menu.cave-ravine-boost.caves.name", "§8┃ §aRéseaux de Caves")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.cave-ravine-boost.caves.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fModifiez la densité des réseaux souterrains.\n"
                                + "\n"
                                + " §a☰ §fMultiplicateur : ×%cave_boost%\n"
                                + "\n"
                                + "§8» §fCliquez pour §amodifier§f."))
                        .replace("%cave_boost%", String.valueOf(manager.getCaveMultiplier()))
                        .split("\n", -1)));

        addItem(new ActionItem(2, caves) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 3, 2, 1, 3, 2, 1,
                        manager.getCaveMultiplier(), MIN_MULT, MAX_MULT, CaveBoostUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        manager.setCaveMultiplier(newValue.intValue());
                    }
                }.open();
            }
        });

        ItemCreator ravines = new ItemCreator(Material.STONE)
                .setName(t(DynamicLang.of("menu.cave-ravine-boost.ravines.name", "§8┃ §cRavines & Failles")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.cave-ravine-boost.ravines.lore",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fModifiez la densité des ravines et failles.\n"
                                + "\n"
                                + " §c☰ §fMultiplicateur : ×%ravine_boost%\n"
                                + "\n"
                                + "§8» §fCliquez pour §cmodifier§f."))
                        .replace("%ravine_boost%", String.valueOf(manager.getRavineMultiplier()))
                        .split("\n", -1)));

        addItem(new ActionItem(4, ravines) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 3, 2, 1, 3, 2, 1,
                        manager.getRavineMultiplier(), MIN_MULT, MAX_MULT, CaveBoostUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        manager.setRavineMultiplier(newValue.intValue());
                    }
                }.open();
            }
        });

        ItemCreator reset = new ItemCreator(Material.REDSTONE_TORCH_ON)
                .setName(t(DynamicLang.of("menu.cave-ravine-boost.reset.name", "§8┃ §eRéinitialiser")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.cave-ravine-boost.reset.lore",
                        "§7» Accès §eHost\n"
                                + "\n"
                                + " §e┃ §fRestaurez les valeurs de génération par défaut.\n"
                                + "\n"
                                + "§8» §fCliquez pour §eutiliser§f."))
                        .split("\n", -1)));

        addItem(new ActionItem(6, reset) {
            @Override
            public void onClick(InventoryClickEvent e) {
                manager.setCaveMultiplier(2);
                manager.setRavineMultiplier(2);
                openAll();
            }
        });

        addItem(new ActionItem(0, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.ORANGE),
                t(DynamicLang.of("menu.cave-ravine-boost.back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WorldUi(getPlayer()).open();
            }
        });
    }
}
