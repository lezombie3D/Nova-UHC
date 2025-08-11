package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

public class LimiteStuffUi extends CustomInventory {

    public LimiteStuffUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCorner(getConfig().getInt("menu.stuff.color"));
        addReturn(45, new GameUi(getPlayer()));
        initializeItems();
        ItemCreator diamond = new ItemCreator(Material.DIAMOND_CHESTPLATE)
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("").setName(ChatColor.AQUA + "Limites de piéce " + UHCManager.get().getDiamondArmor());
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
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic gauche pour " + ChatColor.GREEN + "augmenter")
                .addLore(ChatColor.YELLOW + "► Clic droit pour " + ChatColor.RED + "diminuer")
                .addLore("").setName(ChatColor.AQUA + "Protection Max " + UHCManager.get().getProtectionMax());
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

    private void initializeItems() {
        createEnchantmentItem(10, "Protection", Enchantment.PROTECTION_ENVIRONMENTAL,
                () -> UHCManager.get().getProtection(),
                (value) -> UHCManager.get().setProtection(value),
                4);

        createEnchantmentItem(11, "Protection Feu", Enchantment.PROTECTION_FIRE,
                () -> UHCManager.get().getFireProtection(),
                (value) -> UHCManager.get().setFireProtection(value),
                4);

        createEnchantmentItem(12, "Protection Projectile", Enchantment.PROTECTION_PROJECTILE,
                () -> UHCManager.get().getProjectileProtection(),
                (value) -> UHCManager.get().setProjectileProtection(value),
                4);

        createEnchantmentItem(13, "Protection Explosion", Enchantment.PROTECTION_EXPLOSIONS,
                () -> UHCManager.get().getBlastProtection(),
                (value) -> UHCManager.get().setBlastProtection(value),
                4);

        createEnchantmentItem(14, "Protection Chute", Enchantment.PROTECTION_FALL,
                () -> UHCManager.get().getFeatherFalling(),
                (value) -> UHCManager.get().setFeatherFalling(value),
                4);
        createEnchantmentItem(15, "Depth Strider", Enchantment.DEPTH_STRIDER,
                () -> UHCManager.get().getDepthStrider(),
                (value) -> UHCManager.get().setDepthStrider(value),
                3);

        // Épée et Combat (Ligne 2: 19-25)
        createEnchantmentItem(16, "Sharpness", Enchantment.DAMAGE_ALL,
                () -> UHCManager.get().getSharpness(),
                (value) -> UHCManager.get().setSharpness(value),
                5);

        createEnchantmentItem(19, "Smite", Enchantment.DAMAGE_UNDEAD,
                () -> UHCManager.get().getSmite(),
                (value) -> UHCManager.get().setSmite(value),
                5);

        createEnchantmentItem(20, "Bane of Arthropods", Enchantment.DAMAGE_ARTHROPODS,
                () -> UHCManager.get().getBaneOfArthropods(),
                (value) -> UHCManager.get().setBaneOfArthropods(value),
                5);

        createEnchantmentItem(21, "Knockback", Enchantment.KNOCKBACK,
                () -> UHCManager.get().getKnockback(),
                (value) -> UHCManager.get().setKnockback(value),
                2);

        createEnchantmentItem(22, "Fire Aspect", Enchantment.FIRE_ASPECT,
                () -> UHCManager.get().getFireAspect(),
                (value) -> UHCManager.get().setFireAspect(value),
                2);

        createEnchantmentItem(23, "Looting", Enchantment.LOOT_BONUS_MOBS,
                () -> UHCManager.get().getLooting(),
                (value) -> UHCManager.get().setLooting(value),
                3);

        // Arc et Arbalète (Ligne 3: 28-34)
        createEnchantmentItem(24, "Power", Enchantment.ARROW_DAMAGE,
                () -> UHCManager.get().getPower(),
                (value) -> UHCManager.get().setPower(value),
                5);

        createEnchantmentItem(25, "Punch", Enchantment.ARROW_KNOCKBACK,
                () -> UHCManager.get().getPunch(),
                (value) -> UHCManager.get().setPunch(value),
                2);

        createEnchantmentItem(28, "Flame", Enchantment.ARROW_FIRE,
                () -> UHCManager.get().getFlame(),
                (value) -> UHCManager.get().setFlame(value),
                1);

        createEnchantmentItem(29, "Infinity", Enchantment.ARROW_INFINITE,
                () -> UHCManager.get().getInfinity(),
                (value) -> UHCManager.get().setInfinity(value),
                1);

        // Outils (Ligne 4: 37-43)
        createEnchantmentItem(30, "Efficiency", Enchantment.DIG_SPEED,
                () -> UHCManager.get().getEfficiency(),
                (value) -> UHCManager.get().setEfficiency(value),
                5);

        createEnchantmentItem(31, "Silk Touch", Enchantment.SILK_TOUCH,
                () -> UHCManager.get().getSilkTouch(),
                (value) -> UHCManager.get().setSilkTouch(value),
                1);

        createEnchantmentItem(32, "Fortune", Enchantment.LOOT_BONUS_BLOCKS,
                () -> UHCManager.get().getFortune(),
                (value) -> UHCManager.get().setFortune(value),
                3);

        createEnchantmentItem(33, "Unbreaking", Enchantment.DURABILITY,
                () -> UHCManager.get().getUnbreaking(),
                (value) -> UHCManager.get().setUnbreaking(value),
                3);

        createEnchantmentItem(34, "Thorns", Enchantment.THORNS,
                () -> UHCManager.get().getThorns(),
                (value) -> UHCManager.get().setThorns(value),
                3);

        createEnchantmentItem(37, "Respiration", Enchantment.OXYGEN,
                () -> UHCManager.get().getRespiration(),
                (value) -> UHCManager.get().setRespiration(value),
                3);

        createEnchantmentItem(38, "Aqua Affinity", Enchantment.WATER_WORKER,
                () -> UHCManager.get().getAquaAffinity(),
                (value) -> UHCManager.get().setAquaAffinity(value),
                1);
        createEnchantmentItem(39, "Luck of the Sea", Enchantment.LUCK,
                () -> UHCManager.get().getLuckOfTheSea(),
                (value) -> UHCManager.get().setLuckOfTheSea(value),
                3);

        createEnchantmentItem(40, "Lure", Enchantment.LURE,
                () -> UHCManager.get().getLure(),
                (value) -> UHCManager.get().setLure(value),
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
                .addLore("").addItemFlags(ItemFlag.HIDE_ENCHANTS);

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
