package net.novaproject.novauhc.ui.player;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LimiteStuffbyPlayerUi extends CustomInventory {
    Player target;

    public LimiteStuffbyPlayerUi(Player player, Player target) {
        super(player);
        this.target = target;
    }

    @Override
    public void setup() {
        UHCPlayer targetPlayer = UHCPlayerManager.get().getPlayer(target);
        fillCorner(getConfig().getInt("menu.stuff.color"));

        ItemCreator diamondlimite = new ItemCreator(Material.DIAMOND).setAmount(getUHCPlayer().getLimite()).setName(ChatColor.AQUA + "Diamond Limit : " + formatValue(getUHCPlayer().getLimite()));
        ItemCreator diamond = new ItemCreator(Material.DIAMOND_CHESTPLATE)
                .setName("§8┃ §f" + Common.get().getMainColor() + "Limite de pièce en Diamant")
                .addLore("")
                .addLore(" §8» §fActuel : §3§l" + targetPlayer.getDiamondArmor())
                .addLore("")
                .addLore(CommonString.CLICK_LEFT.getMessage() + "§8» §a§l+1")
                .addLore(CommonString.CLICK_RIGHT.getMessage() + "§8» §c§l-1")
                .addLore("");
        addItem(new ActionItem(3, diamond.setAmount(targetPlayer.getDiamondArmor())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick() && targetPlayer.getDiamondArmor() < 4) {
                    targetPlayer.setDiamondArmor(targetPlayer.getDiamondArmor() + 1);
                    openAll();
                }
                if (e.isRightClick() && targetPlayer.getDiamondArmor() > 0) {
                    targetPlayer.setDiamondArmor(targetPlayer.getDiamondArmor() - 1);
                    openAll();
                }

            }
        });
        ItemCreator protection = new ItemCreator(Material.ENCHANTED_BOOK)
                .setName("§8┃ §f" + Common.get().getMainColor() + "Limite de Protection en Diamant")
                .addLore("")
                .addLore(" §8» §fActuel : §3§l" + targetPlayer.getProtectionMax())
                .addLore("")
                .addLore(CommonString.CLICK_LEFT.getMessage() + "§8» §a§l+1")
                .addLore(CommonString.CLICK_RIGHT.getMessage() + "§8» §c§l-1")
                .addLore("");
        addItem(new ActionItem(5, protection.setAmount(targetPlayer.getProtectionMax())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick() && targetPlayer.getProtectionMax() < 4) {
                    targetPlayer.setProtectionMax(targetPlayer.getProtectionMax() + 1);
                    openAll();
                }
                if (e.isRightClick() && targetPlayer.getProtectionMax() > 0) {
                    targetPlayer.setProtectionMax(targetPlayer.getProtectionMax() - 1);
                    openAll();
                }
            }

        });
        addMenu(49, diamondlimite, new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1, getUHCPlayer().getLimite(), 0, 0, this) {
            @Override
            public void onChange(int newValue) {
                targetPlayer.setLimite(newValue);
            }
        });
        int i = 10;
        for (Enchants enchants : getUHCPlayer().getEnchantLimits().keySet()) {
            addItem(new ActionItem(i, new ItemCreator(Material.ENCHANTED_BOOK)
                    .setName(enchants.getName())
                    .addLore("")
                    .addLore(" §8» §fActuel : §3§l" + getUHCPlayer().getEnchantLimits().get(enchants))
                    .addLore("")
                    .addLore(CommonString.CLICK_LEFT.getMessage() + "§8» §a§l+1")
                    .addLore(CommonString.CLICK_RIGHT.getMessage() + "§8» §c§l-1")
                    .addLore("")) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (e.isLeftClick()) {
                        int current = targetPlayer.getEnchantLimits().get(enchants);
                        int newValue = Math.min(current + 1, enchants.getMax());
                        targetPlayer.setEnchantLimit(enchants, newValue);
                    }
                    if (e.isRightClick()) {
                        int current = targetPlayer.getEnchantLimits().get(enchants);
                        int newValue = Math.max(current - 1, enchants.getMin());
                        targetPlayer.setEnchantLimit(enchants, newValue);
                    }
                    openAll();
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
        return getConfig().getString("menu.enchant.title") + target.getName();
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }


    private String formatValue(int value) {
        if (value == 0) {
            return ChatColor.RED + "Désactivé";
        }
        return ChatColor.GREEN + String.valueOf(value);
    }

    @FunctionalInterface
    private interface ValueGetter {
        int get();
    }

    @FunctionalInterface
    private interface ValueSetter {
        void set(int value);
    }
}
