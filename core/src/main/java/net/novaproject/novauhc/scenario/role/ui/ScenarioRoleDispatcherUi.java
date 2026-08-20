package net.novaproject.novauhc.scenario.role.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.scenario.Scenario.ScenarioVariableUi;
import net.novaproject.novauhc.scenario.random.ScenarioRandomEventsUi;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.CampUtils;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableDescriptor;
import net.novaproject.novauhc.utils.variable.VariableType;
import net.novaproject.novauhc.utils.variable.Variables;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ScenarioRoleDispatcherUi<T extends Role> extends CustomInventory {

    private static final String BACK_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGRmMjlmYWE2N2FlNWI2MTNkYzY1NGRhNDMzMWI5ODczM2RhOTczNGE3NjJjOGVlOTFiYTE0NmI2MzQxIn19fQ==";
    private static final String COMPOSITION_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTU5YjgzYjkzZDQ5MThjZWUxMDNhMWVkNTdiYzJjZWQwM2I0NzU0NzUxMmMxZjdlYTE4MWVmZjU2MGI5ODAzMiJ9fX0=";

    private static final int[] CAMP_SLOTS = {20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
    private static final DyeColor[] CAMP_FALLBACK_COLORS = {
            DyeColor.LIGHT_BLUE, DyeColor.LIME, DyeColor.RED, DyeColor.ORANGE, DyeColor.CYAN,
            DyeColor.YELLOW, DyeColor.MAGENTA, DyeColor.PURPLE, DyeColor.PINK, DyeColor.GREEN};

    private final ScenarioRole<T> scenario;

    public ScenarioRoleDispatcherUi(Player player, ScenarioRole<T> scenario) {
        super(player);
        this.scenario = scenario;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.mode-dashboard.title", "» Mode de Jeu : %mode_de_jeu%"),
                Map.of("%mode_de_jeu%", scenario.getName()));
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
        UiItems.panes(this, DyeColor.WHITE, 0, 1, 7, 8, 9, 17, 36, 44, 46, 52, 53);

        List<Camps> camps = mainCamps();

        for (int i = 0; i < CAMP_SLOTS.length && i < camps.size(); i++) {
            Camps camp = camps.get(i);
            addItem(new ActionItem(CAMP_SLOTS[i], campIcon(CAMP_SLOTS[i], CAMP_FALLBACK_COLORS[i], camp)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ScenarioRoleUi<>(getPlayer(), scenario, camp, false, ScenarioRoleDispatcherUi.this).open();
                }
            });
        }

        ItemCreator composition = UiItems.createCustomButon(COMPOSITION_TEXTURE,
                t(DynamicLang.of("menu.mode-dashboard.btn-4.name", "§8┃ §cCompositions")),
                Arrays.asList(t(DynamicLang.of("menu.mode-dashboard.btn-4.lore",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fAffiches ci-dessous la §ccompostions\n"
                                + " §c┃ §fde la partie par §ccamps§f: \n"
                                + "\n"
                                + " §c☰ §f%camps%§f: %nb_roles_camps%\n"
                                + "\n"
                                + "§8» §fCliquez pour avoir le §cdetails§f.\n"
                                + "§8» §fDrop pour videz la §ccompositions§f."),
                        Map.of("%camps%", compositionLines(camps),
                                "%nb_roles_camps%", compositionLastCount(camps))).split("\n", -1)));

        addItem(new ActionItem(4, composition) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenarioCampUi<>(getPlayer(), scenario, ScenarioRoleDispatcherUi.this).open();
            }
        });

        ItemCreator filler = new ItemCreator(Material.PAPER)
                .setName(t(DynamicLang.of("menu.mode-dashboard.btn-13.name", "§8┃ §aRole de Remplissage")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.mode-dashboard.btn-13.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fCliquez-ici pour §asélectionner §fun §arôle §fqui seras\n"
                                + " §a┃ §fdistribuer §aX §ffois par §arôle manquant\n"
                                + " §a┃ §fdans la §acomposition §fde la §apartie.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §aaccédez§f.")).split("\n", -1)));

        addItem(new ActionItem(13, filler) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenarioCampUi<>(getPlayer(), scenario, true, ScenarioRoleDispatcherUi.this).open();
            }
        });

        ItemCreator config = new ItemCreator(Material.DIODE)
                .setName(t(DynamicLang.of("menu.mode-dashboard.btn-47.name", "§8┃ §3Configuration")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.mode-dashboard.btn-47.lore",
                        "§7» Accès §3Host\n"
                                + "\n"
                                + " §3┃ §fVous permet de §3Configurer en profondeur\n"
                                + " §3┃ §fchaque élément du §3Mode §fde §3Jeu.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §3accédez§f.")).split("\n", -1)));

        addItem(new ActionItem(47, config) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenarioVariableUi(getPlayer(), scenario, ScenarioRoleDispatcherUi.this).open();
            }
        });

        if (hasRandomEvents()) {
            ItemCreator events = new ItemCreator(Material.NAME_TAG)
                    .setName(t(DynamicLang.of("menu.mode-dashboard.btn-49.name", "§8┃ §5Evenement Aléatoires")))
                    .setLores(Arrays.asList(t(DynamicLang.of("menu.mode-dashboard.btn-49.lore",
                            "§7» Accès §5Host\n"
                                    + "\n"
                                    + " §5┃ §fVous permez de §5Configurer les différants\n"
                                    + " §5┃ §5Evenement Aléatoires §fdu §5Mode §fde §5Jeu§f.\n"
                                    + "\n"
                                    + "§8» §fCliquez pour y §5accédez§f.")).split("\n", -1)));

            addItem(new ActionItem(49, events) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new ScenarioRandomEventsUi(getPlayer(), scenario, ScenarioRoleDispatcherUi.this).open();
                }
            });
        }

        addItem(new ActionItem(45, UiItems.createCustomButon(BACK_TEXTURE,
                t(DynamicLang.of("menu.mode-dashboard.btn-45.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenariosUi(getPlayer(), true).open();
            }
        });
    }

    private ItemCreator campIcon(int slot, DyeColor fallback, Camps camp) {
        String name = t(DynamicLang.of("menu.mode-dashboard.btn-" + slot + ".name", "%camps%"),
                Map.of("%camps%", campLabel(camp)));

        ItemStack source = camp.getSkull() != null ? camp.getSkull() : camp.getItem();
        ItemCreator icon = source != null
                ? new ItemCreator(source)
                : new ItemCreator(Material.INK_SACK).setDurability((short) fallback.getDyeData());

        return icon.setName(name);
    }

    private String campLabel(Camps camp) {
        return camp.getColor() + camp.getName() + LangManager.get().get(
                UiLang.SCENARVAR_CAMP_COUNTER,
                getPlayer(),
                Map.of("%active%", String.valueOf(activeRoles(camp)),
                       "%total%", String.valueOf(totalSlots(camp)))
        );
    }

    private String compositionLines(List<Camps> camps) {
        if (camps.isEmpty()) return "§8—";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camps.size(); i++) {
            Camps camp = camps.get(i);
            sb.append(camp.getColor()).append(camp.getName());
            if (i < camps.size() - 1) {
                sb.append("§f: ").append(totalSlots(camp)).append("\n §c☰ §f");
            }
        }
        return sb.toString();
    }

    private String compositionLastCount(List<Camps> camps) {
        return camps.isEmpty() ? "0" : String.valueOf(totalSlots(camps.get(camps.size() - 1)));
    }

    private int activeRoles(Camps camp) {
        int active = 0;
        for (T role : rolesInCamp(camp)) {
            Integer amount = scenario.getDefault_roles().get(role);
            if (amount != null && amount > 0) active++;
        }
        return active;
    }

    private int totalSlots(Camps camp) {
        int total = 0;
        for (T role : rolesInCamp(camp)) {
            Integer amount = scenario.getDefault_roles().get(role);
            if (amount != null) total += amount;
        }
        return total;
    }

    private List<T> rolesInCamp(Camps camp) {
        List<T> list = new ArrayList<>();
        for (T role : scenario.getDefault_roles().keySet()) {
            if (CampUtils.roleBelongsToCamp(role, camp, scenario.getCamps())) list.add(role);
        }
        return list;
    }

    private List<Camps> mainCamps() {
        List<Camps> list = new ArrayList<>();
        for (Camps camp : scenario.getCamps()) {
            if (camp.isMainCamp()) list.add(camp);
        }
        return list;
    }

    private boolean hasRandomEvents() {
        for (VariableDescriptor d : Variables.of(scenario)) {
            if (d.type() == VariableType.RANDOM_EVENT) return true;
        }
        return false;
    }
}
