package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.ConfigVarUi;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.ui.MenuHeads;
import net.novaproject.novauhc.ui.MenuHeads.Shape;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Map;

public class DropUi extends CustomInventory {

    private static final String MENU = "menu.taux-de-drop-des-objets.";

    public DropUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(MENU + "title", "» Taux de drop"));
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
    public void setup() {
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17);
        UiItems.panes(this, DyeColor.BLUE, 0, 8, 53);
        UiItems.panes(this, DyeColor.WHITE, 36);
        UiItems.panes(this, DyeColor.WHITE, 44, 46, 52);

        hunger();

        rate(20, "btn-20", Material.APPLE, "c", "Pommes", "%valeur_pomme%", DropItemRate.APPLE);
        rate(21, "btn-21", Material.FEATHER, "f", "Plumes", "%valeur_plume%", DropItemRate.FEATHER);
        rate(23, "btn-23", Material.STRING, "d", "Fil", "%valeur_fil%", DropItemRate.STRING);
        rate(24, "btn-24", Material.ARROW, "3", "Flèche", "%valeur_arrow%", DropItemRate.ARROW);
        rate(29, "btn-29", Material.LEATHER, "6", "Cuir", "%valeur_cuir%", DropItemRate.LEATHER);
        rate(30, "btn-30", Material.FLINT, "7", "Silex", "%valeur_silex%", DropItemRate.FLINT);
        rate(32, "btn-32", Material.SULPHUR, "b", "Poudre à Canon", "%valeur_canon%", DropItemRate.GUNPOWDER);
        rate(33, "btn-33", Material.ENDER_PEARL, "2", "Ender Pearl", "%valeur_pearl%", DropItemRate.ENDERPEARL);
        rate(48, "btn-48", Material.BLAZE_ROD, "6", "Blaze Rod", "%valeur_blazerod%", DropItemRate.BLAZE_ROD);
        rate(50, "btn-50", Material.MAGMA_CREAM, "4", "Magma Cream", "%valeur_magma%", DropItemRate.MAGMA_CREAM);

        addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(Shape.BACK, DyeColor.BLUE),
                t(DynamicLang.of(MENU + "btn-45.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new GameUi(getPlayer()).open();
            }
        });
    }

    private void hunger() {
        UHCManager uhc = UHCManager.get();
        double current = uhc.getHungerSlowdown();
        Map<String, Object> values = Map.of("%valeur_faim%", (int) (current * 100) + "%");

        ItemCreator icon = new ItemCreator(Material.COOKED_BEEF)
                .setName(t(DynamicLang.of(MENU + "btn-4.name", "§8┃ §eFaim Ralentie"), values));
        icon.setLores(Arrays.asList(t(DynamicLang.of(MENU + "btn-4.lore",
                "§7» Accès §eHost\n \n §e┃ §fChance d'ignorer chaque baisse de faim (§e0% = vanilla)\n\n"
                        + "§e☰ §rTotal: §f<%valeur_faim%>\n\n§8» §fCliquez pour §emodifier§f."),
                values).split("\n", -1)));

        addItem(new ActionItem(4, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 0.25, 0.10, 0.05, 0.25, 0.10, 0.05,
                        current, 0, 1, DropUi.this, VariableType.PERCENTAGE) {
                    @Override
                    public void onChange(Number newValue) {
                        uhc.setHungerSlowdown(newValue.doubleValue());
                    }
                }.open();
            }
        });
    }

    private void rate(int slot, String key, Material material, String color, String label,
                      String placeholder, DropItemRate rate) {
        Map<String, Object> values = Map.of(placeholder, rate.getAmount() + "%");

        ItemCreator icon = new ItemCreator(material)
                .setName(t(DynamicLang.of(MENU + key + ".name", "§8┃ §" + color + label), values))
                .setGlow(rate.getAmount() > 0);
        icon.setLores(Arrays.asList(t(DynamicLang.of(MENU + key + ".lore",
                "§7» Accès §" + color + "Host\n\n"
                        + " §" + color + "┃ §fGauche = §aAjouter§f.\n"
                        + " §" + color + "┃ §fDroit = §cDiminuer§f.\n\n"
                        + "§" + color + "☰ §rTotal: §f<" + placeholder + ">"),
                values).split("\n", -1)));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                rate.toggleAmount(e.getClick());
                openAll();
            }
        });
    }
}
