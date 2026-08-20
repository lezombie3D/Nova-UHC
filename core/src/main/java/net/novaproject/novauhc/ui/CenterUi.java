package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.world.generation.CenterType;
import net.novaproject.novauhc.world.generation.WorldPopulator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class CenterUi extends CustomInventory {

    private final CustomInventory parent;

    public CenterUi(Player player, CustomInventory parent) {
        super(player);
        this.parent = parent;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.type-de-centre.title", "» Biomes & Type de Centre"));
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
        UiItems.panes(this, DyeColor.LIME, 0, 8, 26);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

        center(11, "roofed", new ItemCreator(Material.RED_MUSHROOM),
                "§8┃ §aBiome : Rooft",
                "§7» Accès §aHost\n\n §a┃ §fUtilisez une forêt sombre comme centre.\n\n §a☰ §fStatut : %center_rooft_status%\n\n§8» §fCliquez pour §autiliser§f.",
                "%center_rooft_status%", CenterType.ROOFT, Biome.ROOFED_FOREST);

        center(12, "taiga", new ItemCreator(Material.LOG).setDurability((short) 1),
                "§8┃ §cBiome : Taiga",
                "§7» Accès §cHost\n\n §c┃ §fUtilisez une taïga comme centre.\n\n §c☰ §fStatut : %center_taiga_status%\n\n§8» §fCliquez pour §cutiliser§f.",
                "%center_taiga_status%", CenterType.TAIGA, Biome.TAIGA_HILLS);

        center(14, "forest", new ItemCreator(Material.SAPLING),
                "§8┃ §eBiome : Forest",
                "§7» Accès §eHost\n\n §e┃ §fUtilisez une forêt classique comme centre.\n\n §e☰ §fStatut : %center_forest_status%\n\n§8» §fCliquez pour §eutiliser§f.",
                "%center_forest_status%", CenterType.FOREST, Biome.FOREST);

        center(15, "flat", new ItemCreator(Material.GRASS),
                "§8┃ §bBiome : Flat",
                "§7» Accès §bHost\n\n §b┃ §fUtilisez une zone plane comme centre.\n\n §b☰ §fStatut : %center_flat_status%\n\n§8» §fCliquez pour §butiliser§f.",
                "%center_flat_status%", CenterType.FLAT, Biome.PLAINS);

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.LIME),
                t(DynamicLang.of("menu.type-de-centre.back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (parent != null) parent.open();
                else getPlayer().closeInventory();
            }
        });
    }

    private void center(int slot, String key, ItemCreator item, String name, String lore,
                        String placeholder, CenterType type, Biome biome) {
        String status = LangManager.get().get(
                CenterType.getApplied() == type ? UiLang.CENTER_STATUS_APPLIED : UiLang.CENTER_STATUS_AVAILABLE,
                getPlayer());

        ItemCreator icon = item
                .setName(t(DynamicLang.of("menu.type-de-centre." + key + ".name", name)))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.type-de-centre." + key + ".lore", lore))
                        .replace(placeholder, status).split("\n", -1)))
                .setGlow(CenterType.getApplied() == type);

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfirmMenu(getPlayer(),
                        LangManager.get().get(UiLang.CENTER_CONFIRM_TEXT, getPlayer()),
                        () -> {
                            new WorldPopulator(Common.get().getArena(), type, biome);
                            CenterType.setApplied(type);
                        },
                        null, CenterUi.this).open();
            }
        });
    }
}
