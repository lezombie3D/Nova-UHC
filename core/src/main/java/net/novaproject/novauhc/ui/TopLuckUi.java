package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TopLuckUi extends CustomInventory {

    private static final int PER_PAGE = 28;

    private static final String ENTRY_LORE = "§7» Élément §edynamique\n"
            + "\n"
            + " §e┃ §fPierre minée : §f%stone%\n"
            + " §b┃ §fDiamant : §b%diamond% §8(§b1/%diamond_ratio%§8)\n"
            + " §6┃ §fOr : §6%gold% §8(§61/%gold_ratio%§8)\n"
            + " §7┃ §fFer : §f%iron% §8(§f1/%iron_ratio%§8)\n"
            + "\n"
            + "§8» §fClic §egauche §fpour ses §eactions§f.\n"
            + "§8» §fClic §edroit §fpour s'y §etéléporter§f.";

    private int pages = 1;

    public TopLuckUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.topluck.title", "» TopLuck — ratio minage"));
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public int getCategories() {
        return pages;
    }

    static String ratio(int stone, int ore) {
        if (ore <= 0) return "—";
        return String.valueOf(Math.max(1, Math.round(stone / (float) ore)));
    }

    private static int luckScore(UHCPlayer up) {
        return up.getMinedDiamond() * 100 + up.getMinedGold() * 10 + up.getMinedIron();
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 45, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        List<UHCPlayer> players = new ArrayList<>(UHCPlayerManager.get().getOnlineUHCPlayers());
        players.sort(Comparator.comparingInt(TopLuckUi::luckScore).reversed());

        String entryName = t(DynamicLang.of("menu.topluck.player-dummy.name", "§8┃ §e%pseudo%"));
        String entryLore = t(DynamicLang.of("menu.topluck.player-dummy.lore", ENTRY_LORE));

        pages = paginate(players, PER_PAGE,
                n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                (up, slot) -> {
                    Player online = up.getPlayer();
                    if (online == null) return;
                    ItemCreator icon = new ItemCreator(new ItemStack(Material.SKULL_ITEM, 1, (short) 3))
                            .setOwner(online.getName())
                            .setName(entryName.replace("%pseudo%", online.getName()));
                    icon.setLores(Arrays.asList(entryLore
                            .replace("%stone%", String.valueOf(up.getMinedStone()))
                            .replace("%diamond_ratio%", ratio(up.getMinedStone(), up.getMinedDiamond()))
                            .replace("%diamond%", String.valueOf(up.getMinedDiamond()))
                            .replace("%gold_ratio%", ratio(up.getMinedStone(), up.getMinedGold()))
                            .replace("%gold%", String.valueOf(up.getMinedGold()))
                            .replace("%iron_ratio%", ratio(up.getMinedStone(), up.getMinedIron()))
                            .replace("%iron%", String.valueOf(up.getMinedIron()))
                            .split("\n", -1)));

                    addItem(new ActionItem(slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            if (e.isRightClick()) {
                                getPlayer().closeInventory();
                                getPlayer().teleport(online.getLocation());
                                LangManager.get().send(CoreLang.COMMON_SPEC_TELEPORTED, getPlayer(),
                                        Map.of("%player%", online.getName()));
                                return;
                            }
                            new ModerationMenus.SpectatorActions(getPlayer(), online.getUniqueId()).open();
                        }
                    });
                });

        addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.LIGHT_BLUE),
                t(DynamicLang.of("menu.topluck.previous.name", "§8┃ §cPrécédent")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                previousCategory();
                open();
            }
        });

        addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.LIGHT_BLUE),
                t(DynamicLang.of("menu.topluck.next.name", "§8┃ §aSuivant")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                nextCategory();
                open();
            }
        });
    }
}
