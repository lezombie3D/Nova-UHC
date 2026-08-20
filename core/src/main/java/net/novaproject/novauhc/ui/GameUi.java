package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.config.DropUi;
import net.novaproject.novauhc.ui.config.LimiteStuffUi;
import net.novaproject.novauhc.ui.config.PotionUi;
import net.novaproject.novauhc.ui.config.StuffUi;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;
import java.util.Map;

public class GameUi extends CustomInventory {

    public GameUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.configuration-de-partie.title", "» Option de la partie"));
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
        Player player = getPlayer();
        UHCManager uhc = UHCManager.get();

        Map<String, Object> timerPlaceholders = Map.of(
                "%pvp_timer%", TextUtils.getFormattedTime(uhc.getPvpTimer()),
                "%border_timer%", TextUtils.getFormattedTime(uhc.getBorderTimer()));
        Map<String, Object> diamondPlaceholders = Map.of(
                "%diamond_limit%", String.valueOf(uhc.getDiamondLimit()));

        UiItems.panes(this, DyeColor.BLUE, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        addItem(new ActionItem(2, new ItemCreator(Material.WATCH)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.timers.name", "§8┃ §aGestion des Timers")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.timers.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fCentralisez les timers généraux de la partie :\n"
                                + " §a┃ §fPvP, bordure et Final Heal.\n"
                                + "\n"
                                + " §a☰ §fPvP : %pvp_timer% · Bordure : %border_timer%\n"
                                + "\n"
                                + "§8» §fCliquez pour y §aaccéder§f."), timerPlaceholders).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new HostOptionsMenus.Timers(player).open();
            }
        });

        addItem(new ActionItem(4, new ItemCreator(Material.DIODE)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.advanced.name", "§8┃ §cOptions avancées")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.advanced.lore",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fAccédez aux réglages de démarrage, de combat\n"
                                + " §c┃ §fet de communication sans surcharger ce menu.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §caccéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new HostOptionsMenus.AdvancedOptions(player).open();
            }
        });

        addItem(new ActionItem(6, new ItemCreator(Material.BARRIER)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.border.name", "§8┃ §eBordure")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.border.lore",
                        "§7» Accès §eHost\n"
                                + "\n"
                                + " §e┃ §fModifiez ses tailles, sa vitesse,\n"
                                + " §e┃ §fses dégâts et sa zone tampon.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §eaccéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new BorderConfig(player).open();
            }
        });

        addItem(new ActionItem(10, new ItemCreator(Material.ENCHANTED_BOOK)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.enchants.name", "§8┃ §bLimite d’enchantements")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.enchants.lore",
                        "§7» Accès §bHost\n"
                                + "\n"
                                + " §b┃ §fDéfinissez les limites globales par équipement\n"
                                + " §b┃ §favec les onglets Arme, Armure et Outils.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §baccéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new LimiteStuffUi(player).open();
            }
        });

        addItem(new ActionItem(16, new ItemCreator(Material.POTION)
                .setDurability((short) 8201)
                .addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.effects.name", "§8┃ §dEffets globaux")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.effects.lore",
                        "§7» Accès §dHost\n"
                                + "\n"
                                + " §d┃ §fConfigurez les multiplicateurs globaux de Force,\n"
                                + " §d┃ §fRésistance, Vitesse et Critiques.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §daccéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new EffectsConfigUi(player).open();
            }
        });

        addItem(new ActionItem(22, new ItemCreator(Material.IRON_FENCE)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.moderation.name", "§8┃ §6Modération de la partie")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.moderation.lore",
                        "§7» Accès §6Host\n"
                                + "\n"
                                + " §6┃ §fCentralisez les outils Host et Co-Host\n"
                                + " §6┃ §fsans mélanger les réglages de gameplay.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §6accéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ModerationMenus.Game(player).open();
            }
        });

        addItem(new ActionItem(31, new ItemCreator(Material.WORKBENCH)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.craft-items.name", "§8┃ §2Craft & Items")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.craft-items.lore",
                        "§7» Accès §2Host\n"
                                + "\n"
                                + " §2┃ §fLimitez les objets craftables et obtenables\n"
                                + " §2┃ §fdans une interface commune avec recherche.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §2accéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new CraftItemsUi(player).open();
            }
        });

        addItem(new ActionItem(37, new ItemCreator(Material.GLASS_BOTTLE)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.potions.name", "§8┃ §3Limite de potions")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.potions.lore",
                        "§7» Accès §3Host\n"
                                + "\n"
                                + " §3┃ §fLimitez les familles, niveaux et durées\n"
                                + " §3┃ §fdes potions disponibles pendant la partie.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §3accéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new PotionUi(player).open();
            }
        });

        addItem(new ActionItem(43, new ItemCreator(Material.DIAMOND)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.diamonds.name", "§8┃ §4Limite de Diamant")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.diamonds.lore",
                        "§7» Accès §4Host\n"
                                + "\n"
                                + " §4┃ §fDéfinissez le nombre maximal de diamants\n"
                                + " §4┃ §fqu’un joueur peut miner.\n"
                                + "\n"
                                + " §4☰ §fLimite : %diamond_limit%\n"
                                + "\n"
                                + "§8» §fCliquez pour §4modifier§f."), diamondPlaceholders).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(player, 10, 5, 1, 10, 5, 1,
                        uhc.getDiamondLimit(), 0, Double.NaN, GameUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        uhc.setDiamondLimit(newValue.intValue());
                        UHCPlayerManager.get().getOnlineUHCPlayers()
                                .forEach(p -> p.setDiamondLimit(newValue.intValue()));
                    }
                }.open();
            }
        });

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.BLUE),
                t(DynamicLang.of("menu.configuration-de-partie.back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new DefaultUi(player).open();
            }
        });

        addItem(new ActionItem(47, new ItemCreator(Material.CHEST)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.inventories.name", "§8┃ §5Inventaires")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.inventories.lore",
                        "§7» Accès §5Host\n"
                                + "\n"
                                + " §5┃ §fGérez et prévisualisez dans le même écran\n"
                                + " §5┃ §fles inventaires de départ et de mort.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §5accéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new StuffUi(player, UHCManager.get().start, "start").open();
            }
        });

        addItem(new ActionItem(49, new ItemCreator(Material.BOOK)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.rules.name", "§8┃ §7Règles de la partie")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.rules.lore",
                        "§7» Accès §7Host\n"
                                + "\n"
                                + " §7┃ §fRelisez d’un coup d’œil tout ce que\n"
                                + " §7┃ §fcette configuration produit en jeu.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §7accéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new RulesUi(player, GameUi.this).open();
            }
        });

        addItem(new ActionItem(51, new ItemCreator(Material.APPLE)
                .setName(t(DynamicLang.of("menu.configuration-de-partie.drops.name", "§8┃ §8Taux de drop")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.configuration-de-partie.drops.lore",
                        "§7» Accès §8Host\n"
                                + "\n"
                                + " §8┃ §fModifiez les taux des ressources utiles\n"
                                + " §8┃ §fsans y mélanger les règles de combat.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §8accéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new DropUi(player).open();
            }
        });
    }
}
