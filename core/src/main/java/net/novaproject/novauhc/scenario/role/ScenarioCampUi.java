package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.ui.ScenarioVariableUiLang;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScenarioCampUi<T extends Role> extends CustomInventory {

    private final ScenarioRole<T> scenario;
    private static final int CAMPS_PER_PAGE = 10;
    private static final String NEXT     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0=";
    private static final String PREVIOUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjMwYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI4ODFhYWNjNyJ9fX0=";

    public ScenarioCampUi(Player player, ScenarioRole<T> scenario) {
        super(player);
        this.scenario = scenario;
    }

    @Override
    public void setup() {
        fillCorner(0);

        int returnSlot = (getLines() - 1) * 9 + 4;
        addReturn(returnSlot, new ScenariosUi(getPlayer(), true));

        ItemCreator prevButton = UHCUtils.createCustomButon(PREVIOUS, LangManager.get().get(CommonLang.PAGE_PREVIOUS, getPlayer()), null);
        ItemCreator nextButton = UHCUtils.createCustomButon(NEXT,     LangManager.get().get(CommonLang.PAGE_NEXT,     getPlayer()), null);

        int prevSlot = returnSlot - 2;
        int nextSlot = returnSlot + 2;

        if (getCategories() > 1) {
            for (int page = 1; page <= getCategories(); page++) {
                if (page > 1) {
                    addItem(new ActionItem(page, prevSlot, prevButton) {
                        @Override public void onClick(InventoryClickEvent e) { previousCategory(); refresh(); }
                    });
                }
                if (page < getCategories()) {
                    addItem(new ActionItem(page, nextSlot, nextButton) {
                        @Override public void onClick(InventoryClickEvent e) { nextCategory(); refresh(); }
                    });
                }
            }
        }

        List<Camps> mainCamps = getMainCamps();
        for (int i = 0; i < mainCamps.size(); i++) {
            Camps camp    = mainCamps.get(i);
            int page      = i / CAMPS_PER_PAGE + 1;
            int posInPage = i % CAMPS_PER_PAGE;
            int pageSize  = Math.min(CAMPS_PER_PAGE, mainCamps.size() - (page - 1) * CAMPS_PER_PAGE);
            int slot      = computeCampSlots(pageSize)[posInPage];

            addItem(new ActionItem(page, slot, camp.getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ScenarioRoleUi<>(getPlayer(), scenario, camp).open();
                }
            });
        }
    }

    private int[] computeCampSlots(int n) {
        int row1Count = Math.min(n, 5);
        int row2Count = n - row1Count;
        int[] slots   = new int[n];
        int idx       = 0;
        int startRow  = row2Count > 0 ? 1 : (getLines() / 2);

        idx = fillSpacedRow(slots, idx, startRow, row1Count);
        if (row2Count > 0) fillSpacedRow(slots, idx, startRow + 1, row2Count);

        return slots;
    }

    private int fillSpacedRow(int[] slots, int idx, int row, int count) {
        int span     = count > 1 ? 2 * count - 1 : 1;
        int startCol = (9 - span) / 2;
        for (int i = 0; i < count; i++) slots[idx++] = row * 9 + startCol + i * 2;
        return idx;
    }

    private List<Camps> getMainCamps() {
        List<Camps> mainCamps = new ArrayList<>();
        for (Camps camp : scenario.getCamps()) {
            if (camp.isMainCamp()) mainCamps.add(camp);
        }
        return mainCamps;
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(
                ScenarioVariableUiLang.CAMP_UI_TITLE,
                getPlayer(),
                Map.of("%scenario%", scenario.getName())
        );
    }

    @Override
    public int getCategories() {
        return Math.max(1, (int) Math.ceil((double) getMainCamps().size() / CAMPS_PER_PAGE));
    }

    @Override
    public int getLines() {
        int n = getMainCamps().size();
        return n <= 5 ? 3 : 4;
    }

    @Override public boolean isRefreshAuto() { return false; }
}