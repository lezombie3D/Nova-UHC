package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;

public class BorderConfig extends CustomInventory {

    private static final String MENU = "menu.configuration-de-la-bordure.";

    public BorderConfig(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(MENU + "title", "» Configuration de la Bordure"));
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
        UHCManager uhc = UHCManager.get();
        double liveSize = Common.get().getArena().getWorldBorder().getSize();

        UiItems.panes(this, DyeColor.YELLOW, 0, 8, 26);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

        addNumber(10, tile("generation-size", Material.STAINED_GLASS,
                        "§8┃ §aTaille à la génération",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fTaille donnée à la bordure quand\n"
                                + " §a┃ §fl'arène est §agénérée§f.\n"
                                + "\n"
                                + " §a☰ §fTaille : %border_generation_size%\n"
                                + "\n"
                                + " §c☰ §fNe change §crien §fà la partie en cours :\n"
                                + " §c☰ §frégénérez la map, ou utilisez la tuile\n"
                                + " §c☰ §fTaille actuelle juste à côté.\n"
                                + "\n"
                                + "§8» §fCliquez pour §amodifier§f.",
                        "%border_generation_size%", String.valueOf((int) uhc.getBorderDefaultSize()))
                        .setDurability((short) 5),
                (int) uhc.getBorderDefaultSize(), 500, 250, 100, 250, 4000, VariableType.INTEGER,
                v -> uhc.setBorderDefaultSize(v.doubleValue()));

        addNumber(11, tile("current-size", Material.STAINED_GLASS,
                        "§8┃ §cTaille actuelle",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fDéplace la bordure de l'arène\n"
                                + " §c┃ §f§cimmédiatement§f, partie en cours comprise.\n"
                                + "\n"
                                + " §c☰ §fTaille : %border_current_size%\n"
                                + "\n"
                                + "§8» §fCliquez pour §cmodifier§f.",
                        "%border_current_size%", String.valueOf((int) liveSize))
                        .setDurability((short) 14),
                (int) liveSize, 500, 250, 100, 250, 4000, VariableType.INTEGER,
                v -> Common.get().getArena().getWorldBorder().setSize(v.doubleValue()));

        addNumber(12, tile("final-size", Material.BARRIER,
                        "§8┃ §eBordure finale",
                        "§7» Accès §eHost\n"
                                + "\n"
                                + " §e┃ §fDéfinissez la taille à laquelle\n"
                                + " §e┃ §fla réduction doit s’arrêter.\n"
                                + "\n"
                                + " §e☰ §fTaille : %border_final_size%\n"
                                + "\n"
                                + "§8» §fCliquez pour §emodifier§f.",
                        "%border_final_size%", String.valueOf((int) uhc.getTargetSize())),
                (int) uhc.getTargetSize(), 100, 50, 10, 10, 500, VariableType.INTEGER,
                v -> uhc.setTargetSize(v.doubleValue()));

        addNumber(14, tile("speed", Material.WATCH,
                        "§8┃ §bVitesse de la bordure",
                        "§7» Accès §bHost\n"
                                + "\n"
                                + " §b┃ §fRéglez la vitesse de réduction\n"
                                + " §b┃ §fen blocs par seconde.\n"
                                + "\n"
                                + " §b☰ §fVitesse : %border_speed%\n"
                                + "\n"
                                + "§8» §fCliquez pour §bmodifier§f.",
                        "%border_speed%", uhc.getReducSpeed() + " bloc(s)/s"),
                (int) uhc.getReducSpeed(), 10, 5, 1, 1, 15, VariableType.INTEGER,
                v -> uhc.setReducSpeed(v.longValue()));

        addNumber(15, tile("damage", Material.REDSTONE,
                        "§8┃ §dDégâts de bordure",
                        "§7» Accès §dHost\n"
                                + "\n"
                                + " §d┃ §fRéglez les dégâts subis par un joueur\n"
                                + " §d┃ §frestant en dehors de la zone.\n"
                                + "\n"
                                + " §d☰ §fDégâts : %border_damage%\n"
                                + "\n"
                                + "§8» §fCliquez pour §dmodifier§f.",
                        "%border_damage%", String.valueOf(uhc.getBorderDamageAmount())),
                uhc.getBorderDamageAmount(), 1.0, 0.5, 0.1, 0, 4, VariableType.DOUBLE,
                v -> uhc.setBorderDamageAmount(v.doubleValue()));

        addNumber(16, tile("buffer", Material.SLIME_BALL,
                        "§8┃ §6Zone tampon",
                        "§7» Accès §6Host\n"
                                + "\n"
                                + " §6┃ §fDéfinissez la marge avant l’application\n"
                                + " §6┃ §fdes dégâts de bordure.\n"
                                + "\n"
                                + " §6☰ §fDistance : %border_buffer%\n"
                                + "\n"
                                + "§8» §fCliquez pour §6modifier§f.",
                        "%border_buffer%", uhc.getBorderDamageBuffer() + " bloc(s)"),
                uhc.getBorderDamageBuffer(), 10.0, 5.0, 1.0, 0, 100, VariableType.DOUBLE,
                v -> uhc.setBorderDamageBuffer(v.doubleValue()));

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.YELLOW),
                t(DynamicLang.of(MENU + "back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new GameUi(getPlayer()).open();
            }
        });
    }

    private ItemCreator tile(String key, Material material, String name, String lore,
                             String token, String display) {
        Map<String, Object> extra = Map.of(token, display);
        return new ItemCreator(material)
                .setName(t(DynamicLang.of(MENU + key + ".name", name), extra))
                .setLores(Arrays.asList(t(DynamicLang.of(MENU + key + ".lore", lore), extra).split("\n", -1)));
    }

    private void addNumber(int slot, ItemCreator icon, Number current,
                           Number big, Number mid, Number small,
                           double min, double max, VariableType type, Consumer<Number> apply) {
        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), big, mid, small, big, mid, small,
                        current, min, max, BorderConfig.this, type) {
                    @Override
                    public void onChange(Number newValue) {
                        apply.accept(newValue);
                    }
                }.open();
            }
        });
    }
}
