package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class ScenarioCampUi<T extends Role> extends CustomInventory {

    private final ScenarioRole<T> scenario;

    public ScenarioCampUi(Player player, ScenarioRole<T> scenario) {
        super(player);
        this.scenario = scenario;
    }

    @Override
    public void setup() {

        fillCorner(0);
        List<Camps> mainCamps = new ArrayList<>();
        for (Camps camp : scenario.getCamps()) {
            if (camp.isMainCamp()) mainCamps.add(camp);
        }

        int numCamps = mainCamps.size();
        int[] slots;
        int returnSlot;


        if (numCamps >= 1 && numCamps <= 3) {

            slots = new int[]{11, 13, 15};
            returnSlot = 18;
        } else if (numCamps >= 4 && numCamps <= 5) {

            slots = new int[]{11, 13, 15, 21, 23};
            returnSlot = 27;
        } else {
            slots = new int[]{29, 31, 33, 37, 39, 41, 43, 45};
            returnSlot = 36;
        }

        for (int i = 0; i < mainCamps.size(); i++) {
            int slot = i < slots.length ? slots[i] : -1;
            if (slot == -1) break;
            Camps camp = mainCamps.get(i);
            addItem(new ActionItem(slot, camp.getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ScenarioRoleUi<>(getPlayer(), scenario, camp).open();
                }
            });
        }

        addReturn(returnSlot, new ScenariosUi(getPlayer(), true));
    }

    @Override
    public String getTitle() {
        return scenario.getName() + " §f§l| Choisir un Camps";
    }

    @Override
    public int getLines() {
        List<Camps> mainCamps = new ArrayList<>();
        for (Camps camp : scenario.getCamps()) {
            if (camp.isMainCamp()) mainCamps.add(camp);
        }
        int numCamps = mainCamps.size();
        if (numCamps >= 1 && numCamps <= 3) return 3;
        if (numCamps >= 4 && numCamps <= 5) return 4;
        return 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
