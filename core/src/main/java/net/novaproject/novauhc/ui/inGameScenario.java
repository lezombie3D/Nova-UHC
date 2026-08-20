package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class inGameScenario extends CustomInventory {

    private static final int PER_PAGE = 27;

    private final boolean special;
    private int pages = 1;

    public inGameScenario(Player player, boolean special) {
        super(player);
        this.special = special;
    }

    public inGameScenario(Player player) {
        this(player, false);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.scenarios-en-jeu.title", "» Scénarios en Jeu"));
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return true;
    }

    @Override
    public int getCategories() {
        return pages;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.MAGENTA, 0, 8, 45, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        List<Scenario> scenarios = new ArrayList<>();
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (scenario.isSpecial() == special) scenarios.add(scenario);
        }

        pages = paginate(scenarios, PER_PAGE,
                n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                (scenario, slot) -> addItem(new StaticItem(slot, icon(scenario))));

        if (scenarios.size() <= PER_PAGE) return;

        addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.MAGENTA),
                t(DynamicLang.of("menu.scenarios-en-jeu.previous.name", "§8┃ §cPrécédent")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                previousCategory();
                open();
            }
        });

        addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.MAGENTA),
                t(DynamicLang.of("menu.scenarios-en-jeu.next.name", "§8┃ §aSuivant")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                nextCategory();
                open();
            }
        });
    }

    private ItemCreator icon(Scenario scenario) {
        ItemStack source = scenario.getItem().getItemstack();
        return new ItemCreator(source.getType())
                .setDurability(source.getDurability())
                .setName(t(DynamicLang.of("menu.scenarios-en-jeu.scenario-dummy.name", "§8┃ §f%nom_du_scenario%"))
                        .replace("%nom_du_scenario%", scenario.getName()));
    }
}
