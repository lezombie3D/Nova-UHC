package net.novaproject.novauhc.scenario.random;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.MenuGrid;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.VariableEditorUi;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class ScenarioRandomEventsUi extends CustomInventory {

    private static final String MENU_ID = "evenements-aleatoires";

    private static final String EVENT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWQ3OGNjMzkxYWZmYjgwYjJiMzVlYjczNjRmZjc2MmQzODQyNGMwN2U3MjRiOTkzOTZkZWU5MjFmYmJjOWNmIn19fQ==";
    private static final String BACK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWM4ZGU1ZjQyZGJjMTllYWRmMWU3MTNkODFkNTQ4OTliMzc5Y2Y2NTNhYjNkYzQxMzAzZmJmZGJmNzYzOTgifX19";

    private static final int ROW_FROM = 1;
    private static final int ROW_TO = 4;
    private static final int COL_FROM = 1;
    private static final int COL_TO = 7;
    private static final boolean CENTERED = true;
    private static final int PER_PAGE = 28;

    private final Scenario scenario;
    private final CustomInventory parent;
    private int pages = 1;

    public ScenarioRandomEventsUi(Player player, Scenario scenario, CustomInventory parent) {
        super(player);
        this.scenario = scenario;
        this.parent = parent;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + MENU_ID + ".title", "» Evènement Aléatoire"));
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
        UiItems.panes(this, DyeColor.PURPLE, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        addItem(new StaticItem(4, new ItemCreator(Material.NAME_TAG)
                .setName(t(DynamicLang.of("menu." + MENU_ID + ".btn-4.name", "§8┃ §5Evenement Aléatoires")))));

        pages = paginate(events(), PER_PAGE,
                n -> MenuGrid.grid(ROW_FROM, ROW_TO, COL_FROM, COL_TO, CENTERED, n),
                (event, slot) -> {
                    ItemCreator icon = eventIcon();
                    render(icon, event);
                    addItem(new ActionItem(slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            onEventClick(e, event);
                        }
                    });
                });

        ItemCreator back = UiItems.createCustomButon(BACK_TEXTURE,
                t(DynamicLang.of("menu." + MENU_ID + ".btn-45.name", "§8┃ §7§lRetour")), null);
        addItem(new ActionItem(45, back) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (parent == null) getPlayer().closeInventory();
                else parent.open();
            }
        });
    }

    private ItemCreator eventIcon() {
        ItemCreator icon = UiItems.createCustomButon(EVENT_TEXTURE,
                t(DynamicLang.of("menu." + MENU_ID + ".btn-10.name", "§8┃ §6%event_aleatoire%")), null);
        icon.setLores(Arrays.asList(t(DynamicLang.of("menu." + MENU_ID + ".btn-10.lore",
                "§7» Accès §6Host\n"
                        + "\n"
                        + " §6┃ §f%desc%\n"
                        + "\n"
                        + " §6☰ §fStatus : %event_actif%\n"
                        + " §6☰ §f%info% : %info%\n"
                        + "\n"
                        + "§8» §fClique Droit pour §cDésactiver§f\n"
                        + "§8» §fClique Droit pour §6Configurer§f")).split("\n", -1)));
        return icon;
    }

    private List<RandomGameEvent<?>> events() {
        List<RandomGameEvent<?>> out = new ArrayList<>();
        for (VariableDescriptor d : Variables.of(scenario)) {
            if (d.type() != VariableType.RANDOM_EVENT) continue;
            try {
                if (d.field().get(scenario) instanceof RandomGameEvent<?> event) out.add(event);
            } catch (IllegalAccessException e) {
                Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", e);
            }
        }
        return out;
    }

    private void render(ItemCreator icon, RandomGameEvent<?> event) {
        String name = icon.getName();
        if (name != null) icon.setName(name.replace("%event_aleatoire%", event.getName()));

        List<String> source = icon.getLores();
        if (source == null) source = List.of();

        List<String> lore = new ArrayList<>();
        for (String line : source) {
            List<String> lines = expand(line, "%event_actif%", t(event.isEnabled()
                    ? UiLang.RANDOMEVENTS_STATUS_ON : UiLang.RANDOMEVENTS_STATUS_OFF));
            lines = expandAll(lines, "%desc%", describe(event));
            lines = expandAll(lines, "%info%", varName(event, "repeating"));
            lines = expandAll(lines, "%info%", t(event.isRepeating()
                    ? UiLang.RANDOMEVENTS_YES : UiLang.RANDOMEVENTS_NO));
            lore.addAll(lines);
        }
        icon.setLores(lore);
        if (event.isEnabled()) icon.setGlow(true);
    }

    private void onEventClick(InventoryClickEvent e, RandomGameEvent<?> event) {
        if (e.isRightClick()) {
            new RandomEventConfigUi(getPlayer(), event, this).open();
            return;
        }
        event.setEnabled(!event.isEnabled());
        openAll();
    }

    private String describe(RandomGameEvent<?> event) {
        return t(UiLang.RANDOMEVENTS_LORE_CHANCE, Map.of("%chance%",
                        String.format(Locale.US, "%.0f%%", event.getChance() * 100)))
                + "\n"
                + t(UiLang.RANDOMEVENTS_LORE_WINDOW, Map.of(
                        "%min%", fmtTime(event.getMinGameTime()),
                        "%max%", fmtTime(event.getMaxGameTime())));
    }

    private String varName(RandomGameEvent<?> event, String fieldName) {
        for (VariableDescriptor d : Variables.of(event)) {
            if (d.field().getName().equals(fieldName)) return d.name(getPlayer());
        }
        return fieldName;
    }

    private List<String> expandAll(List<String> lines, String token, String value) {
        List<String> out = new ArrayList<>();
        for (String line : lines) out.addAll(expand(line, token, value));
        return out;
    }

    private List<String> expand(String line, String token, String value) {
        int index = line.indexOf(token);
        if (index < 0) return List.of(line);
        String prefix = line.substring(0, index);
        String suffix = line.substring(index + token.length());
        String[] parts = value.split("\n", -1);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            out.add(prefix + parts[i] + (i == parts.length - 1 ? suffix : ""));
        }
        return out;
    }

    private String fmtTime(int seconds) {
        int m = seconds / 60;
        int s = seconds % 60;
        if (m == 0) return s + "s";
        if (s == 0) return m + "m";
        return m + "m" + s;
    }

    public static class RandomEventConfigUi extends VariableEditorUi {

        public RandomEventConfigUi(Player player, RandomGameEvent<?> event, CustomInventory parent) {
            super(player, event, parent, "configuration-de-l-evenement");
        }

        @Override
        protected Map<String, Object> titlePlaceholders() {
            return Map.of("%événement%", targetName());
        }
    }
}
