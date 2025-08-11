package net.novaproject.novauhc.scenario.role;

import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Map;

public class ScenarioRoleUi<T extends Role> extends CustomInventory {

    private final ScenarioRole<T> scenario;

    public ScenarioRoleUi(Player player, ScenarioRole<T> scenario) {
        super(player);
        this.scenario = scenario;
    }

    @Override
    public void setup() {
        int slot = 0;

        for (Map.Entry<T, Integer> entry : scenario.getDefault_roles().entrySet()) {

            T role = entry.getKey();
            int amount = entry.getValue();

            Class<? extends T> roleClass = (Class<? extends T>) role.getClass();

            ItemCreator item = role.getItem()
                    .setName(role.getColor() + role.getName() + " : " + amount)
                    .setAmount(amount);

            addItem(new ActionItem(slot, item) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (e.isRightClick()) {
                        if (amount > 0) {
                            scenario.decrementRole(roleClass);
                            openAll();
                        }
                    } else if (e.isLeftClick()) {
                        scenario.incrementRole(roleClass);
                        openAll();
                    }
                }
            });

            slot++;
        }

        addReturn(49, new ScenariosUi(getPlayer(), true));
    }

    @Override
    public String getTitle() {
        return ChatColor.DARK_PURPLE + "Role de : " + scenario.getName();
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

}
