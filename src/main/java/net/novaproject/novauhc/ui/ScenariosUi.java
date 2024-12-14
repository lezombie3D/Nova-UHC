package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ScenariosUi extends CustomInventory {

    public ScenariosUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {

        int slot = 0;
        int cat = 1;

        for (Scenario scenario : ScenarioManager.get().getScenarios()){

            ItemCreator item = scenario.getItem();

            item.setName(scenario.getName() + ": " + scenario.isActive());
            item.setAmount(scenario.isActive() ? 1 : 0);

            addItem(new ActionItem(cat, slot, scenario.getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    scenario.toggleActive();
                    openAll();
                }
            });

            slot++;

            if (slot == 45){
                cat++;
                slot = 0;
            }

        }

        if (cat > 1)
            addPage(49);
    }

    @Override
    public String getTitle() {
        return "Scenarios";
    }

    @Override
    public int getLines() {
        return ScenarioManager.get().getScenarios().size() > 9 * 5 ? 6 : 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
