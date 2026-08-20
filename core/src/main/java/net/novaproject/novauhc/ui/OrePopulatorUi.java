package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.world.generation.UHCWorldSettings;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

public class OrePopulatorUi extends CustomInventory {

    private static final String DIAMOND_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTFlZDlhYmY1MWZlNGVhODRjZmNiMjcyOTdmMWJjNTRjZDM4MmVkZjg1ZTdiZDZlNzVlY2NhMmI4MDY2MTEifX19";
    private static final String GOLD_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmIzNWFjZDZlYTM3YWY4YmZhMjNjMjEzNjA2YzI4MjcyZTE5ZTJlNDMyZDMxYmU4OWFmNjQxYWUxYTcwNTQwNSJ9fX0=";
    private static final String IRON_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTAxODQzZWM0M2YwODhjOTYzZmZjM2UyZjcxYzY2ZTMxNTU5NDNiMTc3YTFhMzU5ODJiMTIwZjZmNjQ4MjJiYyJ9fX0=";
    private static final String LAPIS_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjY1NGI2ZDk0OWE0NmQ4NmVjMDE1NDhjODkyYTU2OGI4Y2RhZDQ2NDZjYjJlMjk2ZDBkZDU4YWY3Nzk0NzEifX19";

    private final UHCManager manager = UHCManager.get();
    private final double previousDiamond = manager.getBoost_Diamond();
    private final double previousGold = manager.getBoost_Gold();
    private final double previousIron = manager.getBoost_Iron();
    private final double previousLapis = manager.getBoost_Lapis();
    private boolean change = false;

    public OrePopulatorUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.boost-des-minerais.title", "» Boost des Minerais"));
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
        UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 26);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

        ore(11, "diamond", DIAMOND_TEXTURE, "§b", "Diamant", "%diamond_boost%", UHCWorldSettings.DIAMOND_BASE,
                manager::getBoost_Diamond, manager::setBoost_Diamond);
        ore(12, "gold", GOLD_TEXTURE, "§6", "Or", "%gold_boost%", UHCWorldSettings.GOLD_BASE,
                manager::getBoost_Gold, manager::setBoost_Gold);
        ore(14, "iron", IRON_TEXTURE, "§7", "Fer", "%iron_boost%", UHCWorldSettings.IRON_BASE,
                manager::getBoost_Iron, manager::setBoost_Iron);
        ore(15, "lapis", LAPIS_TEXTURE, "§9", "Lapis-lazuli", "%lapis_boost%", UHCWorldSettings.LAPIS_BASE,
                manager::getBoost_Lapis, manager::setBoost_Lapis);

        ItemCreator reset = new ItemCreator(Material.REDSTONE_TORCH_ON)
                .setName(t(DynamicLang.of("menu.boost-des-minerais.reset.name", "§8┃ §cValeurs par défaut")));
        reset.setLores(Arrays.asList(t(DynamicLang.of("menu.boost-des-minerais.reset.lore",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fRestaurez les quatre fréquences initiales.\n"
                        + "\n"
                        + "§8» §fCliquez pour §cutiliser§f.")).split("\n", -1)));

        addItem(new ActionItem(22, reset) {
            @Override
            public void onClick(InventoryClickEvent e) {
                manager.setBoost_Diamond(0);
                manager.setBoost_Gold(0);
                manager.setBoost_Iron(0);
                manager.setBoost_Lapis(0);
                change = false;
                openAll();
            }
        });

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.LIGHT_BLUE),
                t(DynamicLang.of("menu.boost-des-minerais.back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WorldUi(getPlayer()).open();
            }
        });
    }

    private void ore(int slot, String key, String texture, String color, String label,
                     String placeholder, double vanillaRate,
                     DoubleSupplier getter, DoubleConsumer setter) {
        String value = "×" + String.format("%.2f", vanillaRate + getter.getAsDouble());
        int percent = (int) Math.round(getter.getAsDouble() / vanillaRate * 100);

        String name = t(DynamicLang.of("menu.boost-des-minerais." + key + ".name",
                "§8┃ " + color + label)).replace(placeholder, value);

        String lore = t(DynamicLang.of("menu.boost-des-minerais." + key + ".lore",
                "§7» Accès " + color + "Host\n"
                        + "\n"
                        + " " + color + "┃ §fMultiplicateur total appliqué au\n"
                        + " " + color + "┃ §ftaux vanilla de ce minerai.\n"
                        + "\n"
                        + " " + color + "☰ §fGénération : " + placeholder + "\n"
                        + "\n"
                        + "§8» §fCliquez pour " + color + "modifier§f.")).replace(placeholder, value);

        addItem(new ActionItem(slot, UiItems.createCustomButon(texture, name,
                Arrays.asList(lore.split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 50, 10, 5, 50, 10, 5, percent, 0, 500,
                        OrePopulatorUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        setter.accept(newValue.intValue() / 100.0 * vanillaRate);
                        change = true;
                    }
                }.open();
            }
        });
    }

    @Override
    public void onClose() {
        if (change) LangManager.get().send(CoreLang.COMMON_ORE_BOOST, getPlayer());
        super.onClose();
    }
}
