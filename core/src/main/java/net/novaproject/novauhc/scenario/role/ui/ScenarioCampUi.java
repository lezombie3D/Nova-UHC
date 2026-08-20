package net.novaproject.novauhc.scenario.role.ui;

import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.CampUtils;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

public class ScenarioCampUi<T extends Role> extends CustomInventory {

    private final ScenarioRole<T> scenario;
    private static final int CAMPS_PER_PAGE = 10;
    private static final String NEXT     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0=";
    private static final String PREVIOUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjMwYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI4ODFhYWNjNyJ9fX0=";

    private final boolean fillerPickMode;
    private final CustomInventory parent;

    public ScenarioCampUi(Player player, ScenarioRole<T> scenario) {
        this(player, scenario, false, null);
    }

    public ScenarioCampUi(Player player, ScenarioRole<T> scenario, boolean fillerPickMode) {
        this(player, scenario, fillerPickMode, null);
    }

    public ScenarioCampUi(Player player, ScenarioRole<T> scenario, CustomInventory parent) {
        this(player, scenario, false, parent);
    }

    public ScenarioCampUi(Player player, ScenarioRole<T> scenario, boolean fillerPickMode, CustomInventory parent) {
        super(player);
        this.scenario = scenario;
        this.fillerPickMode = fillerPickMode;
        this.parent = parent;
    }

    @Override
    public void setup() {
        fillCorner(0);

        int returnSlot = (getLines() - 1) * 9 + 4;
        addReturn(returnSlot, parent != null ? parent : new ScenariosUi(getPlayer(), true));

        String fillerName = scenario.getFillerRoleClass() != null
                && scenario.getRoleConfigs().get(scenario.getFillerRoleClass()) != null
                ? scenario.getRoleConfigs().get(scenario.getFillerRoleClass()).getName()
                : "§8—";
        ItemCreator fillerBtn = new ItemCreator(Material.NETHER_STAR)
                .setName(t(UiLang.ROLE_FILLER_BUTTON))
                .addLore(t(UiLang.ROLE_FILLER_CURRENT_BUTTON, Map.of("%name%", fillerName)));
        if (fillerPickMode) fillerBtn.addLore("").addLore(t(UiLang.ROLE_FILLER_PICK_HINT));
        addItem(new ActionItem(4, fillerBtn) {
            @Override public void onClick(InventoryClickEvent e) {
                new ScenarioCampUi<>(getPlayer(), scenario, !fillerPickMode, parent).open();
            }
        });

        ItemCreator prevButton = UiItems.createCustomButon(PREVIOUS, LangManager.get().get(CoreLang.COMMON_PAGE_PREVIOUS, getPlayer()), null);
        ItemCreator nextButton = UiItems.createCustomButon(NEXT,     LangManager.get().get(CoreLang.COMMON_PAGE_NEXT,     getPlayer()), null);

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

            ItemCreator campItem = buildCampItem(camp);

            addItem(new ActionItem(page, slot, campItem) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ScenarioRoleUi<>(getPlayer(), scenario, camp, fillerPickMode, parent).open();
                }
            });
        }
    }

    private ItemCreator buildCampItem(Camps camp) {
        List<T> rolesInCamp = getRolesInCamp(camp);
        int active = 0;
        int totalSlots = 0;
        for (T role : rolesInCamp) {
            Integer amount = scenario.getDefault_roles().get(role);
            if (amount == null) continue;
            if (amount > 0) active++;
            totalSlots += amount;
        }

        String counter = LangManager.get().get(
                UiLang.SCENARVAR_CAMP_COUNTER,
                getPlayer(),
                Map.of("%active%", String.valueOf(active),
                       "%total%",  String.valueOf(totalSlots))
        );

        ItemCreator item = new ItemCreator(camp.getItem())
                .setName(camp.getColor() + camp.getName() + counter);

        item.addLore(" ");
        item.addLore(LangManager.get().get(UiLang.SCENARVAR_CAMP_ACTIVE_HEADER, getPlayer()));

        boolean anyActive = false;
        for (T role : rolesInCamp) {
            Integer amount = scenario.getDefault_roles().get(role);
            if (amount == null || amount <= 0) continue;
            anyActive = true;
            item.addLore(LangManager.get().get(
                    UiLang.SCENARVAR_CAMP_ROLE_ENTRY,
                    getPlayer(),
                    Map.of("%color%", role.getColor(),
                           "%name%",  role.getName(),
                           "%count%", String.valueOf(amount))
            ));
        }
        if (!anyActive) {
            item.addLore(LangManager.get().get(UiLang.SCENARVAR_CAMP_NO_ACTIVE, getPlayer()));
        }

        return item;
    }

    private List<T> getRolesInCamp(Camps parentCamp) {
        List<T> list = new ArrayList<>();
        for (T role : scenario.getDefault_roles().keySet()) {
            if (CampUtils.roleBelongsToCamp(role, parentCamp, scenario.getCamps())) {
                list.add(role);
            }
        }
        return list;
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
                UiLang.SCENARVAR_CAMP_UI_TITLE,
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

