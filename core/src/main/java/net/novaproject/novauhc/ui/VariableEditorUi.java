package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.template.SwitchAbility;
import net.novaproject.novauhc.ability.template.UseAbility;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.random.RandomGameEvent;
import net.novaproject.novauhc.scenario.random.ScenarioRandomEventsUi.RandomEventConfigUi;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import net.novaproject.novauhc.utils.variable.Variables.VariableFormatter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class VariableEditorUi extends CustomInventory {

    private static final String ROLE_MENU = "configuration-du-role";
    private static final String CAMP_TOKEN = "%couelur_of_camps%";

    private static final int ROW_FROM = 1;
    private static final int ROW_TO = 4;
    private static final int COL_FROM = 1;
    private static final int COL_TO = 7;
    private static final int PER_PAGE = 28;

    private record Skin(String title, DyeColor accent) {
    }

    private record Category(String id, int size) {
    }

    private final Object target;
    private final CustomInventory parent;
    private final String menuId;
    private final String categoryFilter;
    private int pages = 1;

    public VariableEditorUi(Player player, Object target, CustomInventory parent) {
        this(player, target, parent, "config-variable");
    }

    protected VariableEditorUi(Player player, Object target, CustomInventory parent, String menuId) {
        this(player, target, parent, menuId, null);
    }

    protected VariableEditorUi(Player player, Object target, CustomInventory parent, String menuId,
                               String categoryFilter) {
        super(player);
        this.target = target;
        this.parent = parent;
        this.menuId = menuId;
        this.categoryFilter = categoryFilter;
    }

    public static String categoryLabel(String id) {
        String pretty = id.replace('_', ' ').replace('-', ' ');
        return Character.toUpperCase(pretty.charAt(0)) + pretty.substring(1);
    }

    protected boolean includeDescriptor(VariableDescriptor d) {
        return true;
    }

    protected Object getTarget() {
        return target;
    }

    protected String targetName() {
        if (target instanceof Scenario s) return s.getName();
        if (target instanceof Role r) return r.getName();
        if (target instanceof Ability a) return a.getName();
        if (target instanceof RandomGameEvent<?> e) return e.getName();
        return target.getClass().getSimpleName();
    }

    protected Map<String, Object> titlePlaceholders() {
        String name = targetName();
        return Map.of("%name%", name, "%scenario%", name, "%role%", name, "%pouvoir%", name);
    }

    private Skin skin() {
        return switch (menuId) {
            case ROLE_MENU -> new Skin("» Configuration : %role%", DyeColor.YELLOW);
            case "configuration-du-pouvoir" -> new Skin("» Configuration : %pouvoir%", DyeColor.YELLOW);
            case "config-du-scenario" -> new Skin("» Configuration : %scenario%", DyeColor.CYAN);
            case "configuration-de-l-evenement" -> new Skin("» Configuration : %événement%", DyeColor.PURPLE);
            default -> new Skin("» Config : %name%", DyeColor.CYAN);
        };
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + menuId + ".title", skin().title()), titlePlaceholders());
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
        return getClass().getName() + ":" + menuId;
    }

    @Override
    public void setup() {
        Skin skin = skin();
        boolean roleMenu = menuId.equals(ROLE_MENU);

        if (roleMenu) {
            ItemCreator accent = new ItemCreator(Material.STAINED_GLASS_PANE)
                    .setDurability((short) skin.accent().getData())
                    .setName(t(DynamicLang.of("menu." + menuId + ".btn-0.name", CAMP_TOKEN),
                            Map.of(CAMP_TOKEN, campLabel())));
            addItem(new StaticItem(0, accent));
            addItem(new StaticItem(8, accent));
            addItem(new StaticItem(53, accent));
        } else {
            UiItems.panes(this, skin.accent(), 0, 8, 53);
        }
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        List<Object> entries = entries();

        pages = paginate(entries, PER_PAGE,
                n -> MenuGrid.grid(ROW_FROM, ROW_TO, COL_FROM, COL_TO, true, n),
                (entry, slot) -> {
                    ItemCreator icon = new ItemCreator(Material.PAPER);
                    render(icon, entry);
                    addItem(new ActionItem(slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            onEntryClick(e, entry);
                        }
                    });
                });

        if (entries.size() > PER_PAGE) {
            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, skin.accent()),
                    t(DynamicLang.of("menu." + menuId + ".previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });
            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, skin.accent()),
                    t(DynamicLang.of("menu." + menuId + ".next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }

        addBack(skin, roleMenu);
    }

    private void addBack(Skin skin, boolean roleMenu) {
        String key = roleMenu ? "btn-45" : "back";
        ItemCreator icon = UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, skin.accent()),
                t(DynamicLang.of("menu." + menuId + "." + key + ".name", "§8┃ §7§lRetour")), null);
        if (roleMenu) {
            icon.setLores(Arrays.asList(t(DynamicLang.of("menu." + menuId + "." + key + ".lore", CAMP_TOKEN),
                    Map.of(CAMP_TOKEN, campLabel())).split("\n", -1)));
        }
        if (parent == null) {
            addItem(new StaticItem(45, icon));
            return;
        }
        addItem(new ActionItem(45, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                parent.open();
            }
        });
    }

    private List<Object> entries() {
        List<Object> out = new ArrayList<>();
        LinkedHashMap<String, Integer> grouped = new LinkedHashMap<>();

        for (VariableDescriptor d : Variables.of(target)) {
            if (!includeDescriptor(d)) continue;
            String category = d.category();
            if (categoryFilter != null) {
                if (categoryFilter.equals(category)) out.add(d);
                continue;
            }
            if (category.isEmpty()) out.add(d);
            else grouped.merge(category, 1, Integer::sum);
        }

        if (categoryFilter != null) return out;

        for (Map.Entry<String, Integer> entry : grouped.entrySet()) {
            out.add(new Category(entry.getKey(), entry.getValue()));
        }
        if (target instanceof SwitchAbility switchAbility) {
            out.addAll(switchAbility.getChildAbilities());
        }
        return out;
    }

    private String campLabel() {
        if (!(target instanceof Role role)) return "";
        Camps camp = role.getCamp();
        return camp == null ? "" : camp.getColor() + camp.getName();
    }

    protected void render(ItemCreator icon, Object entry) {
        icon.clearLores();

        if (entry instanceof Category category) {
            icon.setMaterial(Material.BOOK)
                    .setName("§6" + t(DynamicLang.of("var.category." + category.id(),
                            categoryLabel(category.id()))))
                    .addLore(t(UiLang.SCENARVAR_CATEGORY_LORE,
                            Map.of("%count%", category.size())))
                    .addLore("")
                    .addLore(t(UiLang.SCENARVAR_CLICK_CHANGE));
            return;
        }

        if (entry instanceof UseAbility child) {
            icon.setMaterial(child.getMaterial())
                    .setName("§b" + child.getName())
                    .setLores(List.of(
                            "§7§oVariante du switch §f" + ((SwitchAbility) target).getSwitchName(),
                            "",
                            t(UiLang.SCENARVAR_CLICK_CHANGE)));
            return;
        }

        VariableDescriptor d = (VariableDescriptor) entry;
        Object rawValue = value(d);
        icon.setMaterial(Material.PAPER);

        if (rawValue instanceof Ability ability) {
            icon.setMaterial(ability.getMaterial() == null ? Material.PAPER : ability.getMaterial())
                    .setName(t(UiLang.SCENARVAR_CONFIG_BUTTON, Map.of("%name%", ability.getName())))
                    .addLore(t(UiLang.SCENARVAR_CONFIG_LORE));
            return;
        }
        if (rawValue instanceof RandomGameEvent<?> gameEvent) {
            icon.setName(t(UiLang.SCENARVAR_CONFIG_BUTTON, Map.of("%name%", gameEvent.getName())))
                    .addLore(t(UiLang.SCENARVAR_CONFIG_LORE));
            return;
        }

        icon.setName("§e" + d.name(getPlayer()));

        if (!isEditable(rawValue)) {
            icon.addLore(t(UiLang.SCENARVAR_NOT_DEFINED));
            return;
        }

        for (String wrapped : TextUtils.wrap(d.desc(getPlayer()), "§7")) {
            icon.addLore(wrapped);
        }
        icon.addLore("")
                .addLore(t(UiLang.SCENARVAR_CURRENT_VALUE, Map.of("%value%", displayValue(d, rawValue))))
                .addLore("")
                .addLore(t(UiLang.SCENARVAR_CLICK_CHANGE));
    }

    protected void onEntryClick(InventoryClickEvent e, Object entry) {
        if (entry instanceof Category category) {
            new CategoryConfigUi(getPlayer(), target, this, menuId, category.id()).open();
            return;
        }

        if (entry instanceof UseAbility child) {
            new NamedConfigUi(getPlayer(), child, this).open();
            return;
        }

        VariableDescriptor d = (VariableDescriptor) entry;
        Field field = d.field();
        Object rawValue = value(d);

        if (rawValue instanceof Ability ability) {
            new NamedConfigUi(getPlayer(), ability, this).open();

        } else if (rawValue instanceof RandomGameEvent<?> gameEvent) {
            new RandomEventConfigUi(getPlayer(), gameEvent, this).open();

        } else if (rawValue instanceof Boolean bool) {
            Variables.write(field, target, !bool);
            onValueChanged();

        } else if (rawValue instanceof Enum<?>) {
            Object[] constants = field.getType().getEnumConstants();
            if (constants == null || constants.length == 0) return;
            int idx = 0;
            for (int j = 0; j < constants.length; j++) {
                if (constants[j] == rawValue) {
                    idx = j;
                    break;
                }
            }
            int next = e.isRightClick()
                    ? (idx - 1 + constants.length) % constants.length
                    : (idx + 1) % constants.length;
            Variables.setEnumValue(field, target, ((Enum<?>) constants[next]).name());
            onValueChanged();

        } else if (rawValue instanceof String) {
            new AnvilUi(getPlayer(), event -> {
                if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                    try {
                        field.set(target, event.getName());
                        onValueChanged();
                    } catch (IllegalAccessException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", ex);
                    }
                }
            }).setSlot(t(UiLang.SCENARVAR_ANVIL_NEW_VALUE)).open();

        } else if (rawValue instanceof Number number) {
            VariableType vtype = d.type();
            int[] tiers = VariableFormatter.defaultTiers(vtype);
            double effMin = Double.isNaN(d.min()) ? VariableFormatter.defaultMin(vtype) : d.min();
            double effMax = Double.isNaN(d.max()) ? VariableFormatter.defaultMax(vtype) : d.max();
            new ConfigVarUi(getPlayer(), tiers[0], tiers[1], tiers[2],
                    tiers[0], tiers[1], tiers[2], number, effMin, effMax,
                    VariableEditorUi.this, vtype) {
                @Override
                public void onChange(Number newValue) {
                    Variables.setNumber(field, target, newValue);
                }
            }.open();
        }
    }

    private boolean isEditable(Object rawValue) {
        return rawValue instanceof Boolean || rawValue instanceof Enum<?>
                || rawValue instanceof String || rawValue instanceof Number;
    }

    private Object value(VariableDescriptor d) {
        try {
            return d.field().get(target);
        } catch (IllegalAccessException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            return null;
        }
    }

    protected void onValueChanged() {
        openAll();
    }

    private String displayValue(VariableDescriptor d, Object rawValue) {
        if (rawValue == null) return t(UiLang.SCENARVAR_NOT_DEFINED);
        switch (d.type()) {
            case TIME -> {
                if (rawValue instanceof Integer i) return TextUtils.getFormattedTime(i);
            }
            case PERCENTAGE -> {
                if (rawValue instanceof Double dv) return String.format("%.2f%%", dv * 100);
                if (rawValue instanceof Integer i) return i + "%";
            }
        }
        return String.valueOf(rawValue);
    }

    public static class NamedConfigUi extends VariableEditorUi {

        public NamedConfigUi(Player player, Object target, CustomInventory parent) {
            super(player, target, parent,
                    target instanceof Role ? "configuration-du-role" : "configuration-du-pouvoir");
        }
    }

    public static class CategoryConfigUi extends VariableEditorUi {

        private final String category;

        public CategoryConfigUi(Player player, Object target, CustomInventory parent, String menuId,
                                String category) {
            super(player, target, parent, menuId, category);
            this.category = category;
        }

        @Override
        public String getTitle() {
            return "» " + t(DynamicLang.of("var.category." + category, categoryLabel(category)));
        }
    }
}
