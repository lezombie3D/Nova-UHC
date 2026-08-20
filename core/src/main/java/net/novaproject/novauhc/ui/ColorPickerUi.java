package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.scenario.role.camps.Camps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ColorPickerUi extends CustomInventory {

    private static final Lang COLOR_UPDATED = DynamicLang.of("ui.color.name_updated",
            "§8» §fCouleur de §e%players% §fmise à jour !");
    private static final Lang PASTILLE_UPDATED = DynamicLang.of("ui.color.pastille_updated",
            "§8» §fPastille de §e%players% §fmise à jour !");

    public enum Mode {
        NAME("menu.couleur-tab-joueur", "» Couleur TAB : %player%", "au pseudo du joueur"),
        PASTILLE("menu.pastille-tab-joueur", "» Pastille TAB : %player%", "à la pastille du joueur");

        private final String keyPrefix;
        private final String title;
        private final String appliedTo;

        Mode(String keyPrefix, String title, String appliedTo) {
            this.keyPrefix = keyPrefix;
            this.title = title;
            this.appliedTo = appliedTo;
        }

        Mode other() {
            return this == NAME ? PASTILLE : NAME;
        }
    }

    private final List<Player> targets;
    private final Mode mode;

    public ColorPickerUi(Player player, Player target) {
        this(player, List.of(target), Mode.NAME);
    }

    public ColorPickerUi(Player player, List<Player> targets) {
        this(player, targets, Mode.NAME);
    }

    public ColorPickerUi(Player player, List<Player> targets, Mode mode) {
        super(player);
        this.targets = targets;
        this.mode = mode;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(mode.keyPrefix + ".title", mode.title),
                Map.of("%player%", targets.size() == 1 ? targets.get(0).getName() : targets.size() + " joueurs"));
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
        UiItems.panes(this, DyeColor.YELLOW, 0, 8, 35);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26, 28, 34);

        color(10, "white", DyeColor.WHITE, "§f", "Blanc", ChatColor.WHITE);
        color(11, "yellow", DyeColor.YELLOW, "§e", "Jaune", ChatColor.YELLOW);
        color(12, "orange", DyeColor.ORANGE, "§6", "Orange", ChatColor.GOLD);
        color(13, "red", DyeColor.RED, "§c", "Rouge", ChatColor.RED);
        color(14, "dark-red", DyeColor.BROWN, "§4", "Rouge foncé", ChatColor.DARK_RED);
        color(15, "light-purple", DyeColor.MAGENTA, "§d", "Violet clair", ChatColor.LIGHT_PURPLE);
        color(16, "purple", DyeColor.PURPLE, "§5", "Violet", ChatColor.DARK_PURPLE);
        color(19, "aqua", DyeColor.LIGHT_BLUE, "§b", "Aqua", ChatColor.AQUA);
        color(20, "dark-aqua", DyeColor.CYAN, "§3", "Cyan", ChatColor.DARK_AQUA);
        color(21, "green", DyeColor.LIME, "§a", "Vert", ChatColor.GREEN);
        color(22, "dark-green", DyeColor.GREEN, "§2", "Vert foncé", ChatColor.DARK_GREEN);
        color(23, "gray", DyeColor.SILVER, "§7", "Gris", ChatColor.GRAY);
        color(24, "dark-gray", DyeColor.GRAY, "§8", "Gris foncé", ChatColor.DARK_GRAY);

        ItemCreator reset = new ItemCreator(Material.MILK_BUCKET)
                .setName(t(DynamicLang.of(mode.keyPrefix + ".default.name", "§8┃ §6Réinitialiser")));
        reset.setLores(Arrays.asList(t(DynamicLang.of(mode.keyPrefix + ".default.lore",
                "§7» Accès §6Tous\n"
                        + "\n"
                        + " §6┃ §fRetirez ce que vous avez appliqué " + mode.appliedTo + ".\n"
                        + "\n"
                        + "§8» §fCliquez pour §6utiliser§f.")).split("\n", -1)));

        reset.setGlow(currentColor() == null);

        addItem(new ActionItem(32, reset) {
            @Override
            public void onClick(InventoryClickEvent e) {
                apply(ChatColor.RESET);
            }
        });

        ItemCreator swap = new ItemCreator(Material.INK_SACK)
                .setDurability((short) DyeColor.PINK.getDyeData())
                .setName(t(DynamicLang.of(mode.keyPrefix + ".swap.name",
                        mode == Mode.NAME ? "§8┃ §dPastille" : "§8┃ §dCouleur du pseudo")));
        swap.setLores(Arrays.asList(t(DynamicLang.of(mode.keyPrefix + ".swap.lore",
                "§7» Accès §dTous\n"
                        + "\n"
                        + " §d┃ §fLa pastille est une puce colorée posée\n"
                        + " §d┃ §fdevant le pseudo. Elle est optionnelle.\n"
                        + "\n"
                        + "§8» §fCliquez pour §dbasculer§f.")).split("\n", -1)));

        addItem(new ActionItem(31, swap) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ColorPickerUi(getPlayer(), targets, mode.other()).open();
            }
        });

        addItem(new ActionItem(27, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.YELLOW),
                t(DynamicLang.of(mode.keyPrefix + ".back.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ColorPlayerListUi(getPlayer()).open();
            }
        });
    }

    private void color(int slot, String key, DyeColor dye, String code, String label, ChatColor applied) {
        ItemCreator icon = new ItemCreator(new ItemStack(Material.INK_SACK, 1, (short) dye.getDyeData()))
                .setName(t(DynamicLang.of(mode.keyPrefix + "." + key + ".name", "§8┃ " + code + label)));

        icon.setLores(Arrays.asList(t(DynamicLang.of(mode.keyPrefix + "." + key + ".lore",
                "§7» Accès " + code + "Tous\n"
                        + "\n"
                        + " " + code + "┃ §fAppliquez la couleur " + label + " " + mode.appliedTo + ".\n"
                        + "\n"
                        + "§8» §fCliquez pour " + code + "utiliser§f.")).split("\n", -1)));

        icon.setGlow(applied == currentColor());

        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                apply(applied);
            }
        });
    }

    private ChatColor currentColor() {
        Player target = targets.get(0);
        if (mode == Mode.PASTILLE) return DisplayService.pastilleOf(getPlayer(), target);
        UHCPlayer uhcTarget = UHCPlayerManager.get().getPlayer(target);
        return uhcTarget == null ? null : uhcTarget.getTabColorPrefs().get(getPlayer().getUniqueId());
    }

    private void apply(ChatColor color) {
        for (Player target : targets) {
            if (mode == Mode.PASTILLE) {
                DisplayService.pastilleFor(getPlayer(), target, color == ChatColor.RESET ? null : color);
            } else if (color == ChatColor.RESET) {
                DisplayService.resetColorFor(getPlayer(), target);
            } else {
                DisplayService.colorFor(getPlayer(), target, color);
            }
        }
        getPlayer().closeInventory();
        LangManager.get().send(mode == Mode.PASTILLE ? PASTILLE_UPDATED : COLOR_UPDATED, getPlayer(),
                Map.of("%players%", targets.stream().map(Player::getName).collect(Collectors.joining("§f, §e"))));
    }

    public static class ColorRoleUi extends CustomInventory {

        private static final Lang ROLE_UPDATED = DynamicLang.of("ui.color.role_updated",
                "§8» §fRôle affiché de §e%player% §fmis à jour !");
        private static final Lang NO_ROLE_MODE = DynamicLang.of("ui.color.no_role_mode",
                "§c✘ §7Aucun mode à rôles n'est actif.");

        private static final int PER_PAGE = 28;

        private final Player target;
        private int pages = 1;

        public ColorRoleUi(Player player, Player target) {
            super(player);
            this.target = target;
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.role-tab-joueur.title", "» Rôle TAB : %player%"))
                    .replace("%player%", target.getName());
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
            UiItems.panes(this, DyeColor.YELLOW, 0, 8, 45, 53);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

            pages = paginate(availableRoles(), PER_PAGE,
                    n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                    (role, slot) -> addItem(new ActionItem(slot, roleIcon(role)) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            apply(campLabel(role));
                        }
                    }));

            ItemCreator reset = new ItemCreator(Material.MILK_BUCKET)
                    .setName(t(DynamicLang.of("menu.role-tab-joueur.default.name", "§8┃ §6Réinitialiser")));
            reset.setLores(Arrays.asList(t(DynamicLang.of("menu.role-tab-joueur.default.lore",
                    "§7» Accès §6Tous\n"
                            + "\n"
                            + " §6┃ §fRetirez le rôle affiché sur ce joueur.\n"
                            + "\n"
                            + "§8» §fCliquez pour §6utiliser§f.")).split("\n", -1)));
            reset.setGlow(DisplayService.roleLabelOf(getPlayer(), target) == null);

            addItem(new ActionItem(49, reset) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    apply(null);
                }
            });

            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.YELLOW),
                    t(DynamicLang.of("menu.role-tab-joueur.previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });

            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.YELLOW),
                    t(DynamicLang.of("menu.role-tab-joueur.next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }

        private List<Role> availableRoles() {
            List<Role> roles = new ArrayList<>();
            for (Scenario scenario : ScenarioManager.get().getActiveSpecialScenarios()) {
                if (scenario instanceof ScenarioRole<?> roleScenario) {
                    roles.addAll(roleScenario.getRoleConfigs().values());
                }
            }
            roles.sort((a, b) -> ChatColor.stripColor(a.getName()).compareToIgnoreCase(ChatColor.stripColor(b.getName())));
            return roles;
        }

        private ItemCreator roleIcon(Role role) {
            ItemCreator icon = new ItemCreator(role.getIconMaterial())
                    .setName(t(DynamicLang.of("menu.role-tab-joueur.role-dummy.name", "§8┃ %role%"))
                            .replace("%role%", role.getColor() + role.getName()));

            icon.setLores(Arrays.asList(t(DynamicLang.of("menu.role-tab-joueur.role-dummy.lore",
                    "§7» Élément §adynamique\n"
                            + "\n"
                            + " §a┃ §fAffiche ce rôle au-dessus de la tête et\n"
                            + " §a┃ §fdans le tab de §f%player%§f, pour vous seul.\n"
                            + "\n"
                            + "§8» §fCliquez pour §autiliser§f.")).replace("%player%", target.getName())
                    .split("\n", -1)));

            String current = DisplayService.roleLabelOf(getPlayer(), target);
            icon.setGlow(current != null && ChatColor.stripColor(role.getName()).startsWith(current));
            return icon;
        }

        private static String campLabel(Role role) {
            String nom = ChatColor.stripColor(role.getName());
            Camps camp = role.getCamp();
            String couleur = camp == null ? null : camp.getColor();
            return couleur == null || couleur.isEmpty() ? nom : couleur + nom;
        }

        private void apply(String roleName) {
            DisplayService.roleLabelFor(getPlayer(), target, roleName);
            getPlayer().closeInventory();
            LangManager.get().send(ROLE_UPDATED, getPlayer(), Map.of("%player%", target.getName()));
        }

        public static boolean hasRoleMode() {
            for (Scenario scenario : ScenarioManager.get().getActiveSpecialScenarios()) {
                if (scenario instanceof ScenarioRole<?>) return true;
            }
            return false;
        }

        public static Lang noRoleModeMessage() {
            return NO_ROLE_MODE;
        }
    }

    public static class ColorPlayerListUi extends CustomInventory {

        private static final int PER_PAGE = 28;

        private int pages = 1;

        public ColorPlayerListUi(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.colorer-un-joueur-dans-le-tab.title", "» Colorer un joueur dans le TAB"));
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
            UiItems.panes(this, DyeColor.YELLOW, 0, 8, 45, 53);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

            List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());

            pages = paginate(online, PER_PAGE,
                    n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                    (online_player, slot) -> addItem(new ActionItem(slot, head(online_player)) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            new ColorPickerUi(getPlayer(), online_player).open();
                        }
                    }));

            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.YELLOW),
                    t(DynamicLang.of("menu.colorer-un-joueur-dans-le-tab.previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });

            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.YELLOW),
                    t(DynamicLang.of("menu.colorer-un-joueur-dans-le-tab.next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }

        private ItemCreator head(Player online) {
            ItemCreator icon = new ItemCreator(new ItemStack(Material.SKULL_ITEM, 1, (short) 3))
                    .setOwner(online.getName())
                    .setName(t(DynamicLang.of("menu.colorer-un-joueur-dans-le-tab.player-dummy.name", "§8┃ §a%pseudo%"))
                            .replace("%pseudo%", online.getName()));

            icon.setLores(Arrays.asList(t(DynamicLang.of("menu.colorer-un-joueur-dans-le-tab.player-dummy.lore",
                    "§7» Élément §adynamique\n"
                            + "\n"
                            + " §a┃ §fChoisissez la couleur affichée dans le\n"
                            + " §a┃ §ftab et au-dessus de la tête du joueur.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §aaccéder§f.")).split("\n", -1)));

            return icon;
        }
    }
}
