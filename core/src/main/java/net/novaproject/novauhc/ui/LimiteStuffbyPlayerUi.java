package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LimiteStuffbyPlayerUi extends CustomInventory {

    private static final String WEAPON = "limites-d-enchantements-du-joueur-arme";
    private static final String ARMOR = "limites-d-enchantements-du-joueur-armure";
    private static final String TOOLS = "limites-d-enchantements-du-joueur-outils";

    private record Tile(String id, int slot, String color, String reset, String label,
                        String placeholder, Enchants enchant) {
    }

    private record Deco(String id, int slot, Material material, String name) {
    }

    private static final Map<String, List<Tile>> TABS = Map.of(
            WEAPON, List.of(
                    new Tile("btn-21", 21, "§6", "§e", "Fire Aspect", "%valeur_fireaspect%", Enchants.FIRE_ASPECT),
                    new Tile("btn-30", 30, "§e", "§6", "Power", "%valeur_power%", Enchants.POWER),
                    new Tile("btn-20", 20, "§6", "§e", "Sharpness", "%valeur_sharpness%", Enchants.SHARPNESS),
                    new Tile("btn-24", 24, "§6", "§e", "Looting", "%valeur_looting%", Enchants.LOOTING),
                    new Tile("btn-29", 29, "§e", "§6", "Punch", "%valeur_punch%", Enchants.PUNCH),
                    new Tile("btn-33", 33, "§e", "§6", "Infinity", "%valeur_infinity%", Enchants.INFINITY),
                    new Tile("btn-23", 23, "§6", "§e", "Bane of Arthropods", "%valeur_bane%", Enchants.BANE_OF_ARTHROPODS),
                    new Tile("btn-32", 32, "§e", "§6", "Flame", "%valeur_flame%", Enchants.FLAME),
                    new Tile("btn-19", 19, "§6", "§e", "Knockback", "%valeur_kb%", Enchants.KNOCKBACK),
                    new Tile("btn-25", 25, "§6", "§e", "Unbreaking", "%valeur_unbreaking%", Enchants.UNBREAKING)),
            ARMOR, List.of(
                    new Tile("btn-21", 21, "§9", "§e", "Projectile Protection", "%valeur_projprot%", Enchants.PROJECTILE_PROTECTION),
                    new Tile("btn-20", 20, "§9", "§e", "Blast Protection", "%valeur_blastprot%", Enchants.BLAST_PROTECTION),
                    new Tile("btn-24", 24, "§9", "§e", "Thorns", "%valeur_thorns%", Enchants.THORNS),
                    new Tile("btn-29", 29, "§5", "§6", "Respiration", "%valeur_respiration%", Enchants.RESPIRATION),
                    new Tile("btn-33", 33, "§3", "§6", "Depth Strider", "%valeur_depthstrider%", Enchants.DEPTH_STRIDERS),
                    new Tile("btn-23", 23, "§9", "§e", "Protection", "%valeur_protection%", Enchants.PROTECTION_ENVIRONMENTAL),
                    new Tile("btn-19", 19, "§9", "§e", "Fire Protection", "%valeur_fireprot%", Enchants.FIRE_PROTECTION),
                    new Tile("btn-25", 25, "§9", "§e", "Unbreaking", "%valeur_unbreaking%", Enchants.UNBREAKING),
                    new Tile("btn-28", 28, "§5", "§6", "Aqua Affinity", "%valeur_aquaaffinity%", Enchants.AQUA_AFFINITY),
                    new Tile("btn-34", 34, "§3", "§6", "Feather Falling", "%valeur_featherfalling%", Enchants.FEATHER_FALLING)),
            TOOLS, List.of(
                    new Tile("btn-21", 21, "§2", "§e", "Efficiency", "%valeur_efficiency%", Enchants.EFFICIENCY),
                    new Tile("btn-20", 20, "§2", "§e", "Fortune", "%valeur_fortune%", Enchants.FORTUNE),
                    new Tile("btn-24", 24, "§2", "§e", "Unbreaking", "%valeur_unbreaking%", Enchants.UNBREAKING),
                    new Tile("btn-23", 23, "§2", "§e", "Silk Touch", "%valeur_silktouch%", Enchants.SILK_TOUCH),
                    new Tile("btn-32", 32, "§b", "§6", "Lure", "%valeur_lure%", Enchants.LURE),
                    new Tile("btn-30", 30, "§b", "§6", "Luck Of The Sea", "%valeur_luckofthesea%", Enchants.LUCK_OF_THE_SEA)));

    private static final Map<String, List<Deco>> DECOS = Map.of(
            WEAPON, List.of(
                    new Deco("btn-22", 22, Material.DIAMOND_SWORD, "« §6Enchantement d'Epée§r »"),
                    new Deco("btn-31", 31, Material.BOW, "« §eEnchantement d'Arc§r »")),
            ARMOR, List.of(
                    new Deco("btn-30", 30, Material.IRON_HELMET, "« §5Enchantement du Casque§r »"),
                    new Deco("btn-32", 32, Material.IRON_BOOTS, "« §3Enchantement des bottes§r »"),
                    new Deco("btn-22", 22, Material.IRON_CHESTPLATE, "« §9Enchantement du Plastron§r »")),
            TOOLS, List.of(
                    new Deco("btn-22", 22, Material.IRON_AXE, "« §2Enchantement des Outils§r »"),
                    new Deco("btn-31", 31, Material.FISHING_ROD, "« §bEnchantement de la Fishing Rod§r »")));

    private final Player target;
    private final String tab;

    public LimiteStuffbyPlayerUi(Player player, Player target) {
        this(player, target, WEAPON);
    }

    private LimiteStuffbyPlayerUi(Player player, Player target, String tab) {
        super(player);
        this.target = target;
        this.tab = tab;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + tab + ".title", "» Enchant : %player%"),
                Map.of("%player%", target.getName()));
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
        return getClass().getName() + ":" + tab;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);
        UiItems.panes(this, DyeColor.PURPLE, 0, 8, 53);

        nav("btn-2", 2, Material.IRON_SWORD, "§c", "Arme", "épée", WEAPON);
        nav("btn-4", 4, Material.DIAMOND_CHESTPLATE, "§b", "Armure", "Armure", ARMOR);
        nav("btn-6", 6, Material.IRON_PICKAXE, "§a", "Outils", "Outils", TOOLS);

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.PURPLE),
                t(DynamicLang.of("menu." + tab + ".btn-45.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new PlayerLimitsMenus.Hub(getPlayer(), target).open();
            }
        });

        for (Deco deco : DECOS.get(tab)) {
            addItem(new StaticItem(deco.slot(), new ItemCreator(deco.material())
                    .setName(t(DynamicLang.of("menu." + tab + "." + deco.id() + ".name", deco.name())))));
        }

        UHCPlayer uhcTarget = UHCPlayerManager.get().getPlayer(target);
        for (Tile tile : TABS.get(tab)) enchant(tile, uhcTarget);
    }

    private void nav(String id, int slot, Material material, String color, String label,
                     String subject, String destination) {
        ItemCreator icon = new ItemCreator(material)
                .setName(t(DynamicLang.of("menu." + tab + "." + id + ".name", "§8┃ " + color + label)))
                .setLores(Arrays.asList(t(DynamicLang.of("menu." + tab + "." + id + ".lore",
                        "§7» Accès " + color + "Host\n"
                                + "\n"
                                + " " + color + "┃ §fPermet d'accéder aux enchantements de l'" + color + subject + ".\n"
                                + "\n"
                                + "§8» §fClique pour y " + color + "accedez")).split("\n", -1)));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new LimiteStuffbyPlayerUi(getPlayer(), target, destination).open();
            }
        });
    }

    private void enchant(Tile tile, UHCPlayer uhcTarget) {
        String value = uhcTarget == null
                ? tile.placeholder()
                : String.valueOf(uhcTarget.getEnchantLimits().get(tile.enchant()));

        String name = t(DynamicLang.of("menu." + tab + "." + tile.id() + ".name",
                "§8┃ " + tile.color() + tile.label())).replace(tile.placeholder(), value);

        String lore = t(DynamicLang.of("menu." + tab + "." + tile.id() + ".lore",
                "§7» Accès " + tile.color() + "Host\n"
                        + "\n"
                        + " " + tile.color() + "┃ §fGauche = §aAjouter§f.\n"
                        + " " + tile.color() + "┃ §fDroit = §cDiminuer§f.\n"
                        + " " + tile.color() + "┃ §fDrop = " + tile.reset() + "Reset§f.\n"
                        + "\n"
                        + tile.color() + "☰ §rLevel: §f" + tile.placeholder())).replace(tile.placeholder(), value);

        ItemCreator icon = new ItemCreator(Material.ENCHANTED_BOOK)
                .setName(name)
                .setLores(Arrays.asList(lore.split("\n", -1)));

        addItem(new ActionItem(tile.slot(), icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (uhcTarget == null) return;
                Enchants enchant = tile.enchant();
                int current = uhcTarget.getEnchantLimits().get(enchant);
                if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
                    uhcTarget.setEnchantLimit(enchant, enchant.getConfigValue());
                } else if (e.isLeftClick()) {
                    uhcTarget.setEnchantLimit(enchant, Math.min(current + 1, enchant.getMax()));
                } else if (e.isRightClick()) {
                    uhcTarget.setEnchantLimit(enchant, Math.max(current - 1, enchant.getMin()));
                }
                openAll();
            }
        });
    }
}
