package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.ui.MenuHeads;
import net.novaproject.novauhc.ui.MenuHeads.Shape;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

public class LimiteStuffUi extends CustomInventory {

    private enum Tab {
        ARME("equipement-et-limites-arme"),
        ARMURE("equipement-et-limites-armure"),
        OUTILS("equipement-et-limites-outils");

        private final String menuId;

        Tab(String menuId) {
            this.menuId = menuId;
        }
    }

    private final Tab tab;

    public LimiteStuffUi(Player player) {
        this(player, Tab.ARME);
    }

    private LimiteStuffUi(Player player, Tab tab) {
        super(player);
        this.tab = tab;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName();
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(key("title"), "» Limite Enchantement"));
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
    public void setup() {
        UiItems.panes(this, DyeColor.PURPLE, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        nav(2, Material.IRON_SWORD, "btn-2", Tab.ARME, "§8┃ §cArme",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fPermet d'accéder aux enchantements de l'§cépée.\n"
                        + "\n"
                        + "§8» §fClique pour y §caccedez");
        nav(4, Material.DIAMOND_CHESTPLATE, "btn-4", Tab.ARMURE, "§8┃ §bArmure",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fPermet d'accéder aux enchantements de l'§bArmure.\n"
                        + "\n"
                        + "§8» §fClique pour y §baccedez");
        nav(6, Material.IRON_PICKAXE, "btn-6", Tab.OUTILS, "§8┃ §aOutils",
                "§7» Accès §aHost\n"
                        + "\n"
                        + " §a┃ §fPermet d'accéder aux enchantements de l'§aOutils.\n"
                        + "\n"
                        + "§8» §fClique pour y §aaccedez");

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(Shape.BACK, DyeColor.PURPLE),
                t(DynamicLang.of(key("btn-45.name"), "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new GameUi(getPlayer()).open();
            }
        });

        switch (tab) {
            case ARME -> setupArme();
            case ARMURE -> setupArmure();
            case OUTILS -> setupOutils();
        }
    }

    private void setupArme() {
        enchant(19, "btn-19", "§6", "§e", "Knockback", "%valeur_kb%", Enchants.KNOCKBACK);
        enchant(20, "btn-20", "§6", "§e", "Sharpness", "%valeur_sharpness%", Enchants.SHARPNESS);
        enchant(21, "btn-21", "§6", "§e", "Fire Aspect", "%valeur_fireaspect%", Enchants.FIRE_ASPECT);
        enchant(23, "btn-23", "§6", "§e", "Bane of Arthropods", "%valeur_bane%", Enchants.BANE_OF_ARTHROPODS);
        enchant(24, "btn-24", "§6", "§e", "Looting", "%valeur_looting%", Enchants.LOOTING);
        enchant(25, "btn-25", "§6", "§e", "Unbreaking", "%valeur_unbreaking%", Enchants.UNBREAKING);
        enchant(29, "btn-29", "§e", "§6", "Punch", "%valeur_punch%", Enchants.PUNCH);
        enchant(30, "btn-30", "§e", "§6", "Power", "%valeur_power%", Enchants.POWER);
        enchant(32, "btn-32", "§e", "§6", "Flame", "%valeur_flame%", Enchants.FLAME);
        enchant(33, "btn-33", "§e", "§6", "Infinity", "%valeur_infinity%", Enchants.INFINITY);

        enchant(34, "smite", "§8┃ §fChâtiment",
                "§7» Accès §fHost\n"
                        + "\n"
                        + " §f┃ §fNiveau maximum de Châtiment\n"
                        + " §f┃ §fautorisé sur une arme.\n"
                        + "\n"
                        + " §f☰ §fLimite : %valeur_smite%\n"
                        + "\n"
                        + "§8» §fClic gauche : §aajouter§f, clic droit : §cdiminuer§f.",
                "%valeur_smite%", Enchants.SMITE);

        label(22, Material.DIAMOND_SWORD, "btn-22", "« §6Enchantement d'Epée§r »");
        label(31, Material.BOW, "btn-31", "« §eEnchantement d'Arc§r »");
    }

    private void setupArmure() {
        UHCManager uhc = UHCManager.get();

        number(3, Material.DIAMOND_CHESTPLATE, "diamond-armor", "§8┃ §bArmure en diamant",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fNombre maximum de pièces d'armure\n"
                        + " §b┃ §fen diamant autorisées par joueur.\n"
                        + "\n"
                        + " §b☰ §fLimite : %diamond_armor%\n"
                        + "\n"
                        + "§8» §fCliquez pour §bmodifier§f.",
                "%diamond_armor%", uhc.getDiamondArmor(), v -> uhc.setDiamondArmor(v.intValue()));

        number(5, Material.ENCHANTED_BOOK, "protection-max", "§8┃ §dProtection maximale",
                "§7» Accès §dHost\n"
                        + "\n"
                        + " §d┃ §fNiveau de Protection maximum\n"
                        + " §d┃ §fautorisé sur une pièce d'armure.\n"
                        + "\n"
                        + " §d☰ §fLimite : %protection_max%\n"
                        + "\n"
                        + "§8» §fCliquez pour §dmodifier§f.",
                "%protection_max%", uhc.getProtectionMax(), v -> {
                    uhc.setProtectionMax(v.intValue());
                    UHCPlayerManager.get().getOnlineUHCPlayers()
                            .forEach(p -> p.setProtectionMax(v.intValue()));
                });

        enchant(19, "btn-19", "§9", "§e", "Fire Protection", "%valeur_fireprot%", Enchants.FIRE_PROTECTION);
        enchant(20, "btn-20", "§9", "§e", "Blast Protection", "%valeur_blastprot%", Enchants.BLAST_PROTECTION);
        enchant(21, "btn-21", "§9", "§e", "Projectile Protection", "%valeur_projprot%", Enchants.PROJECTILE_PROTECTION);
        enchant(23, "btn-23", "§9", "§e", "Protection", "%valeur_protection%", Enchants.PROTECTION_ENVIRONMENTAL);
        enchant(24, "btn-24", "§9", "§e", "Thorns", "%valeur_thorns%", Enchants.THORNS);
        enchant(25, "btn-25", "§9", "§e", "Unbreaking", "%valeur_unbreaking%", Enchants.UNBREAKING);
        enchant(28, "btn-28", "§5", "§6", "Aqua Affinity", "%valeur_aquaaffinity%", Enchants.AQUA_AFFINITY);
        enchant(29, "btn-29", "§5", "§6", "Respiration", "%valeur_respiration%", Enchants.RESPIRATION);
        enchant(33, "btn-33", "§3", "§6", "Depth Strider", "%valeur_depthstrider%", Enchants.DEPTH_STRIDERS);
        enchant(34, "btn-34", "§3", "§6", "Feather Falling", "%valeur_featherfalling%", Enchants.FEATHER_FALLING);

        label(22, Material.IRON_CHESTPLATE, "btn-22", "« §9Enchantement du Plastron§r »");
        label(30, Material.IRON_HELMET, "btn-30", "« §5Enchantement du Casque§r »");
        label(32, Material.IRON_BOOTS, "btn-32", "« §3Enchantement des bottes§r »");
    }

    private void setupOutils() {
        enchant(20, "btn-20", "§2", "§e", "Fortune", "%valeur_fortune%", Enchants.FORTUNE);
        enchant(21, "btn-21", "§2", "§e", "Efficiency", "%valeur_efficiency%", Enchants.EFFICIENCY);
        enchant(23, "btn-23", "§2", "§e", "Silk Touch", "%valeur_silktouch%", Enchants.SILK_TOUCH);
        enchant(24, "btn-24", "§2", "§e", "Unbreaking", "%valeur_unbreaking%", Enchants.UNBREAKING);
        enchant(30, "btn-30", "§b", "§6", "Luck Of The Sea", "%valeur_luckofthesea%", Enchants.LUCK_OF_THE_SEA);
        enchant(32, "btn-32", "§b", "§6", "Lure", "%valeur_lure%", Enchants.LURE);

        label(22, Material.IRON_AXE, "btn-22", "« §2Enchantement des Outils§r »");
        label(31, Material.FISHING_ROD, "btn-31", "« §bEnchantement de la Fishing Rod§r »");
    }

    private String key(String suffix) {
        return "menu." + tab.menuId + "." + suffix;
    }

    private ItemCreator icon(Material material, String id, String name, String lore,
                             Map<String, Object> vars) {
        ItemCreator item = new ItemCreator(material)
                .setName(t(DynamicLang.of(key(id + ".name"), name), vars));
        if (lore != null) {
            item.setLores(Arrays.asList(t(DynamicLang.of(key(id + ".lore"), lore), vars).split("\n", -1)));
        }
        return item;
    }

    private void label(int slot, Material material, String id, String name) {
        addItem(new StaticItem(slot, icon(material, id, name, null, Map.of())));
    }

    private void nav(int slot, Material material, String id, Tab target, String name, String lore) {
        addItem(new ActionItem(slot, icon(material, id, name, lore, Map.of())) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new LimiteStuffUi(getPlayer(), target).open();
            }
        });
    }

    private void enchant(int slot, String id, String color, String reset, String label,
                         String placeholder, Enchants enchants) {
        enchant(slot, id, "§8┃ " + color + label,
                "§7» Accès " + color + "Host\n"
                        + "\n"
                        + " " + color + "┃ §fGauche = §aAjouter§f.\n"
                        + " " + color + "┃ §fDroit = §cDiminuer§f.\n"
                        + " " + color + "┃ §fDrop = " + reset + "Reset§f.\n"
                        + "\n"
                        + color + "☰ §rLevel: §f" + placeholder,
                placeholder, enchants);
    }

    private void enchant(int slot, String id, String name, String lore,
                         String placeholder, Enchants enchants) {
        ItemCreator item = icon(Material.ENCHANTED_BOOK, id, name, lore,
                Map.of(placeholder, enchants.getConfigValue()));
        addItem(new ActionItem(slot, item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
                    enchants.resetConfigValue();
                } else if (e.isLeftClick()) {
                    enchants.addConfigValue();
                } else if (e.isRightClick()) {
                    enchants.removeConfigValue();
                } else {
                    return;
                }
                int value = enchants.getConfigValue();
                UHCPlayerManager.get().getOnlineUHCPlayers().forEach(p -> p.setEnchantLimit(enchants, value));
                openAll();
            }
        });
    }

    private void number(int slot, Material material, String id, String name, String lore,
                        String placeholder, int current, Consumer<Number> apply) {
        ItemCreator item = icon(material, id, name, lore, Map.of(placeholder, String.valueOf(current)));
        addItem(new ActionItem(slot, item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 4, 2, 1, 4, 2, 1, current, 0, 4,
                        LimiteStuffUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        apply.accept(newValue);
                    }
                }.open();
            }
        });
    }
}
