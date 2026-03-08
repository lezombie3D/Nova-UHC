package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.ui.ScenarioVariableUiLang;
import net.novaproject.novauhc.scenario.role.camps.CampUtils;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScenarioRoleUi<T extends Role> extends CustomInventory {

    private final ScenarioRole<T> scenario;
    private static final int ROLES_PER_PAGE = 15;
    private static final int ITEMS_PER_ROW  = 5;
    private static final int RETURN_SLOT    = 36;
    private static final int SLOT_PREV      = 39;
    private static final int SLOT_NEXT      = 41;
    private static final String NEXT     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0=";
    private static final String PREVIOUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjMwYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI4ODFhYWNjNyJ9fX0=";
    private final Camps parentCamp;

    public ScenarioRoleUi(Player player, ScenarioRole<T> scenario, Camps parentCamp) {
        super(player);
        this.scenario = scenario;
        this.parentCamp = parentCamp;
    }

    @Override
    public void setup() {
        fillDesign(0);
        addReturn(RETURN_SLOT, new ScenarioCampUi<>(getPlayer(), scenario));

        ItemCreator prevButton = UHCUtils.createCustomButon(PREVIOUS, LangManager.get().get(CommonLang.PAGE_PREVIOUS, getPlayer()), null);
        ItemCreator nextButton = UHCUtils.createCustomButon(NEXT,     LangManager.get().get(CommonLang.PAGE_NEXT,     getPlayer()), null);

        if (getCategories() > 1) {
            for (int page = 1; page <= getCategories(); page++) {
                if (page > 1) {
                    addItem(new ActionItem(page, SLOT_PREV, prevButton) {
                        @Override public void onClick(InventoryClickEvent e) { previousCategory(); refresh(); }
                    });
                }
                if (page < getCategories()) {
                    addItem(new ActionItem(page, SLOT_NEXT, nextButton) {
                        @Override public void onClick(InventoryClickEvent e) { nextCategory(); refresh(); }
                    });
                }
            }
        }

        List<T> roles = getRolesForCamp();
        for (int i = 0; i < roles.size(); i++) {
            T role      = roles.get(i);
            int page    = i / ROLES_PER_PAGE + 1;
            int posInPage = i % ROLES_PER_PAGE;
            int pageSize  = Math.min(ROLES_PER_PAGE, roles.size() - (page - 1) * ROLES_PER_PAGE);
            int slot      = computeRoleSlots(pageSize)[posInPage];

            ItemCreator item = role.getItem()
                    .setName(role.getColor() + role.getName() + " : " + scenario.getDefault_roles().get(role))
                    .setAmount(scenario.getDefault_roles().get(role));

            addItem(new ActionItem(page, slot, item) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    Class<? extends T> roleClass = (Class<? extends T>) role.getClass();
                    int amount = scenario.getDefault_roles().get(role);
                    if (e.isShiftClick()) {
                        new RoleConfigUi(getPlayer(), role, ScenarioRoleUi.this).open();
                    } else if (e.isRightClick() && amount > 0) {
                        scenario.decrementRole(roleClass);
                        new ScenarioRoleUi<>(getPlayer(), scenario, parentCamp).open();
                    } else if (e.isLeftClick()) {
                        scenario.incrementRole(roleClass);
                        new ScenarioRoleUi<>(getPlayer(), scenario, parentCamp).open();
                    }
                }
            });
        }
    }

    private int[] computeRoleSlots(int count) {
        int[] slots = new int[count];
        int idx = 0;
        for (int row = 1; row <= 3 && idx < count; row++) {
            int inRow    = Math.min(ITEMS_PER_ROW, count - idx);
            int startCol = (9 - inRow) / 2;
            for (int j = 0; j < inRow; j++) slots[idx++] = row * 9 + startCol + j;
        }
        return slots;
    }

    private List<T> getRolesForCamp() {
        List<T> roles = new ArrayList<>();
        for (Map.Entry<T, Integer> entry : scenario.getDefault_roles().entrySet()) {
            T role = entry.getKey();
            boolean belongs = role.getCamp() == parentCamp
                    || CampUtils.getSubCamps(parentCamp, scenario.getCamps()).contains(role.getCamp());
            if (belongs) roles.add(role);
        }
        return roles;
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(
                ScenarioVariableUiLang.ROLE_UI_TITLE,
                getPlayer(),
                Map.of("%camp%", parentCamp.getName())
        );
    }

    @Override
    public int getCategories() {
        return Math.max(1, (int) Math.ceil((double) getRolesForCamp().size() / ROLES_PER_PAGE));
    }

    @Override public int getLines() { return 5; }
    @Override public boolean isRefreshAuto() { return false; }
}