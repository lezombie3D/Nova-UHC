package net.novaproject.novauhc.ui.player;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
        fillCorner(14);
        initializeItems();
        ItemCreator diamondlimite = new ItemCreator(Material.DIAMOND).setAmount(getUHCPlayer().getLimite()).setName(ChatColor.AQUA + "Diamond Limit : " + formatValue(getUHCPlayer().getLimite()));
        ItemCreator diamond = new ItemCreator(Material.DIAMOND_CHESTPLATE)
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("").setName(ChatColor.AQUA + "Limites de piéce " + targetPlayer.getDiamondArmor());
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
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("").setName(ChatColor.AQUA + "Protection Max " + targetPlayer.getProtectionMax());
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
        addItem(new ActionItem(49, diamondlimite) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isLeftClick() && targetPlayer.getProtectionMax() < 4) {
                    targetPlayer.setLimite(targetPlayer.getLimite() + 1);
                    openAll();
                }
                if (e.isRightClick() && targetPlayer.getLimite() > 0) {
                    targetPlayer.setLimite(targetPlayer.getLimite() - 1);
                    openAll();
                }
            }
        });
    }

    @Override
    public String getTitle() {
        return "§6Limite d'enchantement : " + target.getName();
    }

    @Override
    public int getLines() {
        return 6;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    private void initializeItems() {
        UHCPlayer targetPlayer = UHCPlayerManager.get().getPlayer(target);
        createEnchantmentItem(10, "Protection", Enchantment.PROTECTION_ENVIRONMENTAL,
                () -> targetPlayer.getProtection(),
                (value) -> targetPlayer.setProtection(value),
                4);

        createEnchantmentItem(11, "Protection Feu", Enchantment.PROTECTION_FIRE,
                () -> targetPlayer.getFireProtection(),
                (value) -> targetPlayer.setFireProtection(value),
                4);

        createEnchantmentItem(12, "Protection Projectile", Enchantment.PROTECTION_PROJECTILE,
                () -> targetPlayer.getProjectileProtection(),
                (value) -> targetPlayer.setProjectileProtection(value),
                4);

        createEnchantmentItem(13, "Protection Explosion", Enchantment.PROTECTION_EXPLOSIONS,
                () -> targetPlayer.getBlastProtection(),
                (value) -> targetPlayer.setBlastProtection(value),
                4);

        createEnchantmentItem(14, "Protection Chute", Enchantment.PROTECTION_FALL,
                () -> targetPlayer.getFeatherFalling(),
                (value) -> targetPlayer.setFeatherFalling(value),
                4);
        createEnchantmentItem(15, "Depth Strider", Enchantment.DEPTH_STRIDER,
                () -> targetPlayer.getDepthStrider(),
                (value) -> targetPlayer.setDepthStrider(value),
                3);

        // Épée et Combat (Ligne 2: 19-25)
        createEnchantmentItem(16, "Sharpness", Enchantment.DAMAGE_ALL,
                () -> targetPlayer.getSharpness(),
                (value) -> targetPlayer.setSharpness(value),
                5);

        createEnchantmentItem(19, "Smite", Enchantment.DAMAGE_UNDEAD,
                () -> targetPlayer.getSmite(),
                (value) -> targetPlayer.setSmite(value),
                5);

        createEnchantmentItem(20, "Bane of Arthropods", Enchantment.DAMAGE_ARTHROPODS,
                () -> targetPlayer.getBaneOfArthropods(),
                (value) -> targetPlayer.setBaneOfArthropods(value),
                5);

        createEnchantmentItem(21, "Knockback", Enchantment.KNOCKBACK,
                () -> targetPlayer.getKnockback(),
                (value) -> targetPlayer.setKnockback(value),
                2);

        createEnchantmentItem(22, "Fire Aspect", Enchantment.FIRE_ASPECT,
                () -> targetPlayer.getFireAspect(),
                (value) -> targetPlayer.setFireAspect(value),
                2);

        createEnchantmentItem(23, "Looting", Enchantment.LOOT_BONUS_MOBS,
                () -> targetPlayer.getLooting(),
                (value) -> targetPlayer.setLooting(value),
                3);

        // Arc et Arbalète (Ligne 3: 28-34)
        createEnchantmentItem(24, "Power", Enchantment.ARROW_DAMAGE,
                () -> targetPlayer.getPower(),
                (value) -> targetPlayer.setPower(value),
                5);

        createEnchantmentItem(25, "Punch", Enchantment.ARROW_KNOCKBACK,
                () -> targetPlayer.getPunch(),
                (value) -> targetPlayer.setPunch(value),
                2);

        createEnchantmentItem(28, "Flame", Enchantment.ARROW_FIRE,
                () -> targetPlayer.getFlame(),
                (value) -> targetPlayer.setFlame(value),
                1);

        createEnchantmentItem(29, "Infinity", Enchantment.ARROW_INFINITE,
                () -> targetPlayer.getInfinity(),
                (value) -> targetPlayer.setInfinity(value),
                1);

        // Outils (Ligne 4: 37-43)
        createEnchantmentItem(30, "Efficiency", Enchantment.DIG_SPEED,
                () -> targetPlayer.getEfficiency(),
                (value) -> targetPlayer.setEfficiency(value),
                5);

        createEnchantmentItem(31, "Silk Touch", Enchantment.SILK_TOUCH,
                () -> targetPlayer.getSilkTouch(),
                (value) -> targetPlayer.setSilkTouch(value),
                1);

        createEnchantmentItem(32, "Fortune", Enchantment.LOOT_BONUS_BLOCKS,
                () -> targetPlayer.getFortune(),
                (value) -> targetPlayer.setFortune(value),
                3);

        createEnchantmentItem(33, "Unbreaking", Enchantment.DURABILITY,
                () -> targetPlayer.getUnbreaking(),
                (value) -> targetPlayer.setUnbreaking(value),
                3);

        createEnchantmentItem(34, "Thorns", Enchantment.THORNS,
                () -> targetPlayer.getThorns(),
                (value) -> targetPlayer.setThorns(value),
                3);

        createEnchantmentItem(37, "Respiration", Enchantment.OXYGEN,
                () -> targetPlayer.getRespiration(),
                (value) -> targetPlayer.setRespiration(value),
                3);

        createEnchantmentItem(38, "Aqua Affinity", Enchantment.WATER_WORKER,
                () -> targetPlayer.getAquaAffinity(),
                (value) -> targetPlayer.setAquaAffinity(value),
                1);
        createEnchantmentItem(39, "Luck of the Sea", Enchantment.LUCK,
                () -> targetPlayer.getLuckOfTheSea(),
                (value) -> targetPlayer.setLuckOfTheSea(value),
                3);

        createEnchantmentItem(40, "Lure", Enchantment.LURE,
                () -> targetPlayer.getLure(),
                (value) -> targetPlayer.setLure(value),
                3);
    }

    private void createEnchantmentItem(int slot, String name, Enchantment enchant,
                                       ValueGetter getter, ValueSetter setter, int maxValue) {

        ItemCreator item = new ItemCreator(Material.ENCHANTED_BOOK)
                .addEnchantment(enchant, 1)
                .setName(ChatColor.GOLD + name)
                .addLore("")
                .addLore(ChatColor.GRAY + "Niveau maximum: " + formatValue(getter.get()))
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("");

        addItem(new ActionItem(slot, item.setAmount(getter.get())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int currentValue = getter.get();
                if (e.getClick().isLeftClick() && currentValue < maxValue) {
                    setter.set(currentValue + 1);
                }
                if (e.getClick().isRightClick() && currentValue > 0) {
                    setter.set(currentValue - 1);
                }
                openAll();
            }
        });
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
