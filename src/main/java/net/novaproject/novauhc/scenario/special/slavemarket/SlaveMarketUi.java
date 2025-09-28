package net.novaproject.novauhc.scenario.special.slavemarket;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.MessageUtils;
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
                new AnvilUi(getPlayer(), new SlaveMarketUi(getPlayer()), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        String enteredText = event.getName();
                        Player player = Bukkit.getPlayer(enteredText);
                        if (player == null) {
                            MessageUtils.sendInvalidArgument(getPlayer(), "Player introuvable");
                            return;
                        }
                        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
                        if (slave.getOwners().size() + 1 <= slave.getTeamPlaces().size()) {
                            slave.addOwner(uhcPlayer);
                        } else {
                            CommonString.DISABLE_ACTION.send(getPlayer());
                        }


                    }

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

        addMenu(10, new ItemCreator(Material.DIAMOND).setName("§8┃ §fNombre de §3Diamond §f: §3§l" + slave.getNbDiamond())
                .addLore("")
                .addLore("  §8┃ §fVous permet de §cmodifier")
                .addLore("  §8┃ §fle nombre de §3Diamond §f.")
                .addLore("  §8┃ §fdonner au §f " + Common.get().getMainColor() + "Owner.")
                .addLore(""), new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, slave.getNbDiamond(), 20, 100, this) {
            @Override
            public void onChange(int newValue) {
                slave.setNbDiamond(newValue);
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
            meta.setDisplayName(started ? "§8┃ §fAnnulé l' §cEnchère" : "§8┃ §fDémarré l' §aEnchère");
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
