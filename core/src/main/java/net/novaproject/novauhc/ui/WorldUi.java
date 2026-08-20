package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.player.utils.InventorySnapshots;
import net.novaproject.novauhc.scenario.normal.Restrictions;
import net.novaproject.novauhc.task.LoadingChunkTask;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.world.generation.UHCWorldSettings;
import net.novaproject.novauhc.world.generation.WorldGenerator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Map;

public class WorldUi extends CustomInventory {

    private static final String BIOMES_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDM4Y2YzZjhlNTRhZmMzYjNmOTFkMjBhNDlmMzI0ZGNhMTQ4NjAwN2ZlNTQ1Mzk5MDU1NTI0YzE3OTQxZjRkYyJ9fX0=";
    private static final String NETHER_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDUwMDI5MmY0YWZlNTJkMTBmMjk5ZGZiMjYwMzYzMjI4MzA0NTAzMzFlMDAzMDg0YmIyMjAzMzM1MzA2NjRlMSJ9fX0=";
    private static final String ARENA_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzAwYTFhN2JiMDdmZGI0ZTZhODZlMzQxODE2ZTg4NDNkZGFmN2NmMzcxM2EzNjY2ZDc0YjcyZjk4NjE5ZjA2MyJ9fX0=";
    private static final String END_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDY5OTRkNzFiODc1ZjA4N2U2NGRlYTliNGEwYTVjYjlmNGViOWFiMGU4ZDkwNjBkZmRlN2Y2ODAzYmFhMTc3OSJ9fX0=";
    private static final String ENTITY_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZiNGJmZGRjMTM1Mjk4ZDE5OTg1ZTkwMWNkOWI5ZWVkMTdjYjUyOTQzYTA0MjY2YTVlZmNhYzYyYzkxNmQ5ZiJ9fX0=";

    private static final Lang CONFIRM_PREGEN = DynamicLang.of("menu.mondes.pregeneration.confirm",
            "§6Relancer la prégénération de l'arène ?");
    private static final Lang CONFIRM_REGEN = DynamicLang.of("menu.mondes.arene-regeneration.confirm",
            "§cSupprimer l'arène actuelle et en générer une nouvelle ?");
    private static final Lang NO_ARENA = DynamicLang.of("menu.mondes.no-arena",
            "§c✗ §7Aucune arène n'existe pour le moment.");

    public WorldUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.mondes.title", "» Mondes✓"));
    }

    @Override
    public int getLines() {
        return 5;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    private static int countGenerationScenarios() {
        int total = 0;
        for (Scenario scenario : ScenarioManager.get().getActiveScenarios()) {
            if (scenario.touchesGeneration()) total++;
        }
        return total;
    }

    private ItemCreator tile(String key, ItemCreator icon, String name, String lore) {
        return tile(key, icon, name, lore, Map.of());
    }

    private ItemCreator tile(String key, ItemCreator icon, String name, String lore,
                             Map<String, String> placeholders) {
        String text = t(DynamicLang.of("menu.mondes." + key + ".lore", lore));
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return icon
                .setName(t(DynamicLang.of("menu.mondes." + key + ".name", name)))
                .setLores(Arrays.asList(text.split("\n", -1)));
    }

    private void submenu(int slot, String key, ItemCreator icon, String name, String lore, Runnable open) {
        addItem(new ActionItem(slot, tile(key, icon, name, lore)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                open.run();
            }
        });
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.CYAN, 0, 8, 44);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 27, 35, 37, 43);

        addItem(new StaticItem(4, summary()));

        submenu(2, "minerais", new ItemCreator(Material.DIAMOND_ORE),
                "§8┃ §bBoost des Minerais",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fAugmentez ou réduisez la quantité\n"
                        + " §b┃ §fde diamant, or, fer et lapis générée.\n"
                        + "\n"
                        + "§8» §fCliquez pour y §baccéder§f.",
                () -> new OrePopulatorUi(getPlayer()).open());

        submenu(6, "caves", new ItemCreator(Material.STONE),
                "§8┃ §6Boost des Caves",
                "§7» Accès §6Host\n"
                        + "\n"
                        + " §6┃ §fAugmentez ou réduisez la densité\n"
                        + " §6┃ §fdes réseaux de caves et des ravines.\n"
                        + "\n"
                        + "§8» §fCliquez pour y §6accéder§f.",
                () -> new CaveBoostUi(getPlayer()).open());

        submenu(16, "biome-centre", UiItems.createCustomButon(BIOMES_TEXTURE, "", null),
                "§8┃ §aBiome du Centre",
                "§7» Accès §aHost\n"
                        + "\n"
                        + " §a┃ §fChoisissez le biome appliqué au\n"
                        + " §a┃ §fcentre de l'arène.\n"
                        + "\n"
                        + "§8» §fCliquez pour y §aaccéder§f.",
                () -> new CenterUi(getPlayer(), WorldUi.this).open());

        submenu(22, "biomes-distance", new ItemCreator(Material.SAPLING),
                "§8┃ §2Biomes par Distance",
                "§7» Accès §2Host\n"
                        + "\n"
                        + " §2┃ §fChoisissez les biomes autorisés\n"
                        + " §2┃ §fprès du spawn et au loin.\n"
                        + "\n"
                        + "§8» §fCliquez pour y §2accéder§f.",
                () -> new BiomeZonesUi(getPlayer()).open());

        submenu(10, "structures", new ItemCreator(Material.WORKBENCH),
                "§8┃ §eStructures & Terrain",
                "§7» Accès §eHost\n"
                        + "\n"
                        + " §e┃ §fActivez ou désactivez les mines,\n"
                        + " §e┃ §fforteresses, villages, temples,\n"
                        + " §e┃ §fdonjons et lacs de l'arène.\n"
                        + "\n"
                        + "§8» §fCliquez pour y §eaccéder§f.",
                () -> new StructuresUi(getPlayer()).open());

        Scenario netherLess = ScenarioManager.get().getScenario(Restrictions.NetherLess.class);
        boolean netherOpen = netherLess == null || !netherLess.isActive();
        addItem(new ActionItem(28, tile("nether", UiItems.createCustomButon(NETHER_TEXTURE, "", null),
                "§8┃ §4Nether",
                "§7» Accès §4Host\n"
                        + "\n"
                        + " §4┃ §fAutorisez ou interdisez l'accès\n"
                        + " §4┃ §fau Nether pendant la partie.\n"
                        + "\n"
                        + " §4☰ §fStatut : %nether_active%\n"
                        + "\n"
                        + "§8» §fCliquez pour §4activer ou désactiver§f.",
                Map.of("%nether_active%", UiItems.onOff(getPlayer(), netherOpen)))
                .setGlow(netherOpen)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (netherLess != null) netherLess.toggleActive();
                openAll();
            }
        });

        addItem(new ActionItem(38, tile("arene-visite", UiItems.createCustomButon(ARENA_TEXTURE, "", null),
                "§8┃ §9Visiter l'Arène",
                "§7» Accès §9Host\n"
                        + "\n"
                        + " §9┃ §fTéléportez-vous dans l'arène avec\n"
                        + " §9┃ §fles outils de régénération et de\n"
                        + " §9┃ §fdéplacement du spawn.\n"
                        + "\n"
                        + "§8» §fCliquez pour §9vous y rendre§f.")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (noArena()) return;
                getPlayer().closeInventory();
                getPlayer().teleport(new Location(Common.get().getArena(), 0, 100, 0));
                InventorySnapshots.clearPlayerInventory(getPlayer());
                getPlayer().getInventory().setItem(0, Common.get().getRegenArena().getItemstack());
                getPlayer().getInventory().setItem(4, Common.get().getChangeSpawn().getItemstack());
                getPlayer().getInventory().setItem(8, new ItemCreator(Material.WOOD_DOOR)
                        .setName(t(UiLang.WORLDS_VISIT_DOOR)).getItemstack());
            }
        });

        Scenario endLess = ScenarioManager.get().getScenario(Restrictions.EndLess.class);
        boolean endOpen = endLess == null || !endLess.isActive();
        addItem(new ActionItem(34, tile("end", UiItems.createCustomButon(END_TEXTURE, "", null),
                "§8┃ §5End",
                "§7» Accès §5Host\n"
                        + "\n"
                        + " §5┃ §fAutorisez ou interdisez l'accès\n"
                        + " §5┃ §fà l'End pendant la partie.\n"
                        + "\n"
                        + " §5☰ §fStatut : %end_active%\n"
                        + "\n"
                        + "§8» §fCliquez pour §5activer ou désactiver§f.",
                Map.of("%end_active%", UiItems.onOff(getPlayer(), endOpen)))
                .setGlow(endOpen)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (endLess != null) endLess.toggleActive();
                openAll();
            }
        });

        addItem(new ActionItem(36, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.CYAN),
                t(DynamicLang.of("menu.mondes.retour.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WorldEntityUi(getPlayer()).open();
            }
        });

        addItem(new ActionItem(40, tile("pregeneration", new ItemCreator(Material.COMMAND),
                "§8┃ §6Relancer la Prégénération",
                "§7» Accès §6Host\n"
                        + "\n"
                        + " §6┃ §fRecharge tous les chunks de l'arène\n"
                        + " §6┃ §fet du Nether jusqu'à la bordure.\n"
                        + " §6┃ §fLe serveur lague pendant l'opération.\n"
                        + "\n"
                        + "§8» §fCliquez pour §6lancer§f.")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (noArena()) return;
                new ConfirmMenu(getPlayer(), t(CONFIRM_PREGEN),
                        () -> {
                            getPlayer().closeInventory();
                            LoadingChunkTask.create(Common.get().getArena(),
                                    Bukkit.getWorld(Common.get().getArenaName() + "_nether"),
                                    (int) Common.get().getArena().getWorldBorder().getSize() / 2);
                        },
                        WorldUi.this::openAll,
                        WorldUi.this).open();
            }
        });

        addItem(new ActionItem(42, tile("arene-regeneration", new ItemCreator(Material.EXPLOSIVE_MINECART),
                "§8┃ §c§lRégénérer l'Arène",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §a┃ §f%generation% §7scénario(s) de génération actif(s)\n"
                        + " §a┃ §7Sans régénération, ils §cn'auront aucun effet§7.\n"
                        + "\n"
                        + " §c┃ §4§lSupprime §fl'arène, le nether et l'end,\n"
                        + " §c┃ §fpuis relance la prégénération. Tout ce qui\n"
                        + " §c┃ §fa été construit est §4perdu§f.\n"
                        + "\n"
                        + "§8» §fCliquez pour §crégénérer§f.",
                Map.of("%generation%", String.valueOf(countGenerationScenarios())))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfirmMenu(getPlayer(), t(CONFIRM_REGEN),
                        () -> {
                            getPlayer().closeInventory();
                            new WorldGenerator(Main.get(), Common.get().getArenaName());
                        },
                        WorldUi.this::openAll,
                        WorldUi.this).open();
            }
        });
    }

    private ItemCreator summary() {
        World arena = Common.get().getArena();
        Scenario netherLess = ScenarioManager.get().getScenario(Restrictions.NetherLess.class);
        Scenario endLess = ScenarioManager.get().getScenario(Restrictions.EndLess.class);
        String none = t(DynamicLang.of("menu.mondes.resume.aucune", "§7aucune"));

        return tile("resume", new ItemCreator(Material.GRASS),
                "§8┃ §3État du Monde",
                "§7» Élément §3dynamique\n"
                        + "\n"
                        + " §3┃ §fArène : §3%arena%\n"
                        + " §3┃ §fBordure : §3%border%\n"
                        + "\n"
                        + " §b☰ §fMinerais : §b%ore%\n"
                        + " §6☰ §fCaves : §6×%cave%\n"
                        + "\n"
                        + " §4☰ §fNether : %nether%\n"
                        + " §5☰ §fEnd : %end%",
                Map.of("%arena%", arena == null ? none : arena.getName(),
                        "%border%", arena == null ? none
                                : String.valueOf((int) arena.getWorldBorder().getSize()),
                        "%ore%", "×" + String.format("%.2f", UHCWorldSettings.diamondMultiplier()),
                        "%cave%", String.valueOf(UHCManager.get().getCaveMultiplier()),
                        "%nether%", UiItems.onOff(getPlayer(), netherLess == null || !netherLess.isActive()),
                        "%end%", UiItems.onOff(getPlayer(), endLess == null || !endLess.isActive())));
    }

    private boolean noArena() {
        if (Common.get().getArena() != null) return false;
        LangManager.get().send(NO_ARENA, getPlayer());
        getPlayer().closeInventory();
        return true;
    }

    public static class WorldEntityUi extends CustomInventory {

        public WorldEntityUi(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.monde-et-entite.title", "» Monde & Entité✓"));
        }

        @Override
        public int getLines() {
            return 1;
        }

        @Override
        public boolean isRefreshAuto() {
            return false;
        }

        @Override
        public void setup() {
            UiItems.panes(this, DyeColor.WHITE, 1, 7);

            addItem(new ActionItem(0, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.CYAN),
                    t(DynamicLang.of("menu.monde-et-entite.btn-0.name", "§8┃ §7§lRetour")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new DefaultUi(getPlayer()).open();
                }
            });

            addItem(new ActionItem(3, UiItems.createCustomButon(MenuHeads.WORLD,
                    t(DynamicLang.of("menu.monde-et-entite.btn-3.name", "§8┃ §3Monde")),
                    Arrays.asList(t(DynamicLang.of("menu.monde-et-entite.btn-3.lore",
                            "§7» Accès §3Host\n\n §3┃ §fPermet de §3configurer §fles paramètres\n §3┃ §fde la §3map§f.\n\n§8» §fClique droit pour §3modifier.")).split("\n", -1)))) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new WorldUi(getPlayer()).open();
                }
            });

            UiItems.panes(this, DyeColor.CYAN, 8);

            addItem(new ActionItem(5, UiItems.createCustomButon(ENTITY_TEXTURE,
                    t(DynamicLang.of("menu.monde-et-entite.btn-5.name", "§8┃ §cEntité")),
                    Arrays.asList(t(DynamicLang.of("menu.monde-et-entite.btn-5.lore",
                            "§7» Accès §cHost\n\n §c┃ §fPermet de §cconfigurer §fles paramètres\n §c┃ §fdes §centités§f.\n\n§8» §fClique droit pour §cmodifier.")).split("\n", -1)))) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new EntityUi(getPlayer()).open();
                }
            });
        }
    }
}
