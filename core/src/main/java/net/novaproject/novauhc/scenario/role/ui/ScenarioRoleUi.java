package net.novaproject.novauhc.scenario.role.ui;

import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.RoleRegistration;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.camps.CampUtils;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.MenuGrid;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.VariableEditorUi.NamedConfigUi;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScenarioRoleUi<T extends Role> extends CustomInventory {

    private static final int ROLES_PER_PAGE = 15;
    private static final int ROW_FROM = 1;
    private static final int ROW_TO = 3;
    private static final int COL_FROM = 2;
    private static final int COL_TO = 6;
    private static final int SLOT_PREV = 39;
    private static final int SLOT_NEXT = 41;
    private static final String NEXT     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJmOGI2Mjc3Y2QzNjI2NjI4M2NiNWE5ZTY5NDM5NTNjNzgzZTZmZjdkNmEyZDU5ZDE1YWQwNjk3ZTkxZDQzYyJ9fX0=";
    private static final String PREVIOUS = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjc2MjMwYTBhYzUyYWYxMWU0YmM4NDAwOWM2ODkwYTQwMjk0NzJmMzk0N2I0ZjQ2NWI1YjU3MjI4ODFhYWNjNyJ9fX0=";
    private static final String BACK     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTUzYmRhMmVkNDZlZmFlMjZkNmI2ZjMyODJlMjgzOTRlZTM5ZjMzMTNjZjM5YWY4ZDllMzk3MDliODc2NDNiIn19fQ==";
    private static final String ROLE     = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTViYWNkMWQ3ZTY3NDZjN2YyNjg4ZDE4NDE3OGM2MjVlNzFjOWFhMTczMWUwMmQ3ZGZlNzg3NDlhNzU0YWY0MSJ9fX0=";

    private final ScenarioRole<T> scenario;
    private final Camps parentCamp;
    private final boolean fillerPickMode;
    private final CustomInventory parent;
    private int pages = 1;

    public ScenarioRoleUi(Player player, ScenarioRole<T> scenario, Camps parentCamp) {
        this(player, scenario, parentCamp, false, null);
    }

    public ScenarioRoleUi(Player player, ScenarioRole<T> scenario, Camps parentCamp, boolean fillerPickMode) {
        this(player, scenario, parentCamp, fillerPickMode, null);
    }

    public ScenarioRoleUi(Player player, ScenarioRole<T> scenario, Camps parentCamp, boolean fillerPickMode,
                          CustomInventory parent) {
        super(player);
        this.scenario = scenario;
        this.parentCamp = parentCamp;
        this.fillerPickMode = fillerPickMode;
        this.parent = parent;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.roles-du-camp.title", "» %camps%")).replace("%camps%", campLabel());
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
        String camp = campLabel();

        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        ItemCreator accent = UiItems.pane(DyeColor.YELLOW).setName(
                t(DynamicLang.of("menu.roles-du-camp.btn-0.name", "%couelur_of_camps%"))
                        .replace("%couelur_of_camps%", camp));
        addItem(new StaticItem(0, accent));
        addItem(new StaticItem(8, accent));
        addItem(new StaticItem(53, accent));

        addItem(new StaticItem(4, new ItemCreator(Material.INK_SACK)
                .setDurability((short) DyeColor.YELLOW.getDyeData())
                .setName(t(DynamicLang.of("menu.roles-du-camp.btn-4.name", "%camps%")).replace("%camps%", camp))));

        String summaryName = t(DynamicLang.of("menu.roles-du-camp.btn-2.name",
                "§8┃ §eResumée du camps : %camps%")).replace("%camps%", camp);
        String summaryLore = t(DynamicLang.of("menu.roles-du-camp.btn-2.lore",
                "§7» Accès §eHost\n"
                        + "\n"
                        + " §e┃ §fRésumée des rôles §aactivées §f: \n"
                        + "\n"
                        + " §e☰ §f%role%: %amount%\n"
                        + "\n"
                        + "§8» §fClique pour §eaccedez"))
                .replace("%role%", summaryLines())
                .replace("%amount%", summaryLastCount());

        addItem(new ActionItem(2, new ItemCreator(Material.NAME_TAG)
                .setName(summaryName)
                .setLores(Arrays.asList(summaryLore.split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenarioCampUi<>(getPlayer(), scenario, false, parent).open();
            }
        });

        addItem(new ActionItem(45, UiItems.createCustomButon(BACK,
                t(DynamicLang.of("menu.roles-du-camp.btn-45.name", "§8┃ §7§lRetour")),
                Arrays.asList(t(DynamicLang.of("menu.roles-du-camp.btn-45.lore", "%couelur_of_camps%"))
                        .replace("%couelur_of_camps%", camp).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenarioCampUi<>(getPlayer(), scenario, fillerPickMode, parent).open();
            }
        });

        String nameTemplate = t(DynamicLang.of("menu.roles-du-camp.btn-10.name", "%role% : %amout%"));
        List<String> loreTemplate = Arrays.asList(t(DynamicLang.of("menu.roles-du-camp.btn-10.lore",
                "§7» Accès §eHost\n"
                        + "\n"
                        + " §e┃ §f%short_desc%\n"
                        + "\n"
                        + "§8» §fClique gauche pour §a+1§f.\n"
                        + "§8» §fClique droit pour §c-1§f.\n"
                        + "§8» §fClique Shift-Clique pour §eModifier§f.")).split("\n", -1));

        pages = paginate(getDisplayRoles(), ROLES_PER_PAGE,
                n -> MenuGrid.grid(ROW_FROM, ROW_TO, COL_FROM, COL_TO, true, n),
                (role, slot) -> addItem(new ActionItem(slot, roleIcon(role, nameTemplate, loreTemplate)) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        clickRole(e, role);
                    }
                }));

        addPagers();
    }

    private ItemCreator roleIcon(T role, String nameTemplate, List<String> loreTemplate) {
        int amount = scenario.getDefault_roles().get(role);
        Class<? extends T> roleClass = (Class<? extends T>) role.getClass();
        Class<? extends T> partnerClass = fillerPickMode ? null : scenario.partnerClassOf(roleClass);
        T partner = partnerClass != null ? scenario.getRoleConfig(partnerClass) : null;

        ItemCreator icon = UiItems.createCustomButon(ROLE, " ", null);
        applyBaseItem(icon, role, amount);

        String label = partner != null
                ? role.getColor() + role.getName() + " §7& " + partner.getColor() + partner.getName()
                : role.getColor() + role.getName();

        icon.setName(nameTemplate.replace("%role%", label).replace("%amout%", String.valueOf(amount)));
        icon.setLores(renderLore(loreTemplate, role));

        if (partner != null) {
            icon.addLore("")
                    .addLore(t(UiLang.ROLE_PAIR_LINKED))
                    .addLore(t(UiLang.ROLE_PAIR_CONFIG_LEFT, Map.of("%name%", role.getName())))
                    .addLore(t(UiLang.ROLE_PAIR_CONFIG_RIGHT, Map.of("%name%", partner.getName())));
        }

        RoleRegistration reg = scenario.getRegistration(roleClass);
        if (reg != null && reg.getMinPlayers() > 0) {
            icon.addLore("").addLore(t(UiLang.ROLE_MIN_PLAYERS, Map.of("%min%", reg.getMinPlayers())));
        }
        if (roleClass.equals(scenario.getFillerRoleClass())) {
            icon.addLore("").addLore(t(UiLang.ROLE_FILLER_CURRENT));
        }
        if (fillerPickMode) {
            icon.addLore("").addLore(t(UiLang.ROLE_FILLER_PICK_HINT));
        }
        return icon;
    }

    private void applyBaseItem(ItemCreator icon, T role, int amount) {
        ItemStack source = amount > 0
                ? role.getItem().setAmount(amount).getItemstack()
                : campSkull(role).getItemstack();
        icon.setMaterial(source.getType());
        icon.setDurability(source.getDurability());
        icon.setAmount(Math.max(1, source.getAmount()));
        if (source.getType() == Material.SKULL_ITEM && source.getItemMeta() instanceof SkullMeta) {
            icon.setSkullMeta((SkullMeta) source.getItemMeta());
        }
    }

    private List<String> renderLore(List<String> template, T role) {
        List<String> out = new ArrayList<>();
        if (template == null) return out;
        String desc = shortDescription(role);
        boolean skipBlank = false;
        for (String line : template) {
            if (fillerPickMode && line.startsWith("§8»")) continue;
            if (line.contains("%short_desc%")) {
                if (desc.isEmpty()) {
                    skipBlank = true;
                    continue;
                }
                out.add(line.replace("%short_desc%", desc));
                continue;
            }
            if (skipBlank && line.trim().isEmpty()) {
                skipBlank = false;
                continue;
            }
            skipBlank = false;
            out.add(line);
        }
        return out;
    }

    private String shortDescription(T role) {
        List<String> lines = role.getDescriptionLines(getPlayer());
        return lines == null || lines.isEmpty() ? "" : lines.get(0);
    }

    private void clickRole(InventoryClickEvent e, T role) {
        Class<? extends T> roleClass = (Class<? extends T>) role.getClass();
        if (fillerPickMode) {
            scenario.setFillerRoleClass(roleClass);
            LangManager.get().send(UiLang.ROLE_FILLER_SET, getPlayer(), Map.of("%name%", role.getName()));
            new ScenarioCampUi<>(getPlayer(), scenario, false, parent).open();
            return;
        }
        int current = scenario.getDefault_roles().get(role);
        if (e.isShiftClick()) {
            Class<? extends T> partnerClass = scenario.partnerClassOf(roleClass);
            T partner = partnerClass != null ? scenario.getRoleConfig(partnerClass) : null;
            if (partner != null && e.isRightClick()) {
                new NamedConfigUi(getPlayer(), partner, this).open();
            } else {
                new NamedConfigUi(getPlayer(), role, this).open();
            }
        } else if (e.isLeftClick()) {
            scenario.incrementRole(roleClass);
            open();
        } else if (e.isRightClick() && current > 0) {
            scenario.decrementRole(roleClass);
            open();
        }
    }

    private void addPagers() {
        if (pages <= 1) return;
        int page = Math.max(1, getActual_category());
        if (page > 1) {
            addItem(new ActionItem(SLOT_PREV, UiItems.createCustomButon(PREVIOUS,
                    LangManager.get().get(CoreLang.COMMON_PAGE_PREVIOUS, getPlayer()), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });
        }
        if (page < pages) {
            addItem(new ActionItem(SLOT_NEXT, UiItems.createCustomButon(NEXT,
                    LangManager.get().get(CoreLang.COMMON_PAGE_NEXT, getPlayer()), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }
    }

    private String summaryLines() {
        List<T> active = activeRoles();
        if (active.isEmpty()) return t(UiLang.SCENARVAR_CAMP_NO_ACTIVE);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < active.size(); i++) {
            T role = active.get(i);
            sb.append(role.getColor()).append(role.getName());
            if (i < active.size() - 1) {
                sb.append("§f: ").append(scenario.getDefault_roles().get(role)).append("\n §e☰ §f");
            }
        }
        return sb.toString();
    }

    private String summaryLastCount() {
        List<T> active = activeRoles();
        if (active.isEmpty()) return "0";
        return String.valueOf(scenario.getDefault_roles().get(active.get(active.size() - 1)));
    }

    private List<T> activeRoles() {
        List<T> active = new ArrayList<>();
        for (T role : getRolesForCamp()) {
            Integer amount = scenario.getDefault_roles().get(role);
            if (amount != null && amount > 0) active.add(role);
        }
        return active;
    }

    private String campLabel() {
        if (parentCamp == null) return "§8—";
        return parentCamp.getColor() + parentCamp.getName();
    }

    private ItemCreator campSkull(T role) {
        Camps camp = parentCamp != null ? parentCamp : role.getCamp();
        if (camp == null) return role.getItem();
        return new ItemCreator(camp.getSkull());
    }

    private List<T> getDisplayRoles() {
        List<T> roles = getRolesForCamp();
        if (fillerPickMode) return roles;
        List<T> display = new ArrayList<>();
        Set<Class<?>> consumed = new HashSet<>();
        for (T role : roles) {
            if (consumed.contains(role.getClass())) continue;
            display.add(role);
            Class<? extends T> partner = scenario.partnerClassOf((Class<? extends T>) role.getClass());
            if (partner != null) consumed.add(partner);
        }
        return display;
    }

    private List<T> getRolesForCamp() {
        List<T> roles = new ArrayList<>();
        for (Map.Entry<T, Integer> entry : scenario.getDefault_roles().entrySet()) {
            T role = entry.getKey();
            if (CampUtils.roleBelongsToCamp(role, parentCamp, scenario.getCamps())) {
                roles.add(role);
            }
        }
        return roles;
    }
}
