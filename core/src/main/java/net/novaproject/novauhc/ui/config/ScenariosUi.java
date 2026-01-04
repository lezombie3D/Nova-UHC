package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ScenariosUi extends CustomInventory {

    private final boolean special;

    public ScenariosUi(Player player, boolean special) {
        super(player);
        this.special = special;
    }

    public ScenariosUi(Player player) {
        this(player, false);
    }

    @Override
    public void setup() {
        int color;
        if (special) {
            color = getConfig().getInt("menu.scenario.special.color");
        } else {
            color = getConfig().getInt("menu.scenario.color");
        }
        fillCadre(color);

        if (getLines() == 6) {
            addReturn(45, new DefaultUi(getPlayer()));
        } else {
            addReturn(36, new DefaultUi(getPlayer()));
        }

        int totalScenarios = 0;
        for (Scenario scenario : ScenarioManager.get().getScenarios()) {
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
        for (Scenario scenario : ScenarioManager.get().getScenarios()) {
            if (scenario.isSpecial() != special) continue;

            currentScenario++;
            int categoryForThisItem = (int) Math.ceil((double) currentScenario / scenariosPerPage);
            int positionInCategory = (currentScenario - 1) % scenariosPerPage;
            int slot = calculateSlot(positionInCategory);

            ItemCreator item = scenario.getItem()
                    .setName("§8┃ §f" + scenario.getName() + ": " + (scenario.isActive() ? "§2Activé" : "§cDésactivé"))
                    .addLore("")
                    .addLore("  §8┃ §f" + scenario.getDescription())
                    .addLore("")
                    .addLore(CommonString.CLICK_HERE_TO_TOGGLE.getMessage());

            if (scenario.isSpecial()) {
                item.addLore(CommonString.CLICK_GAUCHE.getMessage() + "§8» §a§lOuvrir la configuration");
            }
            item.addLore("")
                    .setAmount(scenario.isActive() ? 1 : 0);

            addItem(new ActionItem(categoryForThisItem, slot, item) {
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


    private int calculateSlot(int position) {
        int row = position / 7;
        int col = position % 7;
        return 10 + col + (row * 9);
    }

    @Override
    public int getCategories() {
        int totalScenarios = 0;
        for (Scenario scenario : ScenarioManager.get().getScenarios()) {
            if (scenario.isSpecial() == special) {
                totalScenarios++;
            }
        }

        return (int) Math.ceil((double) totalScenarios / 27);
    }

    @Override
    public String getTitle() {
        if (special) {
            return getConfig().getString("menu.scenario.special.title");
        }
        return getConfig().getString("menu.scenario.title");
    }

    @Override
    public int getLines() {
        return special ? (ScenarioManager.get().getSpecialScenarios().size() > 9 * 5 ? 6 : 5) : (ScenarioManager.get().getScenarios().size() > 9 * 5 ? 6 : 5);
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
