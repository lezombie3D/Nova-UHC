package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class PreconfigUi extends CustomInventory {

    private static final int ROW_FROM = 1;
    private static final int ROW_TO = 4;
    private static final int COL_FROM = 1;
    private static final int COL_TO = 7;
    private static final boolean CENTERED = false;
    private static final int PER_PAGE = MenuGrid.gridCapacity(ROW_FROM, ROW_TO, COL_FROM, COL_TO);

    private static final Map<UUID, List<String>> LOADED = new HashMap<>();

    private final CustomInventory parent;
    private final List<String> configNames;
    private final String menuId;
    private int pages = 1;

    public PreconfigUi(Player player, CustomInventory parent) {
        super(player);
        this.parent = parent;
        this.configNames = LOADED.remove(player.getUniqueId());
        this.menuId = configNames == null
                ? "gestion-des-configurations-chargement"
                : configNames.isEmpty() ? "gestion-des-configurations-vide" : "gestion-des-configurations";
        if (this.configNames == null) loadConfigsAsync();
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + menuId + ".title", "» Gestion des Configurations"));
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
    public int getCategories() {
        return pages;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName() + ":" + menuId;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.ORANGE, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        addItem(new ActionItem(0, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.PLUS, DyeColor.ORANGE),
                t(DynamicLang.of("menu." + menuId + ".add.name", "§8┃ §aSauvegarder la configuration")),
                Arrays.asList(t(DynamicLang.of("menu." + menuId + ".add.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fNommez et enregistrez les réglages actuels.\n"
                                + "\n"
                                + "§8» §fCliquez pour §autiliser§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        getPlayer().performCommand("h preconfig save " + event.getName());
                    }
                    reopenLater();
                }).setSlot(t(UiLang.PRECONFIG_ADD_CONFIG_ANVIL_HINT)).open();
            }
        });

        if (configNames == null) {
            ItemCreator loading = new ItemCreator(Material.WATCH)
                    .setName(t(DynamicLang.of("menu." + menuId + ".loading.name", "§8┃ §eChargement…")));
            loading.setLores(Arrays.asList(t(DynamicLang.of("menu." + menuId + ".loading.lore",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fRécupération de vos sauvegardes en cours.\n")).split("\n", -1)));
            addItem(new StaticItem(22, loading));
        } else if (configNames.isEmpty()) {
            ItemCreator empty = new ItemCreator(Material.BARRIER)
                    .setName(t(DynamicLang.of("menu." + menuId + ".empty.name", "§8┃ §cAucune sauvegarde")));
            empty.setLores(Arrays.asList(t(DynamicLang.of("menu." + menuId + ".empty.lore",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fCommencez par enregistrer la configuration actuelle.\n")).split("\n", -1)));
            addItem(new StaticItem(22, empty));
        } else {
            pages = paginate(configNames, PER_PAGE,
                    n -> MenuGrid.grid(ROW_FROM, ROW_TO, COL_FROM, COL_TO, CENTERED, n),
                    (name, slot) -> {
                        ItemCreator icon = new ItemCreator(Material.PAPER)
                                .setName(t(DynamicLang.of("menu." + menuId + ".config-dummy.name",
                                        "§8┃ §f%nom_de_la_configuration%"))
                                        .replace("%nom_de_la_configuration%", name));
                        icon.setLores(Arrays.asList(t(DynamicLang.of("menu." + menuId + ".config-dummy.lore",
                                "§7» Élément §fdynamique\n"
                                        + "\n"
                                        + " §f┃ §fClic gauche : chargez cette sauvegarde.\n"
                                        + " §f┃ §fClic droit : demandez sa suppression.\n"
                                        + "\n"
                                        + "§8» §fCliquez pour §futiliser§f.")).split("\n", -1)));

                        addItem(new ActionItem(slot, icon) {
                            @Override
                            public void onClick(InventoryClickEvent e) {
                                new ConfirmMenu(getPlayer(),
                                        t(e.isRightClick() ? UiLang.PRECONFIG_CONFIRM_DELETE : UiLang.PRECONFIG_CONFIRM_LOAD,
                                                Map.of("%name%", name)),
                                        () -> getPlayer().performCommand("h preconfig "
                                                + (e.isRightClick() ? "delete " : "load ") + name),
                                        () -> {
                                        },
                                        PreconfigUi.this).open();
                            }
                        });
                    });

            ItemCreator page = new ItemCreator(Material.MAP)
                    .setName(t(DynamicLang.of("menu." + menuId + ".page.name", "§8┃ §ePage %current%/%total%"))
                            .replace("%current%", String.valueOf(getActual_category()))
                            .replace("%total%", String.valueOf(pages)));
            page.setLores(Arrays.asList(t(DynamicLang.of("menu." + menuId + ".page.lore",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fClic gauche : page suivante.\n"
                            + " §e┃ §fClic droit : page précédente.\n"
                            + "\n"
                            + "§8» §fCliquez pour §eutiliser§f.")).split("\n", -1)));

            addItem(new ActionItem(4, page) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (e.isRightClick()) previousCategory();
                    else nextCategory();
                    open();
                }
            });

            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.ORANGE),
                    t(DynamicLang.of("menu." + menuId + ".previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });

            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.ORANGE),
                    t(DynamicLang.of("menu." + menuId + ".next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.ORANGE),
                t(DynamicLang.of("menu." + menuId + ".back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                back().open();
            }
        });
    }

    private void loadConfigsAsync() {
        Main.getConfigManager()
                .getPlayerConfigNames(getPlayer().getUniqueId())
                .thenAccept(configs -> new BukkitRunnable() {
                    @Override
                    public void run() {
                        settle(configs == null ? List.of() : configs);
                    }
                }.runTask(Main.get()))
                .exceptionally(ex -> {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            getPlayer().sendMessage("§c❌ Erreur chargement configs: " + ex.getMessage());
                            Bukkit.getLogger().log(Level.SEVERE, "Erreur inattendue", ex);
                            settle(List.of());
                        }
                    }.runTask(Main.get());
                    return null;
                });
    }

    private void settle(List<String> configs) {
        UUID uuid = getPlayer().getUniqueId();
        LOADED.put(uuid, configs);
        if (cache.get(uuid) == this) {
            new PreconfigUi(getPlayer(), parent).open();
        } else {
            LOADED.remove(uuid);
        }
    }

    private CustomInventory back() {
        return parent == null ? new DefaultUi(getPlayer()) : parent;
    }

    private void reopenLater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                new PreconfigUi(getPlayer(), parent).open();
            }
        }.runTaskLater(Main.get(), 1L);
    }
}
