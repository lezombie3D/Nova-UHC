package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.world.generation.BiomeZones;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;

public class BiomePickerUi extends CustomInventory {

    private static final String ID = "menu.biomes-selection.";
    private static final DyeColor FRAME = DyeColor.GREEN;
    private static final int PER_PAGE = 28;

    private final BiomeZones.Zone zone;
    private final boolean replacement;
    private int pages = 1;

    public BiomePickerUi(Player player, BiomeZones.Zone zone, boolean replacement) {
        super(player);
        this.zone = zone;
        this.replacement = replacement;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName() + ":" + zone.name() + ":" + replacement;
    }

    @Override
    public String getTitle() {
        return replacement
                ? t(DynamicLang.of(ID + "title-replacement", "» Biome de remplacement"))
                : t(DynamicLang.of(ID + "title-list", "» Biomes concernés"));
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
        UiItems.panes(this, FRAME, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        List<Biome> biomes = Arrays.asList(Biome.values());

        pages = paginate(biomes, PER_PAGE,
                n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                (biome, slot) -> addItem(new ActionItem(slot, tile(biome)) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        if (replacement) zone.setReplacement(biome.name());
                        else zone.toggle(biome);
                        openAll();
                    }
                }));

        if (biomes.size() > PER_PAGE) {
            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, FRAME),
                    t(DynamicLang.of(ID + "previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });
            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, FRAME),
                    t(DynamicLang.of(ID + "next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, FRAME),
                t(DynamicLang.of(ID + "back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new BiomeZoneUi(getPlayer(), zone).open();
            }
        });
    }

    private ItemCreator tile(Biome biome) {
        boolean selected = replacement
                ? biome.name().equalsIgnoreCase(zone.replacement())
                : zone.contains(biome);

        return new ItemCreator(selected ? Material.GRASS : Material.DIRT)
                .setName("§8┃ " + (selected ? "§a" : "§7") + biome.name())
                .setLores(Arrays.asList(t(DynamicLang.of(ID + "entry.lore",
                        "§7» Accès §2Host\n"
                                + "\n"
                                + " §2☰ §fÉtat : %etat%\n"
                                + "\n"
                                + "§8» §fCliquez pour §2choisir§f."))
                        .replace("%etat%", UiItems.onOff(getPlayer(), selected))
                        .split("\n", -1)))
                .setGlow(selected);
    }
}
