package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.entity.Player;

public class inGameScenario extends CustomInventory {

    private final boolean special;

    public inGameScenario(Player player, boolean special) {
        super(player);
        this.special = special;
    }

    public inGameScenario(Player player) {
        this(player, false);
    }

    @Override
    public void setup() {
        int color;
        if (special) {
            color = getConfig().getInt("menu.scenario.special.ig.color");
        } else {
            color = getConfig().getInt("menu.scenario.ig.color");
        }
        fillCadre(color);

        int totalScenarios = 0;
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (scenario.isSpecial() == special) {
                totalScenarios++;
            }
        }

        int scenariosPerPage = 28;
        int totalCategories = (int) Math.ceil((double) totalScenarios / scenariosPerPage);

        if (totalCategories > 1) {
            addPage(49);
        }

        int currentScenario = 0;
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (scenario.isSpecial() != special) continue;

            currentScenario++;
            int categoryForThisItem = (int) Math.ceil((double) currentScenario / scenariosPerPage);
            int positionInCategory = (currentScenario - 1) % scenariosPerPage;
            int slot = calculateSlot(positionInCategory);

            ItemCreator item = scenario.getItem();
            item.setName("§8┃ §f" + scenario.getName());

            addItem(new StaticItem(categoryForThisItem, slot, item));
        }
    }


    private int calculateSlot(int position) {
        int row = position / 7;
        int col = position % 7;
        return 10 + col + (row * 9);
    }

    @Override
    public int getCategories() {
        int totalScenarios = 0;
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (scenario.isSpecial() == special) {
                totalScenarios++;
            }
        }

        return (int) Math.ceil((double) totalScenarios / 27);
    }

    @Override
    public String getTitle() {
        if (special) {
            return getConfig().getString("menu.scenario.special.ig.title");
        }
        return getConfig().getString("menu.scenario.ig.title");
    }

    @Override
    public int getLines() {
        return special ? (ScenarioManager.get().getSpecialScenarios().size() > 9 * 5 ? 6 : 5) : (ScenarioManager.get().getActiveScenarios().size() > 9 * 5 ? 6 : 5);
    }

    @Override
    public boolean isRefreshAuto() {
        return true;
    }
}
