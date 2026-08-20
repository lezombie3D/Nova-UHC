package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class EffectsConfigUi extends CustomInventory {

    public EffectsConfigUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.options-effets-globaux.title", "» Effet"));
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void setup() {
        UHCManager uhc = UHCManager.get();

        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17);
        UiItems.panes(this, DyeColor.WHITE, 19, 25);

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.MAGENTA),
                t(DynamicLang.of("menu.options-effets-globaux.btn-18.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new GameUi(getPlayer()).open();
            }
        });

        UiItems.panes(this, DyeColor.MAGENTA, 0, 8, 26);

        percent(12, DyeColor.CYAN, "menu.options-effets-globaux.btn-12.name",
                "§8┃ §3Resistance : %res_player_percent%", "%res_player_percent%",
                uhc.getGlobalResistancePercent(), pct -> {
                    uhc.setGlobalResistancePercent(pct);
                    UHCPlayerManager.get().getOnlineUHCPlayers().forEach(p -> p.setResistancePercent(pct));
                });

        percent(11, DyeColor.RED, "menu.options-effets-globaux.btn-11.name",
                "§8┃ §cForce : %player_force_percent%", "%player_force_percent%",
                uhc.getGlobalForcePercent(), pct -> {
                    uhc.setGlobalForcePercent(pct);
                    UHCPlayerManager.get().getOnlineUHCPlayers().forEach(p -> p.setForcePercent(pct));
                });

        percent(15, DyeColor.ORANGE, "menu.options-effets-globaux.btn-15.name",
                "§8┃ §6Critique : %player_crit_percent%", "%player_crit_percent%",
                uhc.getGlobalForceCriticPercent(), pct -> {
                    uhc.setGlobalForceCriticPercent(pct);
                    UHCPlayerManager.get().getOnlineUHCPlayers().forEach(p -> p.setForceCriticPercent(pct));
                });

        percent(14, DyeColor.YELLOW, "menu.options-effets-globaux.btn-14.name",
                "§8┃ §eSpeed : %player_speed_percent%", "%player_speed_percent%",
                uhc.getGlobalSpeedPercent(), pct -> {
                    uhc.setGlobalSpeedPercent(pct);
                    UHCPlayerManager.get().getOnlineUHCPlayers().forEach(p -> p.setSpeedPercent(pct));
                });
    }

    private void percent(int slot, DyeColor dye, String langKey, String frName, String placeholder,
                         double current, Consumer<Double> apply) {
        int value = (int) (current * 100);
        ItemCreator icon = new ItemCreator(Material.INK_SACK)
                .setDurability((short) dye.getDyeData())
                .setName(t(DynamicLang.of(langKey, frName)).replace(placeholder, value + "%"));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, value, 0, 200,
                        EffectsConfigUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        apply.accept(newValue.intValue() / 100.0);
                    }
                }.open();
            }
        });
    }
}
