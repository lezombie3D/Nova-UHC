package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LimiteStuffUi extends CustomInventory {

    public LimiteStuffUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.stuff.color"));
        addReturn(45, new GameUi(getPlayer()));

        ItemCreator diamond = new ItemCreator(Material.DIAMOND_CHESTPLATE)
                .setName("§8┃ §f" + Common.get().getMainColor() + "Limite de pièce en Diamant")
                .addLore("")
                .addLore(" §8» §fActuel : §3§l" + UHCManager.get().getDiamondArmor())
                .addLore("")
                .addLore(CommonString.CLICK_GAUCHE.getMessage() + "§8» §a§l+1")
                .addLore(CommonString.CLICK_DROITE.getMessage() + "§8» §c§l-1")
                .addLore("");
        addItem(new ActionItem(3, diamond.setAmount(UHCManager.get().getDiamondArmor())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick() && UHCManager.get().getDiamondArmor() < 4) {
                    UHCManager.get().setDiamondArmor(UHCManager.get().getDiamondArmor() + 1);
                    openAll();
                }
                if (e.isRightClick() && UHCManager.get().getDiamondArmor() > 0) {
                    UHCManager.get().setDiamondArmor(UHCManager.get().getDiamondArmor() - 1);
                    openAll();
                }

            }
        });
        ItemCreator protection = new ItemCreator(Material.ENCHANTED_BOOK)
                .setName("§8┃ §f" + Common.get().getMainColor() + "Limite de Protection en Diamant")
                .addLore("")
                .addLore(" §8» §fActuel : §3§l" + UHCManager.get().getProtectionMax())
                .addLore("")
                .addLore(CommonString.CLICK_GAUCHE.getMessage() + "§8» §a§l+1")
                .addLore(CommonString.CLICK_DROITE.getMessage() + "§8» §c§l-1")
                .addLore("");
        addItem(new ActionItem(5, protection.setAmount(UHCManager.get().getProtectionMax())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick() && UHCManager.get().getProtectionMax() < 4) {
                    UHCManager.get().setProtectionMax(UHCManager.get().getProtectionMax() + 1);
                    openAll();
                }
                if (e.isRightClick() && UHCManager.get().getProtectionMax() > 0) {
                    UHCManager.get().setProtectionMax(UHCManager.get().getProtectionMax() - 1);
                    openAll();
                }
            }

        });
        int i = 10;
        for (Enchants enchants : Enchants.values()) {
            addItem(new ActionItem(i, new ItemCreator(Material.ENCHANTED_BOOK)
                    .setName(enchants.getName())
                    .addLore("")
                    .addLore(" §8» §fActuel : §3§l" + enchants.getConfigValue())
                    .addLore("")
                    .addLore(CommonString.CLICK_GAUCHE.getMessage() + "§8» §a§l+1")
                    .addLore(CommonString.CLICK_DROITE.getMessage() + "§8» §c§l-1")
                    .addLore("")) {


                @Override
                public void onClick(InventoryClickEvent e) {
                    if (e.isLeftClick()) {
                        UHCPlayerManager.get().getOnlineUHCPlayers().forEach(uhcPlayer -> {
                            int current = uhcPlayer.getEnchantLimits().get(enchants);
                            int newValue = Math.min(current + 1, enchants.getMax());
                            uhcPlayer.setEnchantLimit(enchants, newValue);
                        });
                        enchants.addConfigValue();
                        openAll();
                    }

                    if (e.isRightClick()) {
                        UHCPlayerManager.get().getOnlineUHCPlayers().forEach(uhcPlayer -> {
                            int current = uhcPlayer.getEnchantLimits().get(enchants);
                            int newValue = Math.max(current - 1, enchants.getMin());
                            uhcPlayer.setEnchantLimit(enchants, newValue);
                        });
                        enchants.removeConfigValue();
                        openAll();
                    }
                }
            });
            switch (i) {
                case 16:
                    i = 19;
                    break;
                case 25:
                    i = 28;
                    break;
                case 34:
                    i = 37;
                    break;
                case 43:
                    i = 46;
                    break;
                default:
                    i++;
                    break;
            }
        }
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.stuff.title");
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
