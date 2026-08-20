package net.novaproject.novauhc.ability.craft;

import net.novaproject.novauhc.ability.Ability;
import net.novaproject.novauhc.ability.AbilityManager;
import net.novaproject.novauhc.ability.craft.CraftAbilityManager.CraftConsultRegistry;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.MenuGrid;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CraftConsultGUI extends CustomInventory {

    private static final String PREVIOUS_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RjOWU0ZGNmYTQyMjFhMWZhZGMxYjViMmIxMWQ4YmVlYjU3ODc5YWYxYzQyMzYyMTQyYmFlMWVkZDUifX19";
    private static final String NEXT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU2YTM2MTg0NTllNDNiMjg3YjIyYjdlMjM1ZWM2OTk1OTQ1NDZjNmZjZDZkYzg0YmZjYTRjZjMwYWI5MzExIn19fQ==";

    private static final int[] GRID_SLOTS = {12, 13, 14, 21, 22, 23, 30, 31, 32};

    private final List<CraftConsultRegistry.Entry> playerEntries;
    private int currentPage = 1;
    private int pages = 1;

    public CraftConsultGUI(Player player) {
        super(player);
        this.playerEntries = filterPlayerEntries(player);
    }

    private List<CraftConsultRegistry.Entry> filterPlayerEntries(Player player) {
        List<CraftConsultRegistry.Entry> filtered = new ArrayList<>();
        for (Ability ability : AbilityManager.get().abilitiesOf(player)) {
            filtered.addAll(CraftConsultRegistry.getByPermissionAbilityName(ability.getName()));
        }
        return filtered;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.craft.title", "» Consultation des Crafts"));
    }

    @Override
    public int getLines() {
        return 5;
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
        int total = Math.max(1, playerEntries.size());
        currentPage = Math.min(Math.max(1, getActual_category()), total);
        setActual_category(currentPage);

        UiItems.panes(this, DyeColor.YELLOW, 0, 8, 44);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 27, 35, 37, 43);

        pages = paginate(playerEntries, 1,
                n -> MenuGrid.linear(4, 4, n),
                (entry, slot) -> addItem(new StaticItem(slot, recipeIcon(entry))));

        for (int i = 0; i < GRID_SLOTS.length; i++) {
            addItem(new StaticItem(GRID_SLOTS[i], gridIcon(i)));
        }

        ItemCreator close = new ItemCreator(Material.WOOD_DOOR)
                .setName(t(DynamicLang.of("menu.craft.close.name", "§8┃ §7Fermer")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.craft.close.lore",
                        "§7» Accès §7Host\n"
                                + "\n"
                                + " §7┃ §fFermez la consultation des crafts.\n"
                                + "\n"
                                + "§8» §fCliquez pour §7utiliser§f.")).split("\n", -1)));

        addItem(new ActionItem(36, close) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().closeInventory();
            }
        });

        addItem(new StaticItem(40, new ItemCreator(Material.PAPER)
                .setName(t(DynamicLang.of("menu.craft.page.name", "§8┃ §9Craft %current%/%total%"))
                        .replace("%current%", String.valueOf(currentPage))
                        .replace("%total%", String.valueOf(total)))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.craft.page.lore",
                        "§7» Accès §9Host\n"
                                + "\n"
                                + " §9┃ §fPosition dans la liste des recettes.\n"
                                + "\n")).split("\n", -1)))));

        if (playerEntries.size() > 1) {
            addItem(new ActionItem(38, UiItems.createCustomButon(PREVIOUS_TEXTURE,
                    t(DynamicLang.of("menu.craft.previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });

            addItem(new ActionItem(42, UiItems.createCustomButon(NEXT_TEXTURE,
                    t(DynamicLang.of("menu.craft.next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }
    }

    private CraftConsultRegistry.Entry current() {
        if (currentPage < 1 || currentPage > playerEntries.size()) return null;
        return playerEntries.get(currentPage - 1);
    }

    private ItemCreator recipeIcon(CraftConsultRegistry.Entry entry) {
        List<String> lore = new ArrayList<>();
        if (entry.description() != null && !entry.description().isEmpty()) {
            lore.add(ChatColor.GRAY + entry.description());
        }
        ItemStack result = entry.result();
        if (result != null) {
            lore.add("");
            lore.add(ChatColor.GRAY + "Resultat : " + ChatColor.WHITE
                    + result.getAmount() + "x " + resultLabel(result));
        }
        return new ItemCreator(Material.NAME_TAG)
                .setName(t(DynamicLang.of("menu.craft.name.name", "§8┃ §a%nom_de_la_recette%"))
                        .replace("%nom_de_la_recette%", entry.displayName()))
                .setLores(lore);
    }

    private String resultLabel(ItemStack result) {
        if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
            return result.getItemMeta().getDisplayName();
        }
        return result.getType().name();
    }

    private ItemCreator gridIcon(int index) {
        CraftConsultRegistry.Entry entry = current();
        Material[] pattern = entry == null ? null : entry.pattern();
        if (pattern != null && index < pattern.length && pattern[index] != null) {
            return new ItemCreator(new ItemStack(pattern[index]));
        }
        return new ItemCreator(Material.STAINED_GLASS_PANE).setDurability((short) 8).setName(" ");
    }

    @Override
    public void open() {
        if (playerEntries.isEmpty()) {
            return;
        }
        super.open();
    }
}
