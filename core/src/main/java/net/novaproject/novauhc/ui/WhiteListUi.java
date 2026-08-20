package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WhiteListUi extends CustomInventory {

    private static final String SPECTATOR_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2RiODM1ODY1NDI5MzRmOGMzMjMxYTUyODRmMjQ4OWI4NzY3ODQ3ODQ1NGZjYTY5MzU5NDQ3NTY5ZjE1N2QxNCJ9fX0=";

    private static final int PER_PAGE = 28;

    private enum Filter {
        ALL("tous", "§eTous"),
        ONLINE("connectes", "§aConnectés"),
        OFFLINE("hors-ligne", "§7Hors ligne");

        private final String key;
        private final String label;

        Filter(String key, String label) {
            this.key = key;
            this.label = label;
        }

        Filter next() {
            return values()[(ordinal() + 1) % values().length];
        }

        boolean matches(OfflinePlayer player) {
            return switch (this) {
                case ALL -> true;
                case ONLINE -> player.isOnline();
                case OFFLINE -> !player.isOnline();
            };
        }
    }

    private int pages = 1;
    private Filter filter = Filter.ALL;

    public WhiteListUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.accessibilite.title", "» Accessibilité✓"));
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
    public void setup() {
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);
        UiItems.panes(this, DyeColor.YELLOW, 53);

        List<OfflinePlayer> all = new ArrayList<>(Bukkit.getWhitelistedPlayers());
        List<OfflinePlayer> whitelisted = new ArrayList<>();
        int online = 0;
        for (OfflinePlayer target : all) {
            if (target.isOnline()) online++;
            if (filter.matches(target)) whitelisted.add(target);
        }

        addItem(new ActionItem(0, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.PLUS, DyeColor.YELLOW),
                t(DynamicLang.of("menu.accessibilite.btn-0.name", "§8┃ §aAjouter un Joueur")),
                Arrays.asList(t(DynamicLang.of("menu.accessibilite.btn-0.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fPermet §ad'ajouter §fun joueur \n"
                                + " §a┃ §fa la §aWhiteList.\n"
                                + "\n"
                                + "§8» §fClique pour §aaccedez")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        Bukkit.getOfflinePlayer(event.getName()).setWhitelisted(true);
                        LangManager.get().send(CoreLang.COMMON_SUCCESSFUL_MODIFICATION, getPlayer());
                        openAll();
                        return;
                    }
                    open();
                }).setSlot(t(UiLang.WHITELIST_ADD_PLAYER_ANVIL_HINT)).open();
            }
        });

        addItem(new StaticItem(4, new ItemCreator(Material.WATCH)
                .setName(t(DynamicLang.of("menu.accessibilite.btn-4.name", "§8┃ §eAccessibilité de la Partie")))));

        addItem(new ActionItem(2, UiItems.createCustomButon(SPECTATOR_TEXTURE,
                t(DynamicLang.of("menu.accessibilite.btn-2.name", "§8┃ §dSpectateur")),
                Arrays.asList(t(DynamicLang.of("menu.accessibilite.btn-2.lore",
                        "§7» Accès §dHost\n"
                                + "\n"
                                + " §d┃ §fPermet §ad'activé §f/ §cdesactivé \n"
                                + " §d┃ §fla §dSpectateur.\n"
                                + "\n"
                                + " §d☰ §fStatus spectateur: %spect_active%\n"
                                + "\n"
                                + "§8» §fClique pour §dtoggles"))
                        .replace("%spect_active%", status(UHCManager.get().isSpectator()))
                        .split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setSpectator(!UHCManager.get().isSpectator());
                openAll();
            }
        });

        addItem(new ActionItem(6, UiItems.stateDye(Bukkit.hasWhitelist())
                .setName(t(DynamicLang.of("menu.accessibilite.btn-6.name", "§8┃ §c§lDésactivée la Whitelist")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.accessibilite.btn-6.lore",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fPermet §ad'activé §f/ §cdesactivé \n"
                                + " §c┃ §fla §cWhitelist.\n"
                                + "\n"
                                + " §c☰ §fStatus whitelist: %whitelist_active%\n"
                                + "\n"
                                + "§8» §fClique pour §ctoggles"))
                        .replace("%whitelist_active%", status(Bukkit.hasWhitelist()))
                        .split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                Bukkit.getServer().setWhitelist(!Bukkit.hasWhitelist());
                openAll();
            }
        });

        addItem(new ActionItem(8, new ItemCreator(Material.BARRIER)
                .setName(t(DynamicLang.of("menu.accessibilite.blacklist.name", "§8┃ §4Gérer la Blacklist")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.accessibilite.blacklist.lore",
                        "§7» Accès §4Host\n"
                                + "\n"
                                + " §4┃ §fConsultez les joueurs interdits\n"
                                + " §4┃ §fet gérez leur accès à la partie.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §4accéder§f.")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new BlackListUi(getPlayer(), WhiteListUi.this).open();
            }
        });

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.YELLOW),
                t(DynamicLang.of("menu.accessibilite.btn-45.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new DefaultUi(getPlayer()).open();
            }
        });

        ItemCreator filterIcon = new ItemCreator(Material.HOPPER)
                .setName(t(DynamicLang.of("menu.accessibilite.filtre.name", "§8┃ §eFiltre : %filtre%"))
                        .replace("%filtre%", t(DynamicLang.of("menu.accessibilite.filtre." + filter.key, filter.label))));
        filterIcon.setLores(Arrays.asList(t(DynamicLang.of("menu.accessibilite.filtre.lore",
                "§7» Accès §eHost\n"
                        + "\n"
                        + " §e┃ §fN'affichez que les joueurs voulus\n"
                        + " §e┃ §fparmi les whitelistés.\n"
                        + "\n"
                        + " §a☰ §fConnectés : §a%online%\n"
                        + " §7☰ §fHors ligne : §7%offline%\n"
                        + " §e☰ §fTotal : §e%total%\n"
                        + "\n"
                        + "§8» §fCliquez pour §echanger§f."))
                .replace("%online%", String.valueOf(online))
                .replace("%offline%", String.valueOf(all.size() - online))
                .replace("%total%", String.valueOf(all.size()))
                .split("\n", -1)));

        addItem(new ActionItem(49, filterIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                filter = filter.next();
                setActual_category(1);
                open();
            }
        });

        String headName = t(DynamicLang.of("menu.accessibilite.btn-10.name", "§8┃ §f%main_color%%nom_du_joueur%"));
        String headLore = t(DynamicLang.of("menu.accessibilite.btn-10.lore",
                " §7┃ §fÉtat : %etat%\n"
                        + "\n"
                        + "§7» §fClique pour §cretirer."));
        String online_label = t(DynamicLang.of("menu.accessibilite.filtre.connectes", "§aConnectés"));
        String offline_label = t(DynamicLang.of("menu.accessibilite.filtre.hors-ligne", "§7Hors ligne"));

        pages = paginate(whitelisted, PER_PAGE,
                n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                (target, slot) -> addItem(new ActionItem(slot, new ItemCreator(Material.SKULL_ITEM)
                        .setDurability((short) 3)
                        .setOwner(target.getName())
                        .setName(headName.replace("%nom_du_joueur%", target.getName()))
                        .setLores(Arrays.asList(headLore
                                .replace("%etat%", target.isOnline() ? online_label : offline_label)
                                .split("\n", -1)))) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        if (!target.isWhitelisted()) return;
                        new ConfirmMenu(getPlayer(),
                                t(UiLang.WHITELIST_CONFIRM_REMOVE, Map.of("%player%", target.getName())),
                                () -> {
                                    target.setWhitelisted(false);
                                    if (target.isOnline() && target.getPlayer() != null) {
                                        target.getPlayer().kickPlayer(LangManager.get().get(CoreLang.COMMON_KICKED, target.getPlayer()));
                                        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_KICKED_MESSAGE,
                                                Map.of("%player%", target.getName())));
                                    }
                                    openAll();
                                },
                                WhiteListUi.this::openAll,
                                WhiteListUi.this).open();
                    }
                }));

        if (whitelisted.size() > PER_PAGE) {
            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.YELLOW),
                    t(DynamicLang.of("menu.accessibilite.btn-47.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });
            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.YELLOW),
                    t(DynamicLang.of("menu.accessibilite.btn-51.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }
    }

    private String status(boolean enabled) {
        return t(enabled ? UiLang.MAIN_STATUS_ENABLED : UiLang.MAIN_STATUS_DISABLED);
    }

}
