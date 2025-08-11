package net.novaproject.novauhc.scenario.special.slavemarket;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.Titles;
import net.novaproject.novauhc.utils.ui.AnvilUi;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;

public class SlaveMarketUi extends CustomInventory {
    private final SlaveMarket slave = SlaveMarket.get();

    public SlaveMarketUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCadre(0);
        addReturn(36, new ScenariosUi(getPlayer(), true));
        addItem(new ActionItem(16, new ItemCreator(Material.PAPER).setName("§2Ajouter un joueur")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        String enteredText = event.getName();
                        Player player = Bukkit.getPlayer(enteredText);
                        if (player == null) {
                            getPlayer().sendMessage(ChatColor.RED + "Joueur invalide");
                            return;
                        }
                        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
                        if (slave.getOwners().size() + 1 <= 8) {
                            slave.addOwner(uhcPlayer);
                        } else {
                            getPlayer().sendMessage(ChatColor.RED + "Impossible, il y a déjà 8 propriétaires !");
                        }


                    }
                    new SlaveMarketUi(getPlayer()).open();
                }).setSlot("Nom du joueur").open();

            }
        });
        int slot = 19;
        for (UHCPlayer uhcPlayer : slave.getOwners()) {
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwner(uhcPlayer.getPlayer().getName());
            meta.setDisplayName(ChatColor.GOLD + uhcPlayer.getPlayer().getName());
            skull.setItemMeta(meta);
            addItem(new ActionItem(slot, skull) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    slave.removeOwner(uhcPlayer);
                    openAll();
                }
            });
            switch (slot) {
                case 25:
                    slot = 31;
                    break;
                default:
                    slot++;
                    break;
            }
        }
        addItem(new ActionItem(10, new ItemCreator(Material.DIAMOND).setName("§2Nombre de diamant : " + slave.getNbDiamond()).setLores(Arrays.asList("", ChatColor.GREEN + "Ajouter des diamants", ChatColor.RED + "Retirés des diamants"))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int current = slave.getNbDiamond();

                if (e.isRightClick()) {
                    if (e.isShiftClick()) {
                        slave.setNbDiamond(current + 5);
                    } else {
                        slave.setNbDiamond(current + 1);
                    }
                    openAll();
                    return;
                }

                if (e.isLeftClick()) {
                    if (e.isShiftClick() && current >= 5) {
                        slave.setNbDiamond(current - 5);
                    } else if (!e.isShiftClick() && current >= 1) {
                        slave.setNbDiamond(current - 1);
                    }
                }
                openAll();
            }
        });
        addItem(new ActionItem(4, getWool(UHCManager.get().isStarted())) {
            @Override
            public void onClick(InventoryClickEvent e) {

                boolean started = slave.canBuy();
                if (started) {
                    slave.startEnchere();
                    openAll();
                } else if (started) {
                    slave.cancelAction();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        new Titles().sendTitle(player, ChatColor.RED + "Enchère annuler...", "", 8);
                    }

                }
                openAll();
            }
        });

    }

    private ItemStack getWool(boolean started) {
        Material material = Material.WOOL;
        byte color = started ? (byte) 14 : (byte) 5;

        ItemStack wool = new ItemStack(material, 1, color);
        ItemMeta meta = wool.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(started ? "§cAnnuler le lancement" : "§aLancer la partie");
            wool.setItemMeta(meta);
        }
        return wool;
    }


    @Override
    public String getTitle() {
        return ChatColor.GOLD + "Slave Market";
    }

    @Override
    public int getLines() {
        return 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }
}
