package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ScenariosUi extends CustomInventory {

    private final boolean special;
    private final String NEXT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0=";
    private final String PREVIOUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjMwYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI4ODFhYWNjNyJ9fX0=";

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
        ItemCreator prevButton = UHCUtils.createCustomButon(PREVIOUS, "§8§l┃ §f§lPage Précédente", null);
        ItemCreator nextButton = UHCUtils.createCustomButon(NEXT, "§8§l┃ §f§lPage Suivante", null);
        int slotprev = 39;
        int slotnext = 41;
        if (special) {
            color = getConfig().getInt("menu.scenario.special.color");

        } else {
            color = getConfig().getInt("menu.scenario.color");
            slotnext = 50;
            slotprev = 48;
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

        if (getCategories() > 1) {
            for (int page = 1; page <= getCategories(); page++) {
                if (page > 1) {
                    addItem(new ActionItem(page, slotprev, prevButton) {
                        @Override public void onClick(InventoryClickEvent e) {
                            previousCategory();
                            refresh();
                        }
                    });
                }
                if (page < getCategories()) {
                    addItem(new ActionItem(page, slotnext, nextButton) {
                        @Override public void onClick(InventoryClickEvent e) {
                            nextCategory();
                            refresh();
                        }
                    });
                }
            }
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
