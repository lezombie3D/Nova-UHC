package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.UUID;

public class PreconfigUi extends CustomInventory {

    List<String> configNames = listConfigs(getPlayer().getUniqueId());
    private final CustomInventory parent;
    private final int index = 0;

    public PreconfigUi(Player player, CustomInventory parent) {
        super(player);
        this.parent = parent;
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.preconfig.corner"));
        ItemCreator nothing = new ItemCreator(Material.BARRIER).setName(ChatColor.RED + "Aucune configuration");
        addReturn(45, new DefaultUi(getPlayer()));
        int configperpage = 24;
        if (configNames != null) {
            int totalCategories = (int) Math.ceil((double) configNames.size() / configperpage);

            if (totalCategories > 1) {
                addPage(4);
            }

            for (int i = 0; i < configNames.size(); i++) {
                String configName = configNames.get(i);
                int category = (i / configperpage) + 1;
                int slot = calculateSlot(i % configperpage);

                ItemCreator item = new ItemCreator(Material.PAPER)
                        .setName("§8┃ §f" + configName)
                        .addLore("")
                        .addLore(" §8» §fAccès §f: §6§lHost")
                        .addLore("")
                        .addLore(CommonString.CLICK_GAUCHE.getMessage() + "§8» §a§lCharger")
                        .addLore(CommonString.CLICK_DROITE.getMessage() + "§8» §c§lSupprimer")
                        .addLore("");

                addItem(new ActionItem(category, slot, item) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        if (e.isRightClick()) {
                            new ConfirmMenu(getPlayer(),
                                    "Êtes-vous sûr de vouloir supprimer la configuration " + configName + " ?",
                                    () -> {
                                        getPlayer().performCommand("config delete " + configName);
                                    },
                                    () -> {
                                    }, new PreconfigUi(getPlayer(), parent)).open();
                        } else {
                            new ConfirmMenu(getPlayer(),
                                    "Êtes-vous sûr de vouloir charger la configuration " + configName + " ?", () -> {
                                getPlayer().performCommand("config load " + configName);

                            }, () -> {

                            }, new PreconfigUi(getPlayer(), parent)).open();
                        }

                    }
                });
            }
        } else {
            addItem(new StaticItem(22, nothing));
        }
        addItem(new ActionItem(0, new ItemCreator(Material.PAPER)
                .setName("§8┃ §fAjoutez une §e§lconfiguration")
                .addLore("")
                .addLore(" §8» §fAccès §f: §6§lHost")
                .addLore("")
                .addLore("  §8┃ §fPermet d'ajouter une §e§lConfiguration")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        String enteredText = event.getName();
                        getPlayer().performCommand("config save " + enteredText);
                    }
                    new WhiteListUi(getPlayer()).open();
                }).setSlot("§fNom de la §e§lConfiguration").open();
            }
        });
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.preconfig.title");
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    private int calculateSlot(int position) {
        int row = position / 7;
        int col = position % 7;
        return 10 + col + (row * 9);
    }

    @Override
    public int getCategories() {
        int totalConfigs = configNames.size();
        int configsPerPage = 24;
        return (int) Math.ceil((double) totalConfigs / configsPerPage);
    }

    private List<String> listConfigs(UUID playerUUID) {
        List<String> configNames = Main.getDatabaseManager().getPlayerUHCConfigNames(playerUUID);

        if (configNames.isEmpty()) {

            return null;
        }
        return configNames;
    }

}
