package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

public class StructuresUi extends CustomInventory {

    private static final String ID = "menu.structures.";
    private static final DyeColor FRAME = DyeColor.ORANGE;

    public StructuresUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(ID + "title", "» Structures & Terrain"));
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

        addItem(new StaticItem(4, new ItemCreator(Material.WORKBENCH)
                .setName(t(DynamicLang.of(ID + "header.name", "§8┃ §6Génération de l'arène")))
                .setLores(Arrays.asList(t(DynamicLang.of(ID + "header.lore",
                        "§7» Accès §6Host\n"
                                + "\n"
                                + " §6┃ §fChoisissez les structures présentes\n"
                                + " §6┃ §fdans l'arène.\n"
                                + "\n"
                                + " §c☰ §fAppliqué à la §cgénération §fde l'arène :\n"
                                + " §c☰ §frégénérez la map après modification.")).split("\n", -1)))));

        toggle(10, "mineshafts", Material.RAILS, "§8┃ §6Mines abandonnées",
                " §6┃ §fGaleries souterraines avec rails,\n §6┃ §fcoffres et toiles d'araignée.",
                uhc::isGenerateMineshafts, () -> uhc.setGenerateMineshafts(!uhc.isGenerateMineshafts()));

        toggle(11, "strongholds", Material.ENDER_PORTAL_FRAME, "§8┃ §6Forteresses",
                " §6┃ §fForteresses souterraines contenant\n §6┃ §fle portail de l'End.",
                uhc::isGenerateStrongholds, () -> uhc.setGenerateStrongholds(!uhc.isGenerateStrongholds()));

        toggle(12, "villages", Material.EMERALD, "§8┃ §6Villages",
                " §6┃ §fVillages de PNJ avec leurs\n §6┃ §fmaisons et leurs cultures.",
                uhc::isGenerateVillages, () -> uhc.setGenerateVillages(!uhc.isGenerateVillages()));

        toggle(13, "temples", Material.SANDSTONE, "§8┃ §6Temples",
                " §6┃ §fTemples du désert et de la jungle,\n §6┃ §fhuttes de sorcière et igloos.",
                uhc::isGenerateTemples, () -> uhc.setGenerateTemples(!uhc.isGenerateTemples()));

        toggle(14, "monuments", Material.PRISMARINE, "§8┃ §6Monuments océaniques",
                " §6┃ §fMonuments sous-marins gardés\n §6┃ §fpar des gardiens.",
                uhc::isGenerateMonuments, () -> uhc.setGenerateMonuments(!uhc.isGenerateMonuments()));

        toggle(20, "dungeons", Material.MOB_SPAWNER, "§8┃ §6Donjons",
                " §6┃ §fPetites salles souterraines avec\n §6┃ §fspawner et coffres.",
                uhc::isGenerateDungeons, () -> uhc.setGenerateDungeons(!uhc.isGenerateDungeons()));

        toggle(21, "water-lakes", Material.WATER_BUCKET, "§8┃ §6Lacs d'eau",
                " §6┃ §fPoches d'eau naturelles en surface\n §6┃ §fet sous terre.",
                uhc::isGenerateWaterLakes, () -> uhc.setGenerateWaterLakes(!uhc.isGenerateWaterLakes()));

        toggle(22, "lava-lakes", Material.LAVA_BUCKET, "§8┃ §6Lacs de lave",
                " §6┃ §fPoches de lave naturelles en surface\n §6┃ §fet sous terre.",
                uhc::isGenerateLavaLakes, () -> uhc.setGenerateLavaLakes(!uhc.isGenerateLavaLakes()));

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, FRAME),
                t(DynamicLang.of(ID + "back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WorldUi(getPlayer()).open();
            }
        });
    }

    private void toggle(int slot, String key, Material material, String name, String description,
                        BooleanSupplier state, Runnable flip) {
        ItemCreator icon = new ItemCreator(material)
                .setName(t(DynamicLang.of(ID + key + ".name", name)))
                .setGlow(state.getAsBoolean());

        icon.setLores(Arrays.asList(t(DynamicLang.of(ID + key + ".lore",
                "§7» Accès §6Host\n"
                        + "\n"
                        + description + "\n"
                        + "\n"
                        + " §6☰ §fStatut : %etat%\n"
                        + "\n"
                        + "§8» §fCliquez pour §6activer ou désactiver§f."))
                .replace("%etat%", UiItems.onOff(getPlayer(), state.getAsBoolean()))
                .split("\n", -1)));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                flip.run();
                openAll();
            }
        });
    }
}
