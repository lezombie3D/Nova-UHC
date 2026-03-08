package net.novaproject.ultimate.slavemarket;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.special.SlaveMarketLang;
import net.novaproject.novauhc.lang.ui.DefaultUiLang;
import net.novaproject.novauhc.scenario.ScenarioVariableUi;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SlaveMarketUi extends CustomInventory {

    private final SlaveMarket slave = SlaveMarket.get();

    public SlaveMarketUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCadre(0);
        addReturn(36, new ScenariosUi(getPlayer(), true));

        addMenu(44, new ItemCreator(Material.PRISMARINE_SHARD)
                        .setName(LangManager.get().get(CommonLang.CLICK_HERE_TO_MODIFY, getPlayer())),
                new ScenarioVariableUi(getPlayer(), slave, this));

        addItem(new ActionItem(3, getWool(UHCManager.get().isStarted())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (slave.canBuy()) {
                    slave.startEnchere();
                } else {
                    slave.cancelAction();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        new Titles().sendTitle(p,
                                LangManager.get().get(SlaveMarketLang.UI_AUCTION_CANCELLED_TITLE, p),
                                "", 8);
                    }
                }
                openAll();
            }
        });

        addMenu(5, new ItemCreator(Material.DIAMOND)
                        .setName(LangManager.get().get(SlaveMarketLang.UI_DIAMOND_ITEM_NAME, getPlayer(),
                                Map.of("%value%", String.valueOf(slave.getNbDiamond()))))
                        .addLore("")
                        .addLore(LangManager.get().get(SlaveMarketLang.UI_DIAMOND_LORE_MODIFY, getPlayer()))
                        .addLore(LangManager.get().get(SlaveMarketLang.UI_DIAMOND_LORE_COUNT, getPlayer()))
                        .addLore(LangManager.get().get(SlaveMarketLang.UI_DIAMOND_LORE_OWNER, getPlayer()))
                        .addLore(""),
                new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, slave.getNbDiamond(), 20, 100, this) {
                    @Override
                    public void onChange(Number newValue) {
                        slave.setNbDiamond((int) newValue);
                    }
                });

        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        int[] slots = computeTeamSlots(teams.size());

        for (int i = 0; i < teams.size(); i++) {
            UHCTeam team = teams.get(i);
            int slot = slots[i];

            ItemCreator item = buildTeamItem(team);

            addItem(new ActionItem(slot, item) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new SlaveMarketTeamUi(getPlayer(), team, SlaveMarketUi.this).open();
                }
            });
        }
    }

    private ItemCreator buildTeamItem(UHCTeam team) {
        var assignedOwner = slave.getOwners().get(team);
        int diamonds = slave.getDiamondsForTeam(team);
        boolean hasOwner = assignedOwner != null;

        ItemCreator item = new ItemCreator(hasOwner ? Material.SKULL_ITEM : Material.STAINED_GLASS_PANE)
                .setDurability(hasOwner ? (short) 3 : (short) 15)
                .setName(team.prefix())
                .addLore("")
                .addLore(ChatColor.GRAY + LangManager.get().get(SlaveMarketLang.UI_DIAMONDS_LABEL, getPlayer())
                        + ChatColor.AQUA + diamonds);

        if (hasOwner) item.setOwner(assignedOwner.getPlayer().getName());

        if (hasOwner) {
            item.addLore(ChatColor.GRAY + LangManager.get().get(SlaveMarketLang.UI_OWNER_LABEL, getPlayer())
                    + ChatColor.GOLD + assignedOwner.getPlayer().getName());
        } else {
            item.addLore(ChatColor.GRAY + LangManager.get().get(SlaveMarketLang.UI_NO_OWNER, getPlayer()));
        }

        item.addLore("").addLore(ChatColor.YELLOW + LangManager.get().get(SlaveMarketLang.UI_CLICK_CONFIGURE, getPlayer()));
        return item;
    }

    private int[] computeTeamSlots(int count) {
        int[] slots = new int[count];
        int idx = 0;
        for (int row = 2; row <= 3 && idx < count; row++) {
            int inRow    = Math.min(7, count - idx);
            int startCol = 1 + (7 - inRow) / 2;
            for (int j = 0; j < inRow; j++) slots[idx++] = row * 9 + startCol + j;
        }
        return slots;
    }


    private ItemCreator getWool(boolean started) {
        ItemCreator dyec = new ItemCreator(Material.INK_SACK).setDurability((short) (started ? 8 : 10));
        if (started) {
            dyec.setName(t(SlaveMarketLang.UI_WOOL_CANCEL));
        } else {
            dyec.setName(t(SlaveMarketLang.UI_WOOL_START));
        }
        return dyec;
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(SlaveMarketLang.UI_TITLE, getPlayer());
    }

    @Override public int getLines() { return 5; }
    @Override public boolean isRefreshAuto() { return false; }
}