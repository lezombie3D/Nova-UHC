package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class AutoReviveUi extends CustomInventory {

    public AutoReviveUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.revive-automatique.title", "» Revive automatique"));
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
        Player player = getPlayer();
        UHCManager uhc = UHCManager.get();

        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);
        UiItems.panes(this, DyeColor.RED, 0, 8, 26);

        addItem(new ActionItem(11, new ItemCreator(Material.DIAMOND_SWORD)
                .setName(t(DynamicLang.of("menu.revive-automatique.btn-11.name",
                        "§8┃ §cRevive jusqu'au PvP")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.revive-automatique.btn-11.lore",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fPermet d'activer ou désactiver la\n"
                                + " §c┃ §f§crésurrection automatique §favant le §cPvP\n"
                                + "\n"
                                + " §c☰ §fStatus revive : %revive_pvp%\n"
                                + "\n"
                                + "§7» §fClique pour §aactiver §f/ §cdésactiver"))
                        .replace("%revive_pvp%", UiItems.onOff(player, uhc.isAutoRevivePvp()))
                        .split("\n", -1)))
                .setGlow(uhc.isAutoRevivePvp())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                uhc.setAutoRevivePvp(!uhc.isAutoRevivePvp());
                openAll();
            }
        });

        addItem(new ActionItem(13, new ItemCreator(Material.NETHER_STAR)
                .setName(t(DynamicLang.of("menu.revive-automatique.btn-13.name",
                        "§8┃ §eRevive jusqu'aux Rôles")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.revive-automatique.btn-13.lore",
                        "§7» Accès §eHost\n"
                                + "\n"
                                + " §e┃ §fPermet d'activer ou désactiver la\n"
                                + " §e┃ §f§erésurrection automatique §favant les §eRôles\n"
                                + " §e┃ §7§o(si un Mode de Jeu à Rôles est actif)\n"
                                + "\n"
                                + " §e☰ §fStatus revive : %revive_roles%\n"
                                + "\n"
                                + "§7» §fClique pour §aactiver §f/ §cdésactiver"))
                        .replace("%revive_roles%", UiItems.onOff(player, uhc.isAutoReviveRoles()))
                        .split("\n", -1)))
                .setGlow(uhc.isAutoReviveRoles())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                uhc.setAutoReviveRoles(!uhc.isAutoReviveRoles());
                openAll();
            }
        });

        addItem(new ActionItem(15, new ItemCreator(Material.SPECKLED_MELON)
                .setName(t(DynamicLang.of("menu.revive-automatique.btn-15.name",
                        "§8┃ §dRevive jusqu'au FinalHeal")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.revive-automatique.btn-15.lore",
                        "§7» Accès §dHost\n"
                                + "\n"
                                + " §d┃ §fPermet d'activer ou désactiver la\n"
                                + " §d┃ §f§drésurrection automatique §favant le §dFinalHeal\n"
                                + "\n"
                                + " §d☰ §fStatus revive : %revive_finalheal%\n"
                                + "\n"
                                + "§7» §fClique pour §aactiver §f/ §cdésactiver"))
                        .replace("%revive_finalheal%", UiItems.onOff(player, uhc.isAutoReviveFinalHeal()))
                        .split("\n", -1)))
                .setGlow(uhc.isAutoReviveFinalHeal())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                uhc.setAutoReviveFinalHeal(!uhc.isAutoReviveFinalHeal());
                openAll();
            }
        });

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.RED),
                t(DynamicLang.of("menu.revive-automatique.btn-18.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new HostOptionsMenus.AdvancedOptions(player).open();
            }
        });
    }
}
