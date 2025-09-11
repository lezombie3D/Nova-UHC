package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class WhiteListUi extends CustomInventory {
    public WhiteListUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.whitelist.corner"));

        addReturn(45, new DefaultUi(getPlayer()));

        ItemStack disableWool = new ItemStack(Material.WOOL, 1, (byte) 14);
        ItemMeta disableMeta = disableWool.getItemMeta();
        if (disableMeta != null) {
            disableMeta.setDisplayName(CommonString.WHITELIST_DISABLE_BUTTON.getRawMessage());
            disableWool.setItemMeta(disableMeta);
        }

        ItemStack enableWool = new ItemStack(Material.WOOL, 1, (byte) 5);
        ItemMeta enableMeta = enableWool.getItemMeta();
        if (enableMeta != null) {
            enableMeta.setDisplayName(CommonString.WHITELIST_ENABLE_BUTTON.getRawMessage());
            enableWool.setItemMeta(enableMeta);
        }

        addItem(new ActionItem(7, disableWool) {
            @Override
            public void onClick(InventoryClickEvent e) {
                Bukkit.getServer().setWhitelist(false);
                CommonString.SUCCESSFUL_DESACTIVATION.send(getPlayer());
            }
        });

        addItem(new ActionItem(8, enableWool) {
            @Override
            public void onClick(InventoryClickEvent e) {
                Bukkit.getServer().setWhitelist(true);
                CommonString.SUCCESSFUL_ACTIVATION.send(getPlayer());
            }
        });

        addItem(new ActionItem(0, new ItemCreator(Material.PAPER).setName("§2Ajouter un joueur")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        String enteredText = event.getName();
                        Bukkit.getOfflinePlayer(enteredText).setWhitelisted(true);
                        CommonString.SUCCESSFUL_MODIFICATION.send(getPlayer());
                        openAll();
                        return;
                    }
                    new WhiteListUi(getPlayer()).open();
                }).setSlot("Nom du joueur").open();
            }
        });

        List<OfflinePlayer> whitelistedPlayers = new ArrayList<>();
        for (OfflinePlayer p : Bukkit.getWhitelistedPlayers()) {
            whitelistedPlayers.add(p);
        }

        int playersPerPage = 24;
        int totalCategories = (int) Math.ceil((double) whitelistedPlayers.size() / playersPerPage);

        if (totalCategories > 1) {
            addPage(4);
        }

        for (int i = 0; i < whitelistedPlayers.size(); i++) {
            OfflinePlayer p = whitelistedPlayers.get(i);

            int categoryForThisPlayer = (int) Math.ceil((double) (i + 1) / playersPerPage);
            int positionInCategory = i % playersPerPage;

            int slot = calculateSlot(positionInCategory);

            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(p.getName());
            meta.setDisplayName(ChatColor.GOLD + p.getName());
            skull.setItemMeta(meta);

            addItem(new ActionItem(categoryForThisPlayer, slot, skull) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (p.isWhitelisted()) {
                        new ConfirmMenu(getPlayer(),
                                "§cÊtes-vous sûr de vouloir retirer " + p.getName() + " de la Whitelist ?",
                                () -> {
                                    p.setWhitelisted(false);

                                    if (p.isOnline() && p.getPlayer() != null) {
                                        Player onlinePlayer = p.getPlayer();
                                        String kickMessage = CommonString.KICKED.getMessage();
                                        onlinePlayer.kickPlayer(kickMessage);
                                        Bukkit.broadcastMessage(CommonString.KICKED_MESSAGE.getMessage().replace("%player%", p.getName()));
                                    }

                                    openAll();
                                },
                                () -> {
                                    openAll();
                                }, new WhiteListUi(getPlayer())).open();
                    }
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
        int playersCount = Bukkit.getWhitelistedPlayers().size();
        int playersPerPage = 24;
        return Math.max(1, (int) Math.ceil((double) playersCount / playersPerPage));
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.whitelist.title");
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
