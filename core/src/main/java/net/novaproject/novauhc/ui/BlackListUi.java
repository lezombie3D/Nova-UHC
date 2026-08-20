package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.api.BlacklistManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.item.ActionItem;
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
import java.util.UUID;

public class BlackListUi extends CustomInventory {

    private static final int ROW_FROM = 1;
    private static final int ROW_TO = 4;
    private static final int COL_FROM = 1;
    private static final int COL_TO = 7;
    private static final boolean CENTERED = false;
    private static final int PER_PAGE = MenuGrid.gridCapacity(ROW_FROM, ROW_TO, COL_FROM, COL_TO);

    private int pages = 1;
    private final CustomInventory parent;

    public BlackListUi(Player player) {
        this(player, null);
    }

    public BlackListUi(Player player, CustomInventory parent) {
        super(player);
        this.parent = parent;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.gestion-de-la-blacklist.title", "» Gestion de la Blacklist"));
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
        UiItems.panes(this, DyeColor.RED, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        ItemCreator add = UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.PLUS, DyeColor.RED),
                t(DynamicLang.of("menu.gestion-de-la-blacklist.add.name", "§8┃ §aAjouter un joueur")),
                Arrays.asList(t(DynamicLang.of("menu.gestion-de-la-blacklist.add.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fSaisissez le pseudo du joueur\n"
                                + " §a┃ §fà ajouter à la blacklist.\n"
                                + "\n"
                                + "§8» §fCliquez pour §autiliser§f.")).split("\n", -1)));

        addItem(new ActionItem(0, add) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() != AnvilUi.AnvilSlot.OUTPUT) {
                        open();
                        return;
                    }
                    OfflinePlayer target = Bukkit.getOfflinePlayer(event.getName());
                    if (target == null || target.getUniqueId() == null) {
                        LangManager.get().send(CoreLang.COMMON_BL_INVALID_PLAYER, getPlayer(),
                                Map.of("%player%", event.getName()));
                        open();
                        return;
                    }
                    String label = target.getName() != null ? target.getName() : event.getName();
                    if (BlacklistManager.get().add(target)) {
                        LangManager.get().send(CoreLang.COMMON_BL_ADDED, getPlayer(), Map.of("%player%", label));
                        kickIfOnline(target);
                    } else {
                        LangManager.get().send(CoreLang.COMMON_BL_ALREADY_IN, getPlayer(), Map.of("%player%", label));
                    }
                    openAll();
                }).setSlot(t(UiLang.BLACKLIST_ADD_PLAYER_ANVIL_HINT)).open();
            }
        });

        ItemCreator clear = new ItemCreator(Material.LAVA_BUCKET)
                .setName(t(DynamicLang.of("menu.gestion-de-la-blacklist.clear.name", "§8┃ §cVider la blacklist")));
        clear.setLores(Arrays.asList(t(DynamicLang.of("menu.gestion-de-la-blacklist.clear.lore",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fDemandez une confirmation avant\n"
                        + " §c┃ §fde retirer tous les joueurs.\n"
                        + "\n"
                        + "§8» §fCliquez pour §cutiliser§f.")).split("\n", -1)));

        addItem(new ActionItem(6, clear) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (BlacklistManager.get().getEntries().isEmpty()) return;
                new ConfirmMenu(getPlayer(),
                        t(UiLang.BLACKLIST_CONFIRM_CLEAR),
                        () -> {
                            BlacklistManager.get().clear();
                            openAll();
                        },
                        BlackListUi.this::openAll,
                        BlackListUi.this).open();
            }
        });

        List<Map.Entry<UUID, String>> entries = new ArrayList<>(BlacklistManager.get().getEntries().entrySet());

        pages = paginate(entries, PER_PAGE,
                n -> MenuGrid.grid(ROW_FROM, ROW_TO, COL_FROM, COL_TO, CENTERED, n),
                (entry, slot) -> {
                    String label = label(entry);
                    ItemCreator icon = new ItemCreator(Material.SKULL_ITEM)
                            .setDurability((short) 3)
                            .setOwner(label)
                            .setName(t(DynamicLang.of("menu.gestion-de-la-blacklist.player-dummy.name",
                                    "§8┃ §e%joueur_blacklisté%")).replace("%joueur_blacklisté%", label));
                    icon.setLores(Arrays.asList(t(DynamicLang.of("menu.gestion-de-la-blacklist.player-dummy.lore",
                            "§7» Élément §edynamique\n"
                                    + "\n"
                                    + " §e┃ §fReprésente chaque joueur blacklisté.\n"
                                    + " §e┃ §fCliquez pour demander son retrait.\n"
                                    + "\n"
                                    + "§8» §fCliquez pour §eutiliser§f.")).split("\n", -1)));

                    addItem(new ActionItem(slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            new ConfirmMenu(getPlayer(),
                                    t(UiLang.BLACKLIST_CONFIRM_REMOVE, Map.of("%player%", label)),
                                    () -> {
                                        if (BlacklistManager.get().remove(entry.getKey())) {
                                            LangManager.get().send(CoreLang.COMMON_BL_REMOVED, getPlayer(),
                                                    Map.of("%player%", label));
                                        }
                                        openAll();
                                    },
                                    BlackListUi.this::openAll,
                                    BlackListUi.this).open();
                        }
                    });
                });

        addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.RED),
                t(DynamicLang.of("menu.gestion-de-la-blacklist.previous.name", "§8┃ §cPrécédent")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                previousCategory();
                open();
            }
        });

        addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.RED),
                t(DynamicLang.of("menu.gestion-de-la-blacklist.next.name", "§8┃ §aSuivant")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                nextCategory();
                open();
            }
        });

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.RED),
                t(DynamicLang.of("menu.gestion-de-la-blacklist.back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                (parent != null ? parent : new DefaultUi(getPlayer())).open();
            }
        });
    }

    private String label(Map.Entry<UUID, String> entry) {
        return entry.getValue() != null ? entry.getValue() : entry.getKey().toString();
    }

    private void kickIfOnline(OfflinePlayer target) {
        if (!target.isOnline() || target.getPlayer() == null) return;
        Player online = target.getPlayer();
        online.kickPlayer(LangManager.get().get(CoreLang.COMMON_KICK_BLACKLIST, online));
        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_BL_KICKED_BROADCAST)
                .replace("%player%", target.getName() != null ? target.getName() : online.getName()));
    }

}
