package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.GameUi;
import net.novaproject.novauhc.ui.MenuHeads;
import net.novaproject.novauhc.ui.MenuHeads.Shape;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.Arrays;

public class PotionUi extends CustomInventory {

    private static final String MENU = "configuration-des-potions";

    public PotionUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + MENU + ".title", "» Limites de Potions"));
    }

    @Override
    public int getLines() {
        return 4;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.CYAN, 0, 8, 35);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26, 28, 34);

        toggle(10, "speed", Potions.SPEED, "§a", "Speed",
                "Autorisez ou interdisez les potions de Vitesse.");
        toggle(11, "strength", Potions.STRENGHT, "§c", "Strength",
                "Autorisez ou interdisez les potions de Force.");
        toggle(12, "jump", Potions.JUMP_BOOST, "§e", "Jump Boost",
                "Autorisez ou interdisez les potions de Saut.");
        toggle(14, "heal", Potions.HEAL, "§b", "Instant Health",
                "Autorisez ou interdisez le Soin instantané.");
        toggle(15, "regen", Potions.REGENERATION, "§d", "Regeneration",
                "Autorisez ou interdisez la Régénération.");
        toggle(16, "fire", Potions.FIRE_RESISTANCE, "§6", "Fire Resistance",
                "Autorisez ou interdisez la Résistance au feu.");
        toggle(19, "poison", Potions.POISON, "§2", "Poison",
                "Autorisez ou interdisez les potions de Poison.");
        toggle(20, "invisibility", Potions.INVISIBILITY, "§3", "Invisibility",
                "Autorisez ou interdisez l’Invisibilité.");
        toggle(21, "other", Potions.OTHER, "§4", "Autres Potions",
                "Gérez les autres familles de potions.");
        toggle(23, "splash", Potions.SPLASH, "§5", "Splash",
                "Autorisez ou interdisez les potions jetables.");
        toggle(24, "level-two", Potions.LEVEL_II, "§9", "Niveau II",
                "Autorisez ou interdisez les potions de niveau II.");
        toggle(25, "extended", Potions.LONG_DURATION, "§f", "Longue durée",
                "Autorisez ou interdisez les potions prolongées.");

        addItem(new ActionItem(27, UiItems.createCustomButon(MenuHeads.of(Shape.BACK, DyeColor.CYAN),
                t(DynamicLang.of("menu." + MENU + ".back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new GameUi(getPlayer()).open();
            }
        });
    }

    private void toggle(int slot, String key, Potions potion,
                        String color, String label, String description) {
        String name = t(DynamicLang.of("menu." + MENU + "." + key + ".name", "§8┃ " + color + label));
        String lore = t(DynamicLang.of("menu." + MENU + "." + key + ".lore",
                "§7» Accès " + color + "Host\n"
                        + "\n"
                        + " " + color + "┃ §f" + description + "\n"
                        + "\n"
                        + " " + color + "☰ §fStatut : %potion_" + key + "_status%\n"
                        + "\n"
                        + "§8» §fCliquez pour " + color + "activer ou désactiver§f."))
                .replace("%potion_" + key + "_status%", UiItems.onOff(getPlayer(), potion.isEnabled()));

        ItemCreator icon = new ItemCreator(potion.getItemMaterial())
                .setDurability((short) potion.getIdItem())
                .addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                .setName(name)
                .setGlow(potion.isEnabled());
        icon.setLores(Arrays.asList(lore.split("\n", -1)));

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                potion.toggleEnabled();
                openAll();
            }
        });
    }
}
