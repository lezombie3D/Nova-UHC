package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.GameSettings;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CraftItemsUi extends CustomInventory {

    private static final int ROW_FROM = 1;
    private static final int ROW_TO = 4;
    private static final int COL_FROM = 1;
    private static final int COL_TO = 7;
    private static final boolean CENTERED = false;
    private static final int PER_PAGE = 28;

    private enum Tab {
        CRAFT("craft-items"),
        OBTAIN("craft-items-obtenables");

        private final String menuId;

        Tab(String menuId) {
            this.menuId = menuId;
        }
    }

    private enum Filter {
        ALL("§7Tous"),
        ALLOWED("§aAutorisés"),
        BLOCKED("§cBloqués");

        private final String label;

        Filter(String label) {
            this.label = label;
        }

        private Filter next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    private static List<Material> craftables;
    private static List<Material> obtainables;

    private final Tab tab;
    private Filter filter = Filter.ALL;
    private String search = "";
    private int pages = 1;

    public CraftItemsUi(Player player) {
        this(player, Tab.CRAFT);
    }

    private CraftItemsUi(Player player, Tab tab) {
        super(player);
        this.tab = tab;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(key("title"), "» Craft & Items"));
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public int getCategories() {
        return pages;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName() + ":" + tab.menuId;
    }

    @Override
    public void setup() {
        GameSettings settings = UHCManager.get().settings();
        List<Material> content = content(settings);
        boolean craft = tab == Tab.CRAFT;

        UiItems.panes(this, DyeColor.GREEN, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        ItemCreator craftTab = new ItemCreator(Material.WORKBENCH)
                .setName(t(DynamicLang.of(key("tab-craftable.name"),
                        craft ? "§8┃ §cCraftables — actif" : "§8┃ §cCraftables")));
        craftTab.setLores(Arrays.asList(t(DynamicLang.of(key("tab-craftable.lore"),
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fAffichez les objets dont la recette est contrôlée.\n"
                        + (craft ? "" : "\n§8» §fCliquez pour §cutiliser§f."))).split("\n", -1)));
        addItem(new ActionItem(2, craftTab) {
            @Override
            public void onClick(InventoryClickEvent e) {
                switchTab(Tab.CRAFT);
            }
        });

        ItemCreator searchIcon = new ItemCreator(Material.COMPASS)
                .setName(t(DynamicLang.of(key("search.name"), "§8┃ §bRechercher un item")));
        searchIcon.setLores(Arrays.asList(t(DynamicLang.of(key("search.lore"),
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fRecherchez un matériau Minecraft 1.8.9\n"
                        + " §b┃ §fdans l’onglet actuellement affiché.\n"
                        + "\n"
                        + "§8» §fCliquez pour §butiliser§f.")).split("\n", -1)));
        addItem(new ActionItem(4, searchIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        String query = event.getName() == null ? "" : event.getName().trim();
                        search = query.equals("?") ? "" : query.toLowerCase(Locale.ROOT).replace(' ', '_');
                        setActual_category(1);
                    }
                    open();
                }).setSlot("§7Nom du matériau").open();
            }
        });

        ItemCreator obtainTab = new ItemCreator(Material.HOPPER)
                .setName(t(DynamicLang.of(key("tab-obtainable.name"),
                        craft ? "§8┃ §aObtenables" : "§8┃ §aObtenables — actif")));
        obtainTab.setLores(Arrays.asList(t(DynamicLang.of(key("tab-obtainable.lore"),
                "§7» Accès §aHost\n"
                        + "\n"
                        + " §a┃ §fAffichez les objets obtenables hors craft.\n"
                        + (craft ? "\n§8» §fCliquez pour §autiliser§f." : ""))).split("\n", -1)));
        addItem(new ActionItem(6, obtainTab) {
            @Override
            public void onClick(InventoryClickEvent e) {
                switchTab(Tab.OBTAIN);
            }
        });

        String itemName = t(DynamicLang.of(key("item-dummy.name"), "§8┃ §e%item%"));
        String itemLore = t(DynamicLang.of(key("item-dummy.lore"),
                "§7» Élément §edynamique\n"
                        + "\n"
                        + " §e┃ §fLe matériau et le nom reprennent l’item réel.\n"
                        + " §e┃ §fLe même clic bascule son autorisation.\n"
                        + "\n"
                        + " §e☰ §fStatut : %item_status%\n"
                        + "\n"
                        + "§8» §fCliquez pour §eactiver ou désactiver§f."));

        pages = paginate(content, PER_PAGE,
                n -> MenuGrid.grid(ROW_FROM, ROW_TO, COL_FROM, COL_TO, CENTERED, n),
                (material, slot) -> {
                    boolean blocked = isBlocked(settings, material);
                    String status = blocked ? "§cBloqué" : "§aAutorisé";
                    ItemCreator icon = new ItemCreator(material)
                            .setName(itemName.replace("%item%", material.name().replace('_', ' ')))
                            .setGlow(!blocked);
                    icon.setLores(Arrays.asList(itemLore.replace("%item_status%", status).split("\n", -1)));
                    addItem(new ActionItem(slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            if (tab == Tab.CRAFT) settings.toggleCraftBlocked(material);
                            else settings.toggleObtainBlocked(material);
                            openAll();
                        }
                    });
                });
        if (getActual_category() > pages) setActual_category(pages);

        ItemCreator filterIcon = new ItemCreator(Material.ITEM_FRAME)
                .setName(t(DynamicLang.of(key("filter.name"), "§8┃ §dFiltre : %filtre%"))
                        .replace("%filtre%", filter.label));
        filterIcon.setLores(Arrays.asList(t(DynamicLang.of(key("filter.lore"),
                "§7» Accès §dHost\n"
                        + "\n"
                        + " §d┃ §fAffichez tous les items, seulement les autorisés\n"
                        + " §d┃ §fou seulement les éléments bloqués.\n"
                        + "\n"
                        + "§8» §fCliquez pour §dutiliser§f."))
                .replace("%filtre%", filter.label).split("\n", -1)));
        addItem(new ActionItem(49, filterIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                filter = filter.next();
                setActual_category(1);
                open();
            }
        });

        addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.GREEN),
                t(DynamicLang.of(key("previous.name"), "§8┃ §cPrécédent")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                previousCategory();
                open();
            }
        });

        addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.GREEN),
                t(DynamicLang.of(key("next.name"), "§8┃ §aSuivant")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                nextCategory();
                open();
            }
        });

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.GREEN),
                t(DynamicLang.of(key("back.name"), "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new GameUi(getPlayer()).open();
            }
        });
    }

    private String key(String suffix) {
        return "menu." + tab.menuId + "." + suffix;
    }

    private boolean isBlocked(GameSettings settings, Material material) {
        return tab == Tab.CRAFT ? settings.isCraftBlocked(material) : settings.isObtainBlocked(material);
    }

    private void switchTab(Tab target) {
        if (target == tab) {
            open();
            return;
        }
        CraftItemsUi ui = new CraftItemsUi(getPlayer(), target);
        ui.filter = filter;
        ui.search = search;
        ui.open();
    }

    private List<Material> content(GameSettings settings) {
        List<Material> out = new ArrayList<>();
        for (Material material : tab == Tab.CRAFT ? craftables() : obtainables()) {
            if (!search.isEmpty() && !material.name().toLowerCase(Locale.ROOT).contains(search)) continue;
            boolean blocked = isBlocked(settings, material);
            if (filter == Filter.ALLOWED && blocked) continue;
            if (filter == Filter.BLOCKED && !blocked) continue;
            out.add(material);
        }
        return out;
    }

    private static List<Material> craftables() {
        if (craftables == null) {
            EnumSet<Material> results = EnumSet.noneOf(Material.class);
            Iterator<Recipe> iterator = Bukkit.recipeIterator();
            while (iterator.hasNext()) {
                Recipe recipe = iterator.next();
                if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) continue;
                if (recipe.getResult() == null) continue;
                results.add(recipe.getResult().getType());
            }
            results.remove(Material.AIR);
            craftables = sorted(results);
        }
        return craftables;
    }

    private static List<Material> obtainables() {
        if (obtainables == null) {
            EnumSet<Material> all = EnumSet.allOf(Material.class);
            all.remove(Material.AIR);
            obtainables = sorted(all);
        }
        return obtainables;
    }

    private static List<Material> sorted(EnumSet<Material> materials) {
        List<Material> out = new ArrayList<>(materials);
        out.sort(Comparator.comparing(Material::name));
        return out;
    }
}
