package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.world.generation.BiomeZones;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class BiomeZonesUi extends CustomInventory {

    private static final String ID = "menu.biomes-distance.";
    private static final DyeColor FRAME = DyeColor.GREEN;

    public BiomeZonesUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(ID + "title", "» Biomes par distance"));
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

        UHCManager uhc = UHCManager.get();
        int radius = uhc.getBiomeInnerRadius();

        addItem(new StaticItem(4, new ItemCreator(Material.WORKBENCH)
                .setName(t(DynamicLang.of(ID + "header.name", "§8┃ §2Biomes de l'arène")))
                .setLores(Arrays.asList(t(DynamicLang.of(ID + "header.lore",
                        "§7» Accès §2Host\n"
                                + "\n"
                                + " §2┃ §fLa règle §2globale §fs'applique partout,\n"
                                + " §2┃ §fpuis la bande de la distance la surcharge.\n"
                                + "\n"
                                + " §c☰ §fAppliqué à la §cgénération §fde l'arène :\n"
                                + " §c☰ §frégénérez la map après modification.")).split("\n", -1)))));

        zone(11, BiomeZones.Zone.INNER, Material.SAPLING,
                "§8┃ §2Bande intérieure",
                " §2┃ §fDe 0 au rayon défini.");

        ItemCreator radiusIcon = new ItemCreator(Material.COMPASS)
                .setName(t(DynamicLang.of(ID + "radius.name", "§8┃ §2Rayon de séparation")));
        radiusIcon.setLores(Arrays.asList(t(DynamicLang.of(ID + "radius.lore",
                "§7» Accès §2Host\n"
                        + "\n"
                        + " §2┃ §fDistance en blocs séparant les deux bandes.\n"
                        + " §2┃ §f0 désactive les bandes : seule la règle\n"
                        + " §2┃ §fglobale reste active.\n"
                        + "\n"
                        + " §2☰ §fRayon : §f%rayon% blocs\n"
                        + "\n"
                        + "§8» §fCliquez pour §2modifier§f."))
                .replace("%rayon%", String.valueOf(radius)).split("\n", -1)));

        addItem(new ActionItem(13, radiusIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 500, 100, 50, 500, 100, 50, radius, 0, 5000,
                        BiomeZonesUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        uhc.setBiomeInnerRadius(newValue.intValue());
                    }
                }.open();
            }
        });

        zone(15, BiomeZones.Zone.OUTER, Material.LOG,
                "§8┃ §2Bande extérieure",
                " §2┃ §fDu rayon défini jusqu'à la bordure.");

        zone(22, BiomeZones.Zone.GLOBAL, Material.GRASS,
                "§8┃ §2Biomes globaux",
                " §2┃ §fAppliqué à toutes les distances.");

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, FRAME),
                t(DynamicLang.of(ID + "back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WorldUi(getPlayer()).open();
            }
        });
    }

    private void zone(int slot, BiomeZones.Zone zone, Material material, String name, String description) {
        String mode = zone.mode() == null ? "AUCUN" : zone.mode();

        ItemCreator icon = new ItemCreator(material)
                .setName(t(DynamicLang.of(ID + zone.name().toLowerCase() + ".name", name)))
                .setGlow(!"AUCUN".equalsIgnoreCase(mode));

        icon.setLores(Arrays.asList(t(DynamicLang.of(ID + zone.name().toLowerCase() + ".lore",
                "§7» Accès §2Host\n"
                        + "\n"
                        + description + "\n"
                        + "\n"
                        + " §2☰ §fMode : §f%mode%\n"
                        + "\n"
                        + "§8» §fCliquez pour §2configurer§f."))
                .replace("%mode%", mode).split("\n", -1)));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new BiomeZoneUi(getPlayer(), zone).open();
            }
        });
    }
}
