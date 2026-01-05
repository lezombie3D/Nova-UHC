package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.scenario.role.camps.CampUtils;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScenarioRoleUi<T extends Role> extends CustomInventory {

    private final ScenarioRole<T> scenario;
    private static final int[] ROLE_SLOTS = {
            11, 12, 13, 14, 15,
            20, 21, 22, 23, 24,
            19, 30, 31, 32, 33
    };
    private static final int RETURN_SLOT = 36;
    private static final int ROLES_PER_PAGE = 15;
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

        List<T> roles = new ArrayList<>();
        for (Map.Entry<T, Integer> entry : scenario.getDefault_roles().entrySet()) {
            T role = entry.getKey();
            boolean belongsToParent = role.getCamp() == parentCamp
                    || CampUtils.getSubCamps(parentCamp, scenario.getCamps()).contains(role.getCamp());
            if (belongsToParent) roles.add(role);
        }

        int totalCategories = (int) Math.ceil((double) roles.size() / ROLES_PER_PAGE);

        for (int i = 0; i < roles.size(); i++) {
            T role = roles.get(i);
            int categoryForThisRole = (int) Math.ceil((double) (i + 1) / ROLES_PER_PAGE);
            int positionInPage = (i % ROLES_PER_PAGE);
            int slot = ROLE_SLOTS[positionInPage];

            ItemCreator item = role.getItem()
                    .setName(role.getColor() + role.getName() + " : " + scenario.getDefault_roles().get(role))
                    .setAmount(scenario.getDefault_roles().get(role));

            addItem(new ActionItem(categoryForThisRole, slot, item) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    Class<? extends T> roleClass = (Class<? extends T>) role.getClass();
                    int amount = scenario.getDefault_roles().get(role);
                    if(e.isShiftClick()) {
                        new RoleConfigUi(getPlayer(),role,ScenarioRoleUi.this).open();
                        return;
                    } else
                    if (e.isRightClick() && amount > 0) {
                        scenario.decrementRole(roleClass);
                    } else if (e.isLeftClick()) {
                        scenario.incrementRole(roleClass);
                    }
                    new ScenarioRoleUi<>(getPlayer(), scenario, parentCamp).open();
                }
            });
        }

        if (totalCategories > 1) {
            addPage(40);
        }
    }

    @Override
    public String getTitle() {
        return "§f§l | Rôles du camp : " + parentCamp.getName();
    }

    @Override
    public int getLines() {
        return 5;
    }

    @Override
    public int getCategories() {
        List<T> roles = new ArrayList<>();
        for (Map.Entry<T, Integer> entry : scenario.getDefault_roles().entrySet()) {
            T role = entry.getKey();
            boolean belongsToParent = role.getCamp() == parentCamp
                    || CampUtils.getSubCamps(parentCamp, scenario.getCamps()).contains(role.getCamp());
            if (belongsToParent) roles.add(role);
        }
        return Math.max(1, (int) Math.ceil((double) roles.size() / ROLES_PER_PAGE));
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
