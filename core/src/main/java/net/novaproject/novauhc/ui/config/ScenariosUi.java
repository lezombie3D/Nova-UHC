package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.ui.ScenariosUiLang;
import net.novaproject.novauhc.lang.ui.UiTitleLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class ScenariosUi extends CustomInventory {

    private final boolean special;
    private final String NEXT     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0=";
    private final String PREVIOUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjMwYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI4ODFhYWNjNyJ9fX0=";

    public ScenariosUi(Player player, boolean special) {
        super(player);
        this.special = special;
    }

    public ScenariosUi(Player player) {
        this(player, false);
    }

    private String t(ScenariosUiLang key) {
        return LangManager.get().get(key, getPlayer());
    }

    @Override
    public void setup() {
        int color;
        int slotPrev;
        int slotNext;
        if (special) {
            color    = getConfig().getInt("menu.scenario.special.color");
            slotPrev = 39;
            slotNext = 41;
        } else {
            color    = getConfig().getInt("menu.scenario.color");
            slotPrev = 48;
            slotNext = 50;
        }
        fillCadre(color);

        if (getLines() == 6) addReturn(45, new DefaultUi(getPlayer()));
        else                  addReturn(36, new DefaultUi(getPlayer()));

        ItemCreator prevButton = UHCUtils.createCustomButon(PREVIOUS, LangManager.get().get(CommonLang.PAGE_PREVIOUS, getPlayer()), null);
        ItemCreator nextButton = UHCUtils.createCustomButon(NEXT,     LangManager.get().get(CommonLang.PAGE_NEXT,     getPlayer()), null);

        if (getCategories() > 1) {
            for (int page = 1; page <= getCategories(); page++) {
                if (page > 1) {
                    addItem(new ActionItem(page, slotPrev, prevButton) {
                        @Override public void onClick(InventoryClickEvent e) { previousCategory(); refresh(); }
                    });
                }
                if (page < getCategories()) {
                    addItem(new ActionItem(page, slotNext, nextButton) {
                        @Override public void onClick(InventoryClickEvent e) { nextCategory(); refresh(); }
                    });
                }
            }
        }

        List<Scenario> scenarios = getFilteredScenarios();
        int scenariosPerPage     = 28;

        for (int i = 0; i < scenarios.size(); i++) {
            Scenario scenario = scenarios.get(i);
            int page          = i / scenariosPerPage + 1;
            int posInPage     = i % scenariosPerPage;
            int pageSize      = Math.min(scenariosPerPage, scenarios.size() - (page - 1) * scenariosPerPage);
            int slot          = computeScenarioSlots(pageSize)[posInPage];

            String status = scenario.isActive() ? t(ScenariosUiLang.SCENARIO_ACTIVE) : t(ScenariosUiLang.SCENARIO_DISABLED);

            ItemCreator item = scenario.getItem()
                    .setName("§8┃ §f" + scenario.getName() + ": " + status)
                    .addLore("")
                    .addLore("  §8┃ §f" + scenario.getDescription(getPlayer()))
                    .addLore("")
                    .addLore(LangManager.get().get(CommonLang.CLICK_HERE_TO_TOGGLE, getPlayer()));

            if (scenario.isSpecial()) item.addLore(t(ScenariosUiLang.OPEN_CONFIG_LORE));
            item.addLore("").setAmount(scenario.isActive() ? 1 : 0);

            addItem(new ActionItem(page, slot, item) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    CustomInventory inv = scenario.getMenu(getPlayer());
                    if (inv != null && scenario.isActive() && e.isRightClick()) {
                        inv.open();
                        return;
                    }
                    scenario.toggleActive();
                    openAll();
                    if (inv != null && scenario.isActive()) inv.open();
                }
            });
        }
    }

    private int[] computeScenarioSlots(int count) {
        int total    = Math.min(count, 28);
        int[] slots  = new int[total];
        int idx      = 0;
        for (int row = 1; row <= 4 && idx < total; row++) {
            int inRow    = Math.min(7, total - idx);
            int startCol = 1 + (7 - inRow) / 2;
            for (int j = 0; j < inRow; j++) slots[idx++] = row * 9 + startCol + j;
        }
        return slots;
    }

    private List<Scenario> getFilteredScenarios() {
        List<Scenario> list = new ArrayList<>();
        for (Scenario s : ScenarioManager.get().getScenarios()) {
            if (s.isSpecial() == special) list.add(s);
        }
        return list;
    }

    @Override
    public int getCategories() {
        return Math.max(1, (int) Math.ceil((double) getFilteredScenarios().size() / 28));
    }

    @Override
    public String getTitle() {
        if (special) return LangManager.get().get(UiTitleLang.SCENARIO_SPECIAL_TITLE, getPlayer());
        return LangManager.get().get(UiTitleLang.SCENARIO_TITLE, getPlayer());
    }

    @Override
    public int getLines() {
        return special
                ? (ScenarioManager.get().getSpecialScenarios().size() > 9 * 5 ? 6 : 5)
                : (ScenarioManager.get().getScenarios().size()         > 9 * 5 ? 6 : 5);
    }

    @Override
    public boolean isRefreshAuto() { return false; }
}