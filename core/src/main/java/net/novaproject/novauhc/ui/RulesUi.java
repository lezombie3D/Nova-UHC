package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.ui.config.Enchants;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RulesUi extends CustomInventory {

    private static final String MENU = "menu.regles-de-la-partie.";

    private final CustomInventory parent;

    public RulesUi(Player player) {
        this(player, null);
    }

    public RulesUi(Player player, CustomInventory parent) {
        super(player);
        this.parent = parent;
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(MENU + "title", "» Règles de la partie"));
    }

    @Override
    public int getLines() {
        return 4;
    }

    @Override
    public boolean isRefreshAuto() {
        return true;
    }

    @Override
    public void setup() {
        UHCManager m = UHCManager.get();

        Map<String, Object> ph = new LinkedHashMap<>();
        ph.put("%rules_mode%", lore(mode(m)));
        ph.put("%rules_timers%", lore(timers(m)));
        ph.put("%rules_border%", lore(border(m)));
        ph.put("%rules_combat%", lore(combat(m)));
        ph.put("%rules_limits%", lore(limits(m)));
        ph.put("%rules_enchants%", enchantsSummary());
        ph.put("%rules_scenarios%", scenariosSummary());
        ph.put("%rules_special%", specialSummary());

        UiItems.panes(this, DyeColor.RED, 0, 8, 27, 35);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26, 28, 34);

        addSection(4, UiItems.createCustomButon(MenuHeads.GAMEMODE,
                        t(DynamicLang.of(MENU + "mode.name", "§8┃ §aMode de jeu"), ph),
                        lines(MENU + "mode.lore",
                                "§7» Accès §aHost\n\n §a┃ §fAffichez le mode sélectionné et son état.\n\n §a☰ §f%rules_mode%\n\n§8» §fCliquez pour §adétailler§f.", ph)),
                "§8» §aMode de jeu", DyeColor.LIME,
                () -> kv(Material.NAME_TAG, "§a", mode(UHCManager.get())));

        addSection(10, new ItemCreator(Material.WATCH)
                        .setName(t(DynamicLang.of(MENU + "timers.name", "§8┃ §cTimers"), ph))
                        .setLores(lines(MENU + "timers.lore",
                                "§7» Accès §cHost\n\n §c┃ §fConsultez les timers importants de la partie.\n\n §c☰ §f%rules_timers%\n\n§8» §fCliquez pour §cdétailler§f.", ph)),
                "§8» §cTimers", DyeColor.RED,
                () -> kv(Material.WATCH, "§c", timers(UHCManager.get())));

        addSection(11, new ItemCreator(Material.BARRIER)
                        .setName(t(DynamicLang.of(MENU + "border.name", "§8┃ §eBordure"), ph))
                        .setLores(lines(MENU + "border.lore",
                                "§7» Accès §eHost\n\n §e┃ §fConsultez ses tailles, sa vitesse et son état.\n\n §e☰ §f%rules_border%\n\n§8» §fCliquez pour §edétailler§f.", ph)),
                "§8» §eBordure", DyeColor.YELLOW,
                () -> kv(Material.BARRIER, "§e", border(UHCManager.get())));

        addSection(12, new ItemCreator(Material.DIAMOND_SWORD)
                        .setName(t(DynamicLang.of(MENU + "combat.name", "§8┃ §bCombat"), ph))
                        .setLores(lines(MENU + "combat.lore",
                                "§7» Accès §bHost\n\n §b┃ §fConsultez les règles globales de combat.\n\n §b☰ §f%rules_combat%\n\n§8» §fCliquez pour §bdétailler§f.", ph)),
                "§8» §bCombat", DyeColor.LIGHT_BLUE,
                () -> kv(Material.DIAMOND_SWORD, "§b", combat(UHCManager.get())));

        addSection(13, new ItemCreator(Material.DIAMOND)
                        .setName(t(DynamicLang.of(MENU + "limits.name", "§8┃ §dLimites"), ph))
                        .setLores(lines(MENU + "limits.lore",
                                "§7» Accès §dHost\n\n §d┃ §fConsultez les limites de ressources et d’équipement.\n\n §d☰ §f%rules_limits%\n\n§8» §fCliquez pour §ddétailler§f.", ph)),
                "§8» §dLimites", DyeColor.MAGENTA,
                () -> kv(Material.DIAMOND, "§d", limits(UHCManager.get())));

        addSection(14, new ItemCreator(Material.ENCHANTED_BOOK)
                        .setName(t(DynamicLang.of(MENU + "enchants.name", "§8┃ §6Enchantements max"), ph))
                        .setLores(lines(MENU + "enchants.lore",
                                "§7» Accès §6Host\n\n §6┃ §fConsultez chaque limite d’enchantement.\n\n §6☰ §f%rules_enchants%\n\n§8» §fCliquez pour §6détailler§f.", ph)),
                "§8» §6Enchantements max", DyeColor.ORANGE, this::enchantEntries);

        addSection(15, UiItems.createCustomButon(MenuHeads.HEADER_SCENARIOS,
                        t(DynamicLang.of(MENU + "scenarios.name", "§8┃ §2Scénarios actifs"), ph),
                        lines(MENU + "scenarios.lore",
                                "§7» Accès §2Host\n\n §2┃ §fConsultez les scénarios classiques actifs.\n\n §2☰ §f%rules_scenarios%\n\n§8» §fCliquez pour §2détailler§f.", ph)),
                "§8» §2Scénarios actifs", DyeColor.GREEN, () -> scenarioEntries(false));

        addSection(16, new ItemCreator(Material.NETHER_STAR)
                        .setName(t(DynamicLang.of(MENU + "special.name", "§8┃ §3Scénario spécial"), ph))
                        .setLores(lines(MENU + "special.lore",
                                "§7» Accès §3Host\n\n §3┃ §fConsultez le mode spécial sélectionné.\n\n §3☰ §f%rules_special%\n\n§8» §fCliquez pour §3détailler§f.", ph)),
                "§8» §3Scénario spécial", DyeColor.CYAN, () -> scenarioEntries(true));

        ItemCreator exit = parent != null
                ? UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.RED),
                t(DynamicLang.of(MENU + "back.name", "§8┃ §7§lRetour")), null)
                : new ItemCreator(Material.WOOD_DOOR)
                .setName(t(DynamicLang.of(MENU + "close.name", "§8┃ §4Fermer"), ph))
                .setLores(lines(MENU + "close.lore",
                        "§7» Accès §4Host\n\n §4┃ §fFermez l’écran des règles.\n\n§8» §fCliquez pour §4utiliser§f.", ph));

        addItem(new ActionItem(22, exit) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (parent != null) parent.open();
                else getPlayer().closeInventory();
            }
        });
    }

    private void addSection(int slot, ItemCreator icon, String title, DyeColor dye,
                            Supplier<List<ItemCreator>> entries) {
        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new Detail(getPlayer(), title, dye, entries,
                        t(DynamicLang.of(MENU + "back.name", "§8┃ §7§lRetour"))).open();
            }
        });
    }

    private List<String> lines(String key, String fallback, Map<String, Object> ph) {
        return Arrays.asList(t(DynamicLang.of(key, fallback), ph).split("\n", -1));
    }

    private String lore(Map<String, String> entries) {
        return entries.entrySet().stream()
                .map(en -> "§7" + en.getKey() + " : §f" + en.getValue())
                .collect(Collectors.joining("\n "));
    }

    private List<ItemCreator> kv(Material material, String color, Map<String, String> entries) {
        List<ItemCreator> items = new ArrayList<>();
        entries.forEach((label, value) -> items.add(new ItemCreator(material)
                .setName("§8┃ §f" + label)
                .setLores(Arrays.asList("", " " + color + "☰ §f" + value, ""))));
        return items;
    }

    private Map<String, String> mode(UHCManager m) {
        String special = specialName();
        Map<String, String> out = new LinkedHashMap<>();
        out.put("Mode", special != null ? special : m.getTeam_size() == 1 ? "Solo" : "Team " + m.getTeam_size());
        out.put("Slots", String.valueOf(m.getSlot()));
        out.put("État", prettyState(m.getGameState()));
        return out;
    }

    private Map<String, String> timers(UHCManager m) {
        int now = Math.max(m.getTimer(), 0);
        Map<String, String> out = new LinkedHashMap<>();
        out.put("Temps écoulé", m.getTimerFormatted());
        out.put("Invulnérabilité", TextUtils.getFormattedTime(m.getInvulnerabilityDuration())
                + statusSuffix(now, m.getInvulnerabilityDuration()));
        out.put("PvP", TextUtils.getFormattedTime(m.getPvpTimer()) + statusSuffix(now, m.getPvpTimer()));
        out.put("Meetup", TextUtils.getFormattedTime(m.getBorderTimer()) + statusSuffix(now, m.getBorderTimer()));
        return out;
    }

    private Map<String, String> border(UHCManager m) {
        Map<String, String> out = new LinkedHashMap<>();
        World arena = Common.get().getArena();
        if (arena == null) {
            out.put("Bordure", "§8indisponible");
            return out;
        }
        WorldBorder wb = arena.getWorldBorder();
        out.put("Taille actuelle", String.valueOf((int) wb.getSize()));
        out.put("Centre", (int) wb.getCenter().getX() + ", " + (int) wb.getCenter().getZ());
        out.put("Taille cible", String.valueOf((int) m.getTargetSize()));
        out.put("Vitesse", m.getReducSpeed() + " bloc/s");
        return out;
    }

    private Map<String, String> combat(UHCManager m) {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("Force", "§c" + (int) (m.getGlobalForcePercent() * 100) + "%");
        out.put("Critique", "§e" + (int) (m.getGlobalForceCriticPercent() * 100) + "%");
        out.put("Résistance", "§a" + (int) (m.getGlobalResistancePercent() * 100) + "%");
        return out;
    }

    private Map<String, String> limits(UHCManager m) {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("Diamants minables", String.valueOf(m.getDiamondLimit()));
        out.put("Armure en diamant", String.valueOf(m.getDiamondArmor()));
        out.put("Protection max", String.valueOf(m.getProtectionMax()));
        return out;
    }

    private String enchantsSummary() {
        int changed = 0;
        for (Enchants e : Enchants.values()) {
            if (e.getConfigValue() != e.getDefaultValue()) changed++;
        }
        return "§7Enchantements : §f" + Enchants.values().length
                + "\n §7Modifiés : §f" + changed;
    }

    private List<ItemCreator> enchantEntries() {
        List<ItemCreator> items = new ArrayList<>();
        for (Enchants e : Enchants.values()) {
            boolean changed = e.getConfigValue() != e.getDefaultValue();
            items.add(new ItemCreator(Material.ENCHANTED_BOOK)
                    .setName("§8┃ §f" + shortName(e.getName()))
                    .setLores(Arrays.asList("",
                            " §6☰ §fNiveau max : §6" + e.getConfigValue(),
                            " §6☰ §fDéfaut : §7" + e.getDefaultValue(),
                            ""))
                    .setGlow(changed));
        }
        return items;
    }

    private String scenariosSummary() {
        List<String> active = ScenarioManager.get().getActiveScenarios().stream()
                .filter(s -> !s.isSpecial())
                .map(Scenario::getName)
                .collect(Collectors.toList());
        if (active.isEmpty()) return "§8aucun scénario actif";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < active.size(); i++) {
            if (i == 0) sb.append("§f");
            else sb.append(i % 3 == 0 ? "\n §f" : "§7, §f");
            sb.append(active.get(i));
        }
        return sb.toString();
    }

    private String specialSummary() {
        String special = specialName();
        return special == null ? "§8aucun scénario spécial actif" : "§7Actif : §f" + special;
    }

    private String specialName() {
        List<Scenario> specials = ScenarioManager.get().getActiveSpecialScenarios();
        return specials.isEmpty() ? null : specials.get(0).getName();
    }

    private List<ItemCreator> scenarioEntries(boolean special) {
        List<ItemCreator> items = new ArrayList<>();
        for (Scenario s : ScenarioManager.get().getActiveScenarios()) {
            if (s.isSpecial() != special) continue;
            ItemStack real = s.getItem().getItemstack();
            ItemCreator icon = new ItemCreator(real.getType())
                    .setDurability(real.getDurability())
                    .setName("§8┃ §f" + s.getName());
            icon.addLore("");
            for (String wrapped : TextUtils.wrap(s.getDescription(getPlayer()), "  §8┃ §f")) {
                icon.addLore(wrapped);
            }
            icon.addLore("");
            items.add(icon);
        }
        if (items.isEmpty()) {
            items.add(new ItemCreator(Material.BARRIER)
                    .setName(special ? "§8┃ §7Aucun scénario spécial" : "§8┃ §7Aucun scénario actif"));
        }
        return items;
    }

    private String shortName(String name) {
        return ChatColor.stripColor(name).replace("┃", "").trim();
    }

    private String statusSuffix(int now, int target) {
        return now < target ? " §7(dans " + TextUtils.getFormattedTime(target - now) + ")" : " §a✓";
    }

    private String prettyState(UHCManager.GameState s) {
        return switch (s) {
            case LOBBY -> "Lobby";
            case SCATTERING -> "Distribution";
            case INGAME -> "En jeu";
            case ENDING -> "Fin de partie";
        };
    }

    private final class Detail extends CustomInventory {

        private final String title;
        private final DyeColor dye;
        private final Supplier<List<ItemCreator>> entries;
        private final String backName;
        private int lines = -1;

        private Detail(Player player, String title, DyeColor dye,
                       Supplier<List<ItemCreator>> entries, String backName) {
            super(player);
            this.title = title;
            this.dye = dye;
            this.entries = entries;
            this.backName = backName;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public int getLines() {
            if (lines < 0) {
                lines = Math.min(6, 2 + (Math.max(entries.get().size(), 1) + 6) / 7);
            }
            return lines;
        }

        @Override
        public boolean isRefreshAuto() {
            return true;
        }

        @Override
        public void setup() {
            int lines = getLines();
            int last = lines * 9 - 9;
            for (int col = 0; col < 9; col++) {
                UiItems.panes(this, dye, col);
                if (col != 4) UiItems.panes(this, dye, last + col);
            }

            List<ItemCreator> content = entries.get();
            int capacity = (lines - 2) * 7;
            for (int i = 0; i < content.size() && i < capacity; i++) {
                addItem(new StaticItem(9 + (i / 7) * 9 + 1 + i % 7, content.get(i)));
            }

            addItem(new ActionItem(last + 4,
                    UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, dye), backName, null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    RulesUi.this.open();
                }
            });
        }
    }
}
