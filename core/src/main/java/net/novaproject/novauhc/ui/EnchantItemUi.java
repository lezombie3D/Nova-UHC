package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class EnchantItemUi extends CustomInventory {

    private enum Tab {
        ARME("enchant-item-arme"),
        ARMURE("enchant-item-armure"),
        OUTILS("enchant-item-outils");

        private final String menuId;

        Tab(String menuId) {
            this.menuId = menuId;
        }
    }

    private final ItemStack working;
    private final Tab tab;

    public EnchantItemUi(Player player, ItemStack source) {
        this(player, source.clone(), Tab.ARME);
    }

    private EnchantItemUi(Player player, ItemStack working, Tab tab) {
        super(player);
        this.working = working;
        this.tab = tab;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + tab.menuId + ".title",
                tab == Tab.OUTILS ? "» Enchant items" : "» Enchant"));
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
    public String broadcastKey() {
        return getClass().getName() + ":" + tab.menuId;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.YELLOW, 0, 8, 45, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        switch (tab) {
            case ARME -> setupArme();
            case ARMURE -> setupArmure();
            case OUTILS -> setupOutils();
        }
    }

    private void setupArme() {
        tabButton(2, "btn-2", Material.IRON_SWORD, "§8┃ §cArme",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fPermet d'accéder aux enchantements de l'§cépée.\n"
                        + "\n"
                        + "§8» §fClique pour y §caccedez", Tab.ARME);
        tabButton(4, "btn-4", Material.DIAMOND_CHESTPLATE, "§8┃ §bArmure",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fPermet d'accéder aux enchantements de l'§bArmure.\n"
                        + "\n"
                        + "§8» §fClique pour y §baccedez", Tab.ARMURE);
        tabButton(6, "btn-6", Material.IRON_PICKAXE, "§8┃ §aOutils",
                "§7» Accès §aHost\n"
                        + "\n"
                        + " §a┃ §fPermet d'accéder aux enchantements de l'§aOutils.\n"
                        + "\n"
                        + "§8» §fClique pour y §aaccedez", Tab.OUTILS);

        preview(13);

        enchant(19, "btn-19", "§8┃ §6Knockback", bookLore("§6", "§e", "%valeur_kb%"),
                "%valeur_kb%", Enchantment.KNOCKBACK);
        enchant(20, "btn-20", "§8┃ §6Sharpness", bookLore("§6", "§e", "%valeur_sharpness%"),
                "%valeur_sharpness%", Enchantment.DAMAGE_ALL);
        enchant(21, "btn-21", "§8┃ §6Fire Aspect", bookLore("§6", "§e", "%valeur_fireaspect%"),
                "%valeur_fireaspect%", Enchantment.FIRE_ASPECT);
        enchant(23, "btn-23", "§8┃ §6Bane of Arthropods", bookLore("§6", "§e", "%valeur_bane%"),
                "%valeur_bane%", Enchantment.DAMAGE_ARTHROPODS);
        enchant(24, "btn-24", "§8┃ §6Looting", bookLore("§6", "§e", "%valeur_looting%"),
                "%valeur_looting%", Enchantment.LOOT_BONUS_MOBS);
        enchant(25, "btn-25", "§8┃ §6Unbreaking", bookLore("§6", "§e", "%valeur_unbreaking%"),
                "%valeur_unbreaking%", Enchantment.DURABILITY);
        enchant(29, "btn-29", "§8┃ §ePunch", bookLore("§e", "§6", "%valeur_punch%"),
                "%valeur_punch%", Enchantment.ARROW_KNOCKBACK);
        enchant(30, "btn-30", "§8┃ §ePower", bookLore("§e", "§6", "%valeur_power%"),
                "%valeur_power%", Enchantment.ARROW_DAMAGE);
        enchant(32, "btn-32", "§8┃ §eFlame", bookLore("§e", "§6", "%valeur_flame%"),
                "%valeur_flame%", Enchantment.ARROW_FIRE);
        enchant(33, "btn-33", "§8┃ §eInfinity", bookLore("§e", "§6", "%valeur_infinity%"),
                "%valeur_infinity%", Enchantment.ARROW_INFINITE);

        label(22, "btn-22", Material.DIAMOND_SWORD, "« §6Enchantement d'Epée§r »");
        label(31, "btn-31", Material.BOW, "« §eEnchantement d'Arc§r »");

        legacyUnbreakable();
        legacyRename();
        legacyValidate();
    }

    private void setupArmure() {
        tabButton(2, "btn-2", Material.IRON_SWORD, "§8┃ §cArme",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fPermet d'accéder aux enchantements de l'§cépée.\n"
                        + "\n"
                        + "§8» §fClique pour y §caccedez", Tab.ARME);
        tabButton(4, "btn-4", Material.DIAMOND_CHESTPLATE, "§8┃ §bArmure",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fPermet d'accéder aux enchantements de l'§bArmure.\n"
                        + "\n"
                        + "§8» §fClique pour y §baccedez", Tab.ARMURE);
        tabButton(6, "btn-6", Material.IRON_PICKAXE, "§8┃ §aOutils",
                "§7» Accès §aHost\n"
                        + "\n"
                        + " §a┃ §fPermet d'accéder aux enchantements de l'§aOutils.\n"
                        + "\n"
                        + "§8» §fClique pour y §aaccedez", Tab.OUTILS);

        preview(13);

        enchant(19, "btn-19", "§8┃ §9Fire Protection", bookLore("§9", "§e", "%valeur_fireprot%"),
                "%valeur_fireprot%", Enchantment.PROTECTION_FIRE);
        enchant(20, "btn-20", "§8┃ §9Blast Protection", bookLore("§9", "§e", "%valeur_blastprot%"),
                "%valeur_blastprot%", Enchantment.PROTECTION_EXPLOSIONS);
        enchant(21, "btn-21", "§8┃ §9Projectile Protection", bookLore("§9", "§e", "%valeur_projprot%"),
                "%valeur_projprot%", Enchantment.PROTECTION_PROJECTILE);
        enchant(23, "btn-23", "§8┃ §9Protection", bookLore("§9", "§e", "%valeur_protection%"),
                "%valeur_protection%", Enchantment.PROTECTION_ENVIRONMENTAL);
        enchant(24, "btn-24", "§8┃ §9Thorns", bookLore("§9", "§e", "%valeur_thorns%"),
                "%valeur_thorns%", Enchantment.THORNS);
        enchant(25, "btn-25", "§8┃ §9Unbreaking", bookLore("§9", "§e", "%valeur_unbreaking%"),
                "%valeur_unbreaking%", Enchantment.DURABILITY);
        enchant(28, "btn-28", "§8┃ §5Aqua Affinity", bookLore("§5", "§6", "%valeur_aquaaffinity%"),
                "%valeur_aquaaffinity%", Enchantment.WATER_WORKER);
        enchant(29, "btn-29", "§8┃ §5Respiration", bookLore("§5", "§6", "%valeur_respiration%"),
                "%valeur_respiration%", Enchantment.OXYGEN);
        enchant(33, "btn-33", "§8┃ §3Depth Strider", bookLore("§3", "§6", "%valeur_depthstrider%"),
                "%valeur_depthstrider%", Enchantment.DEPTH_STRIDER);
        enchant(34, "btn-34", "§8┃ §3Feather Falling", bookLore("§3", "§6", "%valeur_featherfalling%"),
                "%valeur_featherfalling%", Enchantment.PROTECTION_FALL);

        label(22, "btn-22", Material.IRON_CHESTPLATE, "« §9Enchantement du Plastron§r »");
        label(30, "btn-30", Material.IRON_HELMET, "« §5Enchantement du Casque§r »");
        label(32, "btn-32", Material.IRON_BOOTS, "« §3Enchantement des bottes§r »");

        legacyUnbreakable();
        legacyRename();
        legacyValidate();
    }

    private void setupOutils() {
        tabButton(2, "tab-weapon", Material.IRON_SWORD, "§8┃ §cArme",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fAffichez les enchantements d’arme et d’arc.\n"
                        + "\n"
                        + "§8» §fCliquez pour §cutiliser§f.", Tab.ARME);
        tabButton(4, "tab-armor", Material.DIAMOND_CHESTPLATE, "§8┃ §bArmure",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fAffichez les enchantements d’armure.\n"
                        + "\n"
                        + "§8» §fCliquez pour §butiliser§f.", Tab.ARMURE);
        tabButton(6, "tab-tools", Material.IRON_PICKAXE, "§8┃ §aOutils — actif",
                "§7» Accès §aHost\n"
                        + "\n"
                        + " §a┃ §fOnglet des outils actuellement affiché.\n"
                        + "", Tab.OUTILS);

        preview(13);

        enchant(19, "efficiency", "§8┃ §eEfficiency", toolLore("§e", "%item_enchant_efficiency%"),
                "%item_enchant_efficiency%", Enchantment.DIG_SPEED);
        enchant(20, "silk-touch", "§8┃ §dSilk Touch", toolLore("§d", "%item_enchant_silk_touch%"),
                "%item_enchant_silk_touch%", Enchantment.SILK_TOUCH);
        enchant(21, "fortune", "§8┃ §6Fortune", toolLore("§6", "%item_enchant_fortune%"),
                "%item_enchant_fortune%", Enchantment.LOOT_BONUS_BLOCKS);
        enchant(23, "unbreaking", "§8┃ §2Unbreaking", toolLore("§2", "%item_enchant_unbreaking%"),
                "%item_enchant_unbreaking%", Enchantment.DURABILITY);
        enchant(24, "luck-of-the-sea", "§8┃ §3Luck of the Sea", toolLore("§3", "%item_enchant_luck_of_the_sea%"),
                "%item_enchant_luck_of_the_sea%", Enchantment.LUCK);
        enchant(25, "lure", "§8┃ §4Lure", toolLore("§4", "%item_enchant_lure%"),
                "%item_enchant_lure%", Enchantment.LURE);

        unbreakable(47, "unbreakable", "§8┃ §5Unbreakable",
                "§7» Accès §5Host\n"
                        + "\n"
                        + " §5┃ §fRendez l’item incassable ou restaurez son usure normale.\n"
                        + "\n"
                        + " §5☰ §fStatut : %item_unbreakable_status%\n"
                        + "\n"
                        + "§8» §fCliquez pour §5activer ou désactiver§f.", "%item_unbreakable_status%");

        rename(51, "rename", "§8┃ §7Renommer l’item",
                "§7» Accès §7Host\n"
                        + "\n"
                        + " §7┃ §fSaisissez le nom affiché du nouvel item.\n"
                        + "\n"
                        + "§8» §fCliquez pour §7utiliser§f.");

        validate(49, "validate", "§8┃ §fValidation de l’item",
                "§7» Accès §fHost\n"
                        + "\n"
                        + " §f┃ §fEnregistrez toutes les modifications de l’item.\n"
                        + "\n"
                        + "§8» §fCliquez pour §futiliser§f.");
    }

    private void legacyUnbreakable() {
        unbreakable(47, "btn-47", "§8┃ §eUnbreakable",
                "§7» Accès §eHost\n"
                        + "\n"
                        + " §e┃ §fPermet de mettre l'item incassable.\n"
                        + "\n"
                        + "§e☰ §rStatut: %statut_unbreakable%\n"
                        + "\n"
                        + "§8» §fCliquez pour §emodifier§f.", "%statut_unbreakable%");
    }

    private void legacyRename() {
        rename(51, "btn-51", "§8┃ §eRenommer l'item",
                "§7» Accès §eHost\n"
                        + "\n"
                        + " §e┃ §fPermet de renommer un item.\n"
                        + "\n"
                        + "§8» §fCliquez pour §emodifier§f.");
    }

    private void legacyValidate() {
        validate(49, "btn-49", "§8┃ §eValidation de l'item§r",
                "§e☰ §rTout est §eprêt§r ?\n"
                        + "\n"
                        + "§8» §fClique pour §evalider.");
    }

    private static String bookLore(String color, String reset, String placeholder) {
        return "§7» Accès " + color + "Host\n"
                + "\n"
                + " " + color + "┃ §fGauche = §aAjouter§f.\n"
                + " " + color + "┃ §fDroit = §cDiminuer§f.\n"
                + " " + color + "┃ §fDrop = " + reset + "Reset§f.\n"
                + "\n"
                + color + "☰ §rLevel: §f" + placeholder;
    }

    private static String toolLore(String color, String placeholder) {
        return "§7» Accès " + color + "Host\n"
                + "\n"
                + " " + color + "┃ §fClic gauche : augmentez le niveau maximal.\n"
                + " " + color + "┃ §fClic droit : diminuez le niveau maximal.\n"
                + "\n"
                + " " + color + "☰ §fNiveau : " + placeholder + "\n"
                + "\n"
                + "§8» §fCliquez pour " + color + "modifier§f.";
    }

    private String name(String key, String def) {
        return t(DynamicLang.of("menu." + tab.menuId + "." + key + ".name", def));
    }

    private String lore(String key, String def) {
        return t(DynamicLang.of("menu." + tab.menuId + "." + key + ".lore", def));
    }

    private ItemCreator icon(Material material, String key, String defName, String defLore) {
        ItemCreator item = new ItemCreator(material).setName(name(key, defName));
        if (defLore != null) item.setLores(Arrays.asList(lore(key, defLore).split("\n", -1)));
        return item;
    }

    private void label(int slot, String key, Material material, String defName) {
        addItem(new StaticItem(slot, icon(material, key, defName, null)));
    }

    private void preview(int slot) {
        addItem(new StaticItem(slot, new ItemCreator(working.clone())));
    }

    private void tabButton(int slot, String key, Material material, String defName, String defLore, Tab target) {
        addItem(new ActionItem(slot, icon(material, key, defName, defLore)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new EnchantItemUi(getPlayer(), working, target).open();
            }
        });
    }

    private void enchant(int slot, String key, String defName, String defLore,
                         String placeholder, Enchantment enchantment) {
        String value = String.valueOf(working.getEnchantmentLevel(enchantment));
        ItemCreator item = new ItemCreator(Material.ENCHANTED_BOOK)
                .setName(name(key, defName).replace(placeholder, value));
        item.setLores(Arrays.asList(lore(key, defLore).replace(placeholder, value).split("\n", -1)));

        addItem(new ActionItem(slot, item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                int cap = enchantment.getMaxLevel() == 1 ? 1 : 10;
                int current = working.getEnchantmentLevel(enchantment);
                int next = e.isLeftClick() ? Math.min(current + 1, cap)
                        : e.isRightClick() ? Math.max(current - 1, 0)
                        : 0;
                working.removeEnchantment(enchantment);
                if (next > 0) working.addUnsafeEnchantment(enchantment, next);
                open();
            }
        });
    }

    private void unbreakable(int slot, String key, String defName, String defLore, String placeholder) {
        String value = UiItems.onOff(getPlayer(), isUnbreakable());
        ItemCreator item = new ItemCreator(Material.BARRIER)
                .setName(name(key, defName).replace(placeholder, value));
        item.setLores(Arrays.asList(lore(key, defLore).replace(placeholder, value).split("\n", -1)));

        addItem(new ActionItem(slot, item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ItemCreator(working).setUnbreakable(!isUnbreakable());
                openAll();
            }
        });
    }

    private boolean isUnbreakable() {
        return working.getItemMeta() != null && working.getItemMeta().spigot().isUnbreakable();
    }

    private void rename(int slot, String key, String defName, String defLore) {
        addItem(new ActionItem(slot, icon(Material.NAME_TAG, key, defName, defLore)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), EnchantItemUi.this, event -> {
                    if (event.getSlot() != AnvilUi.AnvilSlot.OUTPUT) return;
                    String raw = event.getName();
                    if (raw != null && !raw.trim().isEmpty()) {
                        new ItemCreator(working).setName(ChatColor.translateAlternateColorCodes('&', raw));
                    }
                    open();
                }).setSlot(displayName()).open();
            }
        });
    }

    private String displayName() {
        return working.getItemMeta() != null && working.getItemMeta().hasDisplayName()
                ? working.getItemMeta().getDisplayName()
                : working.getType().name();
    }

    private void validate(int slot, String key, String defName, String defLore) {
        ItemCreator item = UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.ACCEPT, DyeColor.YELLOW), name(key, defName),
                Arrays.asList(lore(key, defLore).split("\n", -1)));

        addItem(new ActionItem(slot, item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().setItemInHand(working.clone());
                getPlayer().updateInventory();
                LangManager.get().send(UiLang.ENCHANT_ITEM_APPLIED, getPlayer());
                getPlayer().closeInventory();
            }
        });
    }
}
