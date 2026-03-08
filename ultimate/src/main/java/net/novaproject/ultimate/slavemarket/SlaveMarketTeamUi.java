package net.novaproject.ultimate.slavemarket;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.special.SlaveMarketLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Map;

public class SlaveMarketTeamUi extends CustomInventory {

    private final SlaveMarket slave = SlaveMarket.get();
    private final UHCTeam team;
    private final CustomInventory parent;

    public SlaveMarketTeamUi(Player player, UHCTeam team, CustomInventory parent) {
        super(player);
        this.team = team;
        this.parent = parent;
    }

    @Override
    public void setup() {
        fillCadre(0);
        addReturn(22, parent);

        UHCPlayer assignedOwner = slave.getOwners().get(team);

        if (assignedOwner != null) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta  = (SkullMeta) skull.getItemMeta();
            meta.setOwner(assignedOwner.getPlayer().getName());
            meta.setDisplayName(ChatColor.GOLD + assignedOwner.getPlayer().getName());
            meta.setLore(java.util.List.of(
                    "",
                    ChatColor.RED + LangManager.get().get(SlaveMarketLang.UI_CLICK_REMOVE_OWNER, getPlayer())
            ));
            skull.setItemMeta(meta);

            addItem(new ActionItem(11, skull) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    slave.removeOwner(team);
                    openAll();
                }
            });
        } else {
            addItem(new ActionItem(11, new ItemCreator(Material.STAINED_GLASS_PANE)
                    .setDurability((short) 15)
                    .setName(ChatColor.GRAY + LangManager.get().get(SlaveMarketLang.UI_NO_OWNER, getPlayer()))
                    .addLore("")
                    .addLore(ChatColor.YELLOW + LangManager.get().get(SlaveMarketLang.UI_CLICK_ASSIGN_OWNER, getPlayer()))) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new AnvilUi(getPlayer(), new SlaveMarketTeamUi(getPlayer(), team, parent), event -> {
                        if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                            Player found = Bukkit.getPlayer(event.getName());
                            if (found == null) {
                                LangManager.get().send(SlaveMarketLang.UI_PLAYER_NOT_FOUND, getPlayer());
                                return;
                            }
                            UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(found);
                            slave.assignOwner(team, uhcPlayer);
                        }
                    }).setSlot(LangManager.get().get(SlaveMarketLang.UI_ADD_PLAYER_ANVIL, getPlayer())).open();
                }
            });
        }

        int currentDiamonds = slave.getDiamondsForTeam(team);

        addMenu(15, new ItemCreator(Material.DIAMOND)
                        .setName(LangManager.get().get(SlaveMarketLang.UI_DIAMOND_ITEM_NAME, getPlayer(),
                                Map.of("%value%", String.valueOf(currentDiamonds))))
                        .addLore("")
                        .addLore(LangManager.get().get(SlaveMarketLang.UI_DIAMOND_LORE_MODIFY, getPlayer()))
                        .addLore(""),
                new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, currentDiamonds, 0, 0, this) {
                    @Override
                    public void onChange(Number newValue) {
                        slave.setDiamondsForTeam(team, newValue.intValue());
                    }
                });
    }

    @Override
    public String getTitle() {
        return LangManager.get().get(SlaveMarketLang.UI_TEAM_CONFIG_TITLE, getPlayer(),
                Map.of("%team%", team.prefix()));
    }

    @Override public int getLines() { return 3; }
    @Override public boolean isRefreshAuto() { return false; }
}