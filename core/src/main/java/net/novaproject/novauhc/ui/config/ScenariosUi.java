package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.Scenario.Family;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.MenuGrid;
import net.novaproject.novauhc.ui.MenuHeads;
import net.novaproject.novauhc.ui.MenuHeads.Shape;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ScenariosUi extends CustomInventory {

    private static final String SCENARIOS_DUMMY_LORE = "§7» Élément §fdynamique\n"
            + "\n"
            + " §f┃ §fLe matériau, le nom et la description\n"
            + " §f┃ §freprennent le scénario réel.\n"
            + "\n"
            + "§8» §fCliquez pour §factiver ou désactiver§f.";

    private static final String MODE_DUMMY_LORE = "§7» Élément §cdynamique\n"
            + "\n"
            + " §c┃ §fLe matériau, le nom et la description\n"
            + " §c┃ §freprennent le mode réel.\n"
            + "\n"
            + " §c┃ §fClic droit : ouvre sa configuration.\n"
            + "\n"
            + "§8» §fCliquez pour §cactiver ou désactiver§f.";

    private static final int PER_PAGE = 28;

    private static final Map<UUID, Family> FILTER = new HashMap<>();
    private static final Set<UUID> ACTIFS_ONLY = new HashSet<>();

    private final boolean special;
    private int pages = 1;

    public ScenariosUi(Player player, boolean special) {
        super(player);
        this.special = special;
    }

    public ScenariosUi(Player player) {
        this(player, false);
    }

    private String menuId() {
        return special ? "mode-de-jeu" : "scenarios";
    }

    @Override
    public String broadcastKey() {
        return getClass().getName() + ":" + menuId();
    }

    @Override
    public String getTitle() {
        return special
                ? t(DynamicLang.of("menu.mode-de-jeu.title", "» Mode de Jeu✓"))
                : t(DynamicLang.of("menu.scenarios.title", "» Scénarios✓"));
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
    public void setup() {
        String id = menuId();
        List<Scenario> scenarios = getFilteredScenarios();

        DyeColor frame = special ? DyeColor.RED : DyeColor.PURPLE;

        UiItems.panes(this, frame, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        addItem(new ActionItem(45, UiItems.createCustomButon(
                MenuHeads.of(Shape.BACK, frame),
                t(DynamicLang.of("menu." + id + ".btn-45.name",
                        special ? "§8┃ §7Retour" : "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new DefaultUi(getPlayer()).open();
            }
        });

        addItem(new StaticItem(4, UiItems.createCustomButon(
                special ? MenuHeads.GAMEMODE : MenuHeads.HEADER_SCENARIOS,
                t(DynamicLang.of("menu." + id + ".btn-4.name",
                        special ? "§8┃ §cMode de jeu" : "§8┃ §5Scénarios")), null)));

        pages = paginate(scenarios, PER_PAGE,
                n -> MenuGrid.grid(1, 4, 1, 7, true, n),
                (scenario, slot) -> addItem(new ActionItem(slot, icon(scenario, id)) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        onScenarioClick(e, scenario);
                    }
                }));

        if (scenarios.size() > PER_PAGE) {
            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(Shape.LEFT, frame),
                    t(DynamicLang.of("menu." + id + ".btn-47.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });
            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(Shape.RIGHT, frame),
                    t(DynamicLang.of("menu." + id + ".btn-51.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }

        if (!special) {
            Family current = FILTER.get(getPlayer().getUniqueId());
            String label = current == null
                    ? t(DynamicLang.of("menu.scenarios-familles.toutes-court", "Toutes"))
                    : t(DynamicLang.of(familyKey(current), current.getLabel()));
            addItem(new ActionItem(49, new ItemCreator(current == null ? Material.HOPPER : current.getIcon())
                    .setDurability((short) (current == null ? 0 : current.getData()))
                    .setName(t(DynamicLang.of("menu." + id + ".btn-49.name", "§8┃ §dFamille §8: §f%famille%"))
                            .replace("%famille%", label))
                    .setLores(Arrays.asList(t(DynamicLang.of("menu." + id + ".btn-49.lore",
                            "§7» §fFiltre §dpar famille\n"
                                    + "\n"
                                    + " §d┃ §f%actifs% §7scénario(s) affiché(s)\n"
                                    + "\n"
                                    + "§8» §fCliquez pour §dchanger de famille§f."))
                            .replace("%actifs%", String.valueOf(scenarios.size()))
                            .split("\n", -1)))) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new FamilyPickerUi(getPlayer()).open();
                }
            });

            boolean actifsOnly = ACTIFS_ONLY.contains(getPlayer().getUniqueId());
            addItem(new ActionItem(50, new ItemCreator(actifsOnly ? Material.REDSTONE_TORCH_ON : Material.TORCH)
                    .setName(t(DynamicLang.of("menu." + id + ".btn-50.name",
                            actifsOnly ? "§8┃ §aActifs uniquement" : "§8┃ §7Tous les statuts")))
                    .setLores(Arrays.asList(t(DynamicLang.of("menu." + id + ".btn-50.lore",
                            "§7» §fFiltre §apar statut\n"
                                    + "\n"
                                    + " §a┃ §f%actifs% §7scénario(s) activé(s)\n"
                                    + "\n"
                                    + "§8» §fCliquez pour §abasculer§f."))
                            .replace("%actifs%", String.valueOf(countActive()))
                            .split("\n", -1)))
                    .setGlow(actifsOnly)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    UUID uuid = getPlayer().getUniqueId();
                    if (!ACTIFS_ONLY.remove(uuid)) ACTIFS_ONLY.add(uuid);
                    setCategory(0);
                    open();
                }
            });
        }
    }

    private static int countActive() {
        int total = 0;
        for (Scenario s : ScenarioManager.get().getScenarios()) {
            if (!s.isSpecial() && s.isActive()) total++;
        }
        return total;
    }

    private ItemCreator icon(Scenario scenario, String id) {
        ItemCreator icon = new ItemCreator(Material.PAPER)
                .setName(t(DynamicLang.of("menu." + id + ".scenario-dummy.name",
                        special ? "§8┃ §fNom du mode : statut" : "§8┃ §fNom du scénario : statut")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu." + id + ".scenario-dummy.lore",
                        special ? MODE_DUMMY_LORE : SCENARIOS_DUMMY_LORE)).split("\n", -1)));

        ItemStack real = scenario.getItem().getItemstack();
        icon.setMaterial(real.getType());
        icon.setDurability(real.getDurability());

        String status = scenario.isActive()
                ? t(UiLang.SCENARIOS_SCENARIO_ACTIVE)
                : t(UiLang.SCENARIOS_SCENARIO_DISABLED);

        icon.setName("§8┃ §f" + scenario.getName() + ": " + status);
        icon.clearLores();
        icon.addLore("");
        for (String wrapped : TextUtils.wrap(scenario.getDescription(getPlayer()), "  §8┃ §f")) {
            icon.addLore(wrapped);
        }
        icon.addLore("");
        if (scenario.touchesGeneration()) {
            icon.addLore(t(DynamicLang.of("menu." + id + ".scenario-generation.lore",
                    "  §e┃ §6Nécessite une régénération de l'arène")));
            icon.addLore("");
        }
        icon.addLore(LangManager.get().get(CoreLang.COMMON_CLICK_HERE_TO_TOGGLE, getPlayer()));
        if (scenario.isSpecial()) icon.addLore(t(UiLang.SCENARIOS_OPEN_CONFIG_LORE));
        icon.addLore("");
        icon.setAmount(1);
        icon.setGlow(scenario.isActive());
        return icon;
    }

    private void onScenarioClick(InventoryClickEvent e, Scenario scenario) {
        CustomInventory menu = scenario.getMenu(getPlayer());
        if (menu != null && scenario.isActive() && e.isRightClick()) {
            menu.open();
            return;
        }
        scenario.toggleActive();
        openAll();
        if (menu != null && scenario.isActive()) menu.open();
    }

    private List<Scenario> getFilteredScenarios() {
        Family family = special ? null : FILTER.get(getPlayer().getUniqueId());
        boolean actifsOnly = !special && ACTIFS_ONLY.contains(getPlayer().getUniqueId());
        List<Scenario> list = new ArrayList<>();
        for (Scenario s : ScenarioManager.get().getScenarios()) {
            if (s.isSpecial() != special) continue;
            if (family != null && s.getFamily() != family) continue;
            if (actifsOnly && !s.isActive()) continue;
            list.add(s);
        }
        return list;
    }

    private static String familyKey(Family family) {
        return "scenario.family." + family.name().toLowerCase();
    }

    private static int count(Family family) {
        int total = 0;
        for (Scenario s : ScenarioManager.get().getScenarios()) {
            if (s.isSpecial()) continue;
            if (family == null || s.getFamily() == family) total++;
        }
        return total;
    }

    public static class FamilyPickerUi extends CustomInventory {

        private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23};

        public FamilyPickerUi(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.scenarios-familles.title", "» Familles✓"));
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
            UiItems.panes(this, DyeColor.PURPLE, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26);

            addItem(new ActionItem(SLOTS[0], new ItemCreator(Material.HOPPER)
                    .setName(t(DynamicLang.of("menu.scenarios-familles.toutes", "§8┃ §fToutes")))
                    .setAmount(amount(null))) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    select(null);
                }
            });

            Family[] families = Family.values();
            for (int i = 0; i < families.length; i++) {
                Family family = families[i];
                addItem(new ActionItem(SLOTS[i + 1], new ItemCreator(family.getIcon())
                        .setDurability(family.getData())
                        .setName("§8┃ §d" + t(DynamicLang.of(familyKey(family), family.getLabel())))
                        .setAmount(amount(family))) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        select(family);
                    }
                });
            }

            addItem(new ActionItem(25, UiItems.createCustomButon(
                    MenuHeads.of(Shape.BACK, DyeColor.PURPLE),
                    t(DynamicLang.of("menu.scenarios-familles.btn-25.name", "§8┃ §7§lRetour")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ScenariosUi(getPlayer()).open();
                }
            });
        }

        private int amount(Family family) {
            return Math.max(1, Math.min(64, count(family)));
        }

        private void select(Family family) {
            if (family == null) FILTER.remove(getPlayer().getUniqueId());
            else FILTER.put(getPlayer().getUniqueId(), family);
            ScenariosUi ui = new ScenariosUi(getPlayer());
            ui.setCategory(0);
            ui.open();
        }
    }
}
