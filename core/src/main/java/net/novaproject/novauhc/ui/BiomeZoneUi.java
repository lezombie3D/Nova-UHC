package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.world.generation.BiomeZones;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Locale;

public class BiomeZoneUi extends CustomInventory {

    private static final String ID = "menu.biomes-zone.";
    private static final DyeColor FRAME = DyeColor.GREEN;

    private final BiomeZones.Zone zone;

    public BiomeZoneUi(Player player, BiomeZones.Zone zone) {
        super(player);
        this.zone = zone;
    }

    @Override
    public String broadcastKey() {
        return getClass().getName() + ":" + zone.name();
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(ID + "title", "» Règle : %zone%"))
                .replace("%zone%", label());
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
    public void setup() {
        UiItems.panes(this, FRAME, 0, 8, 26);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

        addItem(new StaticItem(4, new ItemCreator(Material.SAPLING)
                .setName(t(DynamicLang.of(ID + "header.name", "§8┃ §2%zone%")).replace("%zone%", label()))
                .setLores(Arrays.asList(t(DynamicLang.of(ID + "header.lore",
                        "§7» Accès §2Host\n"
                                + "\n"
                                + " §2┃ §fEXCLURE : les biomes listés sont remplacés.\n"
                                + " §2┃ §fSEULEMENT : tout sauf les biomes listés\n"
                                + " §2┃ §fest remplacé.\n"
                                + "\n"
                                + " §c☰ §fAppliqué à la §cgénération §fde l'arène.")).split("\n", -1)))));

        String mode = currentMode();

        ItemCreator modeIcon = new ItemCreator(Material.COMPASS)
                .setName(t(DynamicLang.of(ID + "mode.name", "§8┃ §2Mode")))
                .setGlow(!"AUCUN".equals(mode));
        modeIcon.setLores(Arrays.asList(t(DynamicLang.of(ID + "mode.lore",
                "§7» Accès §2Host\n"
                        + "\n"
                        + " §2☰ §fMode actuel : §f%mode%\n"
                        + "\n"
                        + "§8» §fCliquez pour §2changer§f."))
                .replace("%mode%", mode).split("\n", -1)));

        addItem(new ActionItem(11, modeIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                zone.setMode(BiomeZones.Mode.valueOf(currentMode()).next().name());
                openAll();
            }
        });

        ItemCreator listIcon = new ItemCreator(Material.PAPER)
                .setName(t(DynamicLang.of(ID + "list.name", "§8┃ §2Biomes concernés")));
        listIcon.setLores(Arrays.asList(t(DynamicLang.of(ID + "list.lore",
                "§7» Accès §2Host\n"
                        + "\n"
                        + " §2☰ §fSélection : §f%liste%\n"
                        + "\n"
                        + "§8» §fCliquez pour §2choisir§f."))
                .replace("%liste%", summary()).split("\n", -1)));

        addItem(new ActionItem(13, listIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new BiomePickerUi(getPlayer(), zone, false).open();
            }
        });

        ItemCreator replacementIcon = new ItemCreator(Material.GRASS)
                .setName(t(DynamicLang.of(ID + "replacement.name", "§8┃ §2Biome de remplacement")));
        replacementIcon.setLores(Arrays.asList(t(DynamicLang.of(ID + "replacement.lore",
                "§7» Accès §2Host\n"
                        + "\n"
                        + " §2☰ §fBiome utilisé : §f%biome%\n"
                        + "\n"
                        + "§8» §fCliquez pour §2choisir§f."))
                .replace("%biome%", zone.replacement()).split("\n", -1)));

        addItem(new ActionItem(15, replacementIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new BiomePickerUi(getPlayer(), zone, true).open();
            }
        });

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, FRAME),
                t(DynamicLang.of(ID + "back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new BiomeZonesUi(getPlayer()).open();
            }
        });
    }

    private String currentMode() {
        String raw = zone.mode();
        if (raw == null) return "AUCUN";
        try {
            return BiomeZones.Mode.valueOf(raw.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException e) {
            return "AUCUN";
        }
    }

    private String summary() {
        String raw = zone.list();
        if (raw == null || raw.trim().isEmpty()) return t(DynamicLang.of(ID + "empty", "aucun"));
        String[] names = raw.split(",");
        return names.length <= 2 ? raw : names.length + " biomes";
    }

    private String label() {
        return switch (zone) {
            case INNER -> t(DynamicLang.of(ID + "zone-inner", "Bande intérieure"));
            case OUTER -> t(DynamicLang.of(ID + "zone-outer", "Bande extérieure"));
            case GLOBAL -> t(DynamicLang.of(ID + "zone-global", "Biomes globaux"));
        };
    }
}
