package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class LanguageUi extends CustomInventory {

    private static final ChatColor[] PALETTE = {
            ChatColor.AQUA, ChatColor.GREEN, ChatColor.YELLOW, ChatColor.GOLD,
            ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.BLUE, ChatColor.DARK_AQUA,
            ChatColor.DARK_GREEN, ChatColor.DARK_PURPLE
    };

    private static final int ROW_FROM = 1;
    private static final int ROW_TO = 1;
    private static final int COL_FROM = 1;
    private static final int COL_TO = 7;
    private static final boolean CENTERED = false;
    private static final int PER_PAGE = MenuGrid.gridCapacity(ROW_FROM, ROW_TO, COL_FROM, COL_TO);

    private static final String LOCALE_LORE = "§7» Élément §adynamique\n\n §a┃ §fLe remplissage commence ici, sept langues\n §a┃ §fpar ligne, le français en premier.\n\n§8» §fCliquez pour §autiliser§f.";
    private static final String CLOSE_LORE = "§7» Accès §cHost\n\n §c┃ §fFermez le menu sans changer de langue.\n\n§8» §fCliquez pour §cutiliser§f.";

    private int pages = 1;

    public LanguageUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.choix-de-la-langue.title", "» Choix de la langue"));
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public int getCategories() {
        return pages;
    }

    private List<String> locales() {
        List<String> list = new ArrayList<>(LangManager.get().getAvailableLocales());
        list.sort((a, b) -> {
            if (a.equalsIgnoreCase(b)) return 0;
            if (a.equalsIgnoreCase("fr_FR")) return -1;
            if (b.equalsIgnoreCase("fr_FR")) return 1;
            return a.compareToIgnoreCase(b);
        });
        return list;
    }

    static String displayName(String code) {
        String[] p = code.split("_");
        Locale loc = p.length > 1 ? new Locale(p[0], p[1]) : new Locale(p[0]);
        String name = loc.getDisplayLanguage(loc);
        if (name == null || name.isEmpty()) name = code;
        return name.substring(0, 1).toUpperCase(loc) + name.substring(1);
    }

    static ChatColor colorFor(String code) {
        return PALETTE[Math.floorMod(code.hashCode(), PALETTE.length)];
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 18, 26);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

        String current = getUHCPlayer().getLocale();

        pages = paginate(locales(), PER_PAGE,
                n -> MenuGrid.grid(ROW_FROM, ROW_TO, COL_FROM, COL_TO, CENTERED, n),
                (locale, slot) -> {
                    ItemCreator icon = LocaleFlags.icon(locale)
                            .setName(t(DynamicLang.of("menu.choix-de-la-langue.locale-dummy.name", "§8┃ §a%nom_de_la_langue%"))
                                    .replace("%nom_de_la_langue%",
                                            colorFor(locale).toString() + ChatColor.BOLD + displayName(locale)))
                            .setLores(Arrays.asList(t(DynamicLang.of("menu.choix-de-la-langue.locale-dummy.lore", LOCALE_LORE))
                                    .split("\n", -1)));
                    if (locale.equalsIgnoreCase(current)) icon.setGlow(true);

                    addItem(new ActionItem(slot, icon) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            if (locale.equalsIgnoreCase(getUHCPlayer().getLocale())) return;
                            getUHCPlayer().setLocale(locale);
                            LangManager.get().send(CoreLang.COMMON_SUCCESSFUL_MODIFICATION, getPlayer());
                            open();
                        }
                    });
                });

        addItem(new ActionItem(22, new ItemCreator(Material.BARRIER)
                .setName(t(DynamicLang.of("menu.choix-de-la-langue.close.name", "§8┃ §cFermer")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.choix-de-la-langue.close.lore", CLOSE_LORE))
                        .split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().closeInventory();
            }
        });
    }
}
