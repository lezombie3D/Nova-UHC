package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.mumble.MumbleManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.ScheduledScenarios;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.ui.MenuHeads.Shape;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.chat.TextUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static net.novaproject.novauhc.ui.UiItems.onOff;

public final class HostOptionsMenus {

    private HostOptionsMenus() {
    }

    private static ItemCreator tile(CustomInventory ui, String menu, String key, ItemCreator icon,
                                    String name, String lore) {
        return tile(ui, menu, key, icon, name, lore, null, null);
    }

    private static ItemCreator tile(CustomInventory ui, String menu, String key, ItemCreator icon,
                                    String name, String lore, String placeholder, String value) {
        String rendered = ui.t(DynamicLang.of("menu." + menu + "." + key + ".lore", lore));
        if (placeholder != null) rendered = rendered.replace(placeholder, value);
        return icon.setName(ui.t(DynamicLang.of("menu." + menu + "." + key + ".name", name)))
                .setLores(Arrays.asList(rendered.split("\n", -1)));
    }

    private static ItemCreator backIcon(CustomInventory ui, String menu, String texture) {
        return UiItems.createCustomButon(texture,
                ui.t(DynamicLang.of("menu." + menu + ".back.name", "§8┃ §7§lRetour")), null);
    }

    private static ActionItem submenu(int slot, ItemCreator icon, Supplier<CustomInventory> target) {
        return new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                target.get().open();
            }
        };
    }

    private static ActionItem toggle(CustomInventory ui, int slot, ItemCreator icon, Runnable flip) {
        return new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                flip.run();
                ui.openAll();
            }
        };
    }

    private static ActionItem number(CustomInventory ui, int slot, ItemCreator icon,
                                     Number current, Number big, Number mid, Number small,
                                     double min, double max, VariableType type, Consumer<Number> apply) {
        return new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(ui.getPlayer(), big, mid, small, big, mid, small,
                        current, min, max, ui, type) {
                    @Override
                    public void onChange(Number newValue) {
                        apply.accept(newValue);
                    }
                }.open();
            }
        };
    }

    private static ItemCreator head(short data) {
        return new ItemCreator(Material.SKULL_ITEM).setDurability(data);
    }

    public static class AdvancedOptions extends CustomInventory {

        private static final String ID = "options-avancees";

        public AdvancedOptions(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Options Avancées"));
        }

        @Override
        public int getLines() {
            return 2;
        }

        @Override
        public boolean isRefreshAuto() {
            return false;
        }

        @Override
        public void setup() {
            Player player = getPlayer();

            UiItems.panes(this, DyeColor.WHITE, 1, 7, 10, 16);
            UiItems.panes(this, DyeColor.RED, 0, 8, 17);

            addItem(submenu(9, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.RED)), () -> new GameUi(player)));

            addItem(submenu(2, tile(this, ID, "start-and-pace", new ItemCreator(Material.WATCH),
                    "§8┃ §6Démarrage & Rythme",
                    "§7» Accès §6Host\n"
                            + "\n"
                            + " §6┃ §fGérez la §6préparation§f, les plateformes\n"
                            + " §6┃ §fet le déclenchement de la §6partie§f.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §6accéder§f."),
                    () -> new StartAndPace(player)));

            addItem(submenu(4, tile(this, ID, "combat-and-safety", new ItemCreator(Material.IRON_SWORD),
                    "§8┃ §cCombat & Sécurité",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fConfigurez le combat-log, les protections\n"
                            + " §c┃ §fet les règles actives avant le PvP.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §caccéder§f."),
                    () -> new CombatAndSafety(player)));

            addItem(submenu(6, tile(this, ID, "communication-and-interface",
                    new ItemCreator(Material.BOOK_AND_QUILL),
                    "§8┃ §bCommunication & Interface",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fGérez le chat, les annonces et les\n"
                            + " §b┃ §finformations visibles par le staff.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §baccéder§f."),
                    () -> new CommunicationInterface(player)));

            addItem(submenu(12, tile(this, ID, "uhc-roles", new ItemCreator(Material.NETHER_STAR),
                    "§8┃ §dRéglages UHC à rôles",
                    "§7» Accès §dHost\n"
                            + "\n"
                            + " §d┃ §fÉpisodes, items de pouvoir, résurrection\n"
                            + " §d┃ §fet affichage des effets de rôle.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §daccéder§f."),
                    () -> new UhcRoles(player)));

            addItem(submenu(13, tile(this, ID, "auto-revive", new ItemCreator(Material.GHAST_TEAR),
                    "§8┃ §cRevive automatique",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fRessuscitez automatiquement les morts\n"
                            + " §c┃ §ftant qu'une phase de départ dure.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §caccéder§f."),
                    () -> new AutoReviveUi(player)));

            addItem(submenu(14, tile(this, ID, "day-night-cycle", new ItemCreator(Material.DOUBLE_PLANT),
                    "§8┃ §eCycle Jour / Nuit",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fRythme du jour et de la nuit, durées\n"
                            + " §e┃ §fpersonnalisées et annonces de transition.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §eaccéder§f."),
                    () -> new DayNightCycle(player)));
        }
    }

    public static class StartAndPace extends CustomInventory {

        private static final String ID = "demarrage-rythme";

        public StartAndPace(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Démarrage & Rythme"));
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
            Player player = getPlayer();
            UHCManager uhc = UHCManager.get();

            UiItems.panes(this, DyeColor.ORANGE, 0, 8, 26);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

            addItem(number(this, 14, tile(this, ID, "invulnerability", new ItemCreator(Material.GOLDEN_APPLE),
                    "§8┃ §6Invulnérabilité au départ",
                    "§7» Accès §6Host\n"
                            + "\n"
                            + " §6┃ §fDéfinissez la durée de protection\n"
                            + " §6┃ §faccordée après le lancement.\n"
                            + "\n"
                            + " §6☰ §fDurée : %invulnerability_duration%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §6modifier§f.",
                    "%invulnerability_duration%", String.valueOf(uhc.getInvulnerabilityDuration())),
                    uhc.getInvulnerabilityDuration(), 30, 10, 5, 0, 300, VariableType.INTEGER,
                    v -> uhc.setInvulnerabilityDuration(v.intValue())));

            addItem(toggle(this, 22, tile(this, ID, "starting-platforms",
                    new ItemCreator(Material.BEDROCK).setGlow(uhc.isStartPlatformsEnabled()),
                    "§8┃ §2Plateformes de départ",
                    "§7» Accès §2Host\n"
                            + "\n"
                            + " §2┃ §fActivez les plateformes sous les joueurs\n"
                            + " §2┃ §fpendant la préparation de la partie.\n"
                            + "\n"
                            + " §2☰ §fStatut : %starting_platforms_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §2activer ou désactiver§f.",
                    "%starting_platforms_active%", onOff(player, uhc.isStartPlatformsEnabled())),
                    () -> uhc.setStartPlatformsEnabled(!uhc.isStartPlatformsEnabled())));

            addItem(toggle(this, 4, tile(this, ID, "auto-start",
                    new ItemCreator(Material.REDSTONE_TORCH_ON).setGlow(uhc.isAutoStartEnabled()),
                    "§8┃ §aAuto-start",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fLance automatiquement la partie lorsque\n"
                            + " §a┃ §fle nombre requis de joueurs est atteint.\n"
                            + "\n"
                            + " §a☰ §fStatut : %auto_start_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §aactiver ou désactiver§f.",
                    "%auto_start_active%", onOff(player, uhc.isAutoStartEnabled())),
                    () -> uhc.setAutoStartEnabled(!uhc.isAutoStartEnabled())));

            addItem(number(this, 12, tile(this, ID, "auto-start-players", head((short) 3),
                    "§8┃ §eJoueurs requis",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fDéfinissez le nombre de joueurs\n"
                            + " §e┃ §fnécessaire au déclenchement automatique.\n"
                            + "\n"
                            + " §e☰ §fMinimum : %auto_start_min_players%\n"
                            + "\n"
                            + "§8» §fCliquez pour §emodifier§f.",
                    "%auto_start_min_players%", String.valueOf(uhc.getAutoStartMinPlayers())),
                    uhc.getAutoStartMinPlayers(), 10, 5, 1, 2, 100, VariableType.INTEGER,
                    v -> uhc.setAutoStartMinPlayers(v.intValue())));

            addItem(number(this, 13, tile(this, ID, "start-countdown", new ItemCreator(Material.WATCH),
                    "§8┃ §3Compte à rebours",
                    "§7» Accès §3Host\n"
                            + "\n"
                            + " §3┃ §fRéglez le décompte affiché avant\n"
                            + " §3┃ §fle lancement réel de la partie.\n"
                            + "\n"
                            + " §3☰ §fDurée : %start_countdown_seconds%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §3modifier§f.",
                    "%start_countdown_seconds%", String.valueOf(uhc.getStartCountdownSeconds())),
                    uhc.getStartCountdownSeconds(), 10, 5, 1, 3, 120, VariableType.INTEGER,
                    v -> uhc.setStartCountdownSeconds(v.intValue())));

            addItem(submenu(18, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.ORANGE)), () -> new AdvancedOptions(player)));
        }
    }

    public static class CombatAndSafety extends CustomInventory {

        private static final String ID = "combat-securite";

        public CombatAndSafety(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Combat & Sécurité"));
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
            Player player = getPlayer();
            UHCManager uhc = UHCManager.get();

            UiItems.panes(this, DyeColor.RED, 0, 8, 35);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26, 28, 34);

            addItem(number(this, 12, tile(this, ID, "combat-tag", new ItemCreator(Material.IRON_SWORD),
                    "§8┃ §cCombat tag",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fDéfinissez la durée pendant laquelle\n"
                            + " §c┃ §fun joueur reste considéré en combat.\n"
                            + "\n"
                            + " §c☰ §fDurée : %combat_tag_seconds%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §cmodifier§f.",
                    "%combat_tag_seconds%", String.valueOf(uhc.getCombatTagSeconds())),
                    uhc.getCombatTagSeconds(), 10, 5, 1, 0, 120, VariableType.INTEGER,
                    v -> uhc.setCombatTagSeconds(v.intValue())));

            addItem(toggle(this, 20, tile(this, ID, "combat-log-npc",
                    head((short) 2).setGlow(uhc.isCombatLogEnabled()),
                    "§8┃ §2NPC de combat-log",
                    "§7» Accès §2Host\n"
                            + "\n"
                            + " §2┃ §fFaites apparaître un NPC lorsqu’un joueur\n"
                            + " §2┃ §fse déconnecte pendant un combat.\n"
                            + "\n"
                            + " §2☰ §fStatut : %combat_log_npc_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §2activer ou désactiver§f.",
                    "%combat_log_npc_active%", onOff(player, uhc.isCombatLogEnabled())),
                    () -> uhc.setCombatLogEnabled(!uhc.isCombatLogEnabled())));

            addItem(toggle(this, 21, tile(this, ID, "spectator-killer-teleport",
                    new ItemCreator(Material.EYE_OF_ENDER).setGlow(uhc.isSpectatorAutoTpKiller()),
                    "§8┃ §dSpectateur → Tueur",
                    "§7» Accès §dHost\n"
                            + "\n"
                            + " §d┃ §fTéléportez automatiquement le joueur mort\n"
                            + " §d┃ §fen mode spectateur sur son tueur.\n"
                            + "\n"
                            + " §d☰ §fStatut : %spectator_killer_tp_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §dactiver ou désactiver§f.",
                    "%spectator_killer_tp_active%", onOff(player, uhc.isSpectatorAutoTpKiller())),
                    () -> uhc.setSpectatorAutoTpKiller(!uhc.isSpectatorAutoTpKiller())));

            addItem(number(this, 13, tile(this, ID, "assist-window", new ItemCreator(Material.BOW),
                    "§8┃ §6Fenêtre d’assistance",
                    "§7» Accès §6Host\n"
                            + "\n"
                            + " §6┃ §fDéfinissez le délai pendant lequel\n"
                            + " §6┃ §fun dégât est comptabilisé comme assistance.\n"
                            + "\n"
                            + " §6☰ §fDurée : %assist_window_seconds%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §6modifier§f.",
                    "%assist_window_seconds%", String.valueOf(uhc.getStatsAssistWindow())),
                    uhc.getStatsAssistWindow(), 10, 5, 1, 0, 120, VariableType.INTEGER,
                    v -> uhc.setStatsAssistWindow(v.intValue())));

            addItem(number(this, 14, tile(this, ID, "pearl-damage", new ItemCreator(Material.ENDER_PEARL),
                    "§8┃ §bDégâts de perle",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fDéfinissez les dégâts reçus après\n"
                            + " §b┃ §fl’utilisation d’une Ender Pearl.\n"
                            + "\n"
                            + " §b☰ §fDégâts : %pearl_damage%\n"
                            + "\n"
                            + "§8» §fCliquez pour §bmodifier§f.",
                    "%pearl_damage%", String.valueOf(uhc.getPearlDamage())),
                    uhc.getPearlDamage(), 1.0, 0.5, 0.1, -1, 20, VariableType.DOUBLE,
                    v -> uhc.setPearlDamage(v.doubleValue())));

            addItem(toggle(this, 23, tile(this, ID, "lava-before-pvp",
                    new ItemCreator(Material.LAVA_BUCKET).setGlow(uhc.isBlockLavaBeforePvp()),
                    "§8┃ §eLave avant le PvP",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fBloquez la pose de lave avant\n"
                            + " §e┃ §fl’activation officielle du PvP.\n"
                            + "\n"
                            + " §e☰ §fStatut : %lava_before_pvp_blocked%\n"
                            + "\n"
                            + "§8» §fCliquez pour §eactiver ou désactiver§f.",
                    "%lava_before_pvp_blocked%", onOff(player, uhc.isBlockLavaBeforePvp())),
                    () -> uhc.setBlockLavaBeforePvp(!uhc.isBlockLavaBeforePvp())));

            addItem(toggle(this, 24, tile(this, ID, "anti-obsi-trap",
                    new ItemCreator(Material.OBSIDIAN).setGlow(uhc.isAntiObsiTrap()),
                    "§8┃ §5Anti obsi-trap",
                    "§7» Accès §5Host\n"
                            + "\n"
                            + " §5┃ §fAnnulez les dégâts d’étouffement\n"
                            + " §5┃ §fsubis à l’intérieur de l’obsidienne.\n"
                            + "\n"
                            + " §5☰ §fStatut : %anti_obsi_trap_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §5activer ou désactiver§f.",
                    "%anti_obsi_trap_active%", onOff(player, uhc.isAntiObsiTrap())),
                    () -> uhc.setAntiObsiTrap(!uhc.isAntiObsiTrap())));

            addItem(submenu(27, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.RED)), () -> new AdvancedOptions(player)));
        }
    }

    public static class CommunicationInterface extends CustomInventory {

        private static final String ID = "communication-interface";

        public CommunicationInterface(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Communication & Interface"));
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
            Player player = getPlayer();
            UHCManager uhc = UHCManager.get();

            UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 35);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26, 28, 34);

            addItem(toggle(this, 14, tile(this, ID, "diamond-alert",
                    new ItemCreator(Material.DIAMOND_ORE).setGlow(uhc.isAlertFoundDiamond()),
                    "§8┃ §bAlerte de diamants",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fAvertissez le staff lorsqu’un joueur\n"
                            + " §b┃ §ftrouve et mine des diamants.\n"
                            + "\n"
                            + " §b☰ §fStatut : %diamond_alert_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §bactiver ou désactiver§f.",
                    "%diamond_alert_active%", onOff(player, uhc.isAlertFoundDiamond())),
                    () -> uhc.setAlertFoundDiamond(!uhc.isAlertFoundDiamond())));

            addItem(number(this, 12, tile(this, ID, "vote-duration", new ItemCreator(Material.PAPER),
                    "§8┃ §eDurée des votes",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fDéfinissez le temps accordé aux\n"
                            + " §e┃ §fjoueurs pour répondre à un vote.\n"
                            + "\n"
                            + " §e☰ §fDurée : %vote_duration_seconds%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §emodifier§f.",
                    "%vote_duration_seconds%", String.valueOf(uhc.getVoteDuration())),
                    uhc.getVoteDuration(), 30, 10, 5, 5, 600, VariableType.INTEGER,
                    v -> uhc.setVoteDuration(v.intValue())));

            addItem(toggle(this, 10, tile(this, ID, "disabled-chat",
                    UiItems.stateDye(!uhc.isChatDisabled()),
                    "§8┃ §cChat des joueurs",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fCoupez le chat des joueurs tout en\n"
                            + " §c┃ §flaissant le staff communiquer.\n"
                            + "\n"
                            + " §c☰ §fDésactivé : %chat_disabled%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cbasculer le statut§f.",
                    "%chat_disabled%", uhc.isChatDisabled() ? "§cOui" : "§aNon"),
                    () -> uhc.setChatDisabled(!uhc.isChatDisabled())));

            addItem(number(this, 11, tile(this, ID, "lobby-anti-spam", new ItemCreator(Material.WATCH),
                    "§8┃ §6Anti-spam du lobby",
                    "§7» Accès §6Host\n"
                            + "\n"
                            + " §6┃ §fDéfinissez le délai minimum entre\n"
                            + " §6┃ §fdeux messages envoyés dans le lobby.\n"
                            + "\n"
                            + " §6☰ §fDélai : %lobby_chat_cooldown_ms% ms\n"
                            + "\n"
                            + "§8» §fCliquez pour §6modifier§f.",
                    "%lobby_chat_cooldown_ms%", String.valueOf(uhc.getLobbyChatCooldownMs())),
                    uhc.getLobbyChatCooldownMs(), 1000, 500, 100, 0, 10000, VariableType.INTEGER,
                    v -> uhc.setLobbyChatCooldownMs(v.intValue())));

            addItem(toggle(this, 15, tile(this, ID, "tab-information",
                    new ItemCreator(Material.ITEM_FRAME).setGlow(uhc.isShowTabInfo()),
                    "§8┃ §aInformations du TAB",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fAffichez les informations du serveur\n"
                            + " §a┃ §fdans la liste des joueurs.\n"
                            + "\n"
                            + " §a☰ §fStatut : %tab_information_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §aactiver ou désactiver§f.",
                    "%tab_information_active%", onOff(player, uhc.isShowTabInfo())),
                    () -> uhc.setShowTabInfo(!uhc.isShowTabInfo())));

            addItem(toggle(this, 13, tile(this, ID, "show-health",
                    new ItemCreator(Material.SPECKLED_MELON).setGlow(uhc.isShowHealthPercent()),
                    "§8┃ §cVie dans le TAB",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fAffichez les points de vie des joueurs\n"
                            + " §c┃ §fà côté de leur pseudo.\n"
                            + "\n"
                            + " §c☰ §fStatut : %show_health_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cactiver ou désactiver§f.",
                    "%show_health_active%", onOff(player, uhc.isShowHealthPercent())),
                    () -> uhc.setShowHealthPercent(!uhc.isShowHealthPercent())));

            addItem(toggle(this, 21, tile(this, ID, "show-kill-scoreboard",
                    new ItemCreator(Material.DIAMOND_SWORD).setGlow(uhc.isShowKillScoreboard()),
                    "§8┃ §6Kills dans le scoreboard",
                    "§7» Accès §6Host\n"
                            + "\n"
                            + " §6┃ §fAffichez le nombre de kills du joueur\n"
                            + " §6┃ §fdans le scoreboard de partie.\n"
                            + "\n"
                            + " §6☰ §fStatut : %show_kill_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §6activer ou désactiver§f.",
                    "%show_kill_active%", onOff(player, uhc.isShowKillScoreboard())),
                    () -> uhc.setShowKillScoreboard(!uhc.isShowKillScoreboard())));

            addItem(toggle(this, 11, tile(this, ID, "mumble-scramble-names",
                    new ItemCreator(Material.NAME_TAG).setGlow(uhc.isMumbleScrambleNames()),
                    "§8┃ §5Pseudos vocaux anonymes",
                    "§7» Accès §5Host\n"
                            + "\n"
                            + " §5┃ §fDans Mumble, les joueurs apparaissent\n"
                            + " §5┃ §fsous un jeton au lieu de leur pseudo.\n"
                            + " §5┃ §fChacun reçoit le sien en cliquant le lien.\n"
                            + "\n"
                            + " §5☰ §fStatut : %mumble_scramble_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §5activer ou désactiver§f.",
                    "%mumble_scramble_active%", onOff(player, uhc.isMumbleScrambleNames())),
                    () -> {
                        uhc.setMumbleScrambleNames(!uhc.isMumbleScrambleNames());
                        // Le pseudo affiché dans Mumble est figé à la connexion : il faut
                        // repousser l'option au serveur vocal ET redistribuer les liens,
                        // sinon la bascule reste sans effet pour les déjà-connectés.
                        MumbleManager.get().applyOptions();
                    }));

            addItem(toggle(this, 12, tile(this, ID, "mumble-team-tags",
                    new ItemCreator(Material.BANNER).setGlow(uhc.isMumbleShowTeamTags()),
                    "§8┃ §aTags d'équipe dans le vocal",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fAffichez le tag d'équipe devant le pseudo\n"
                            + " §a┃ §fdans le menu de modération vocal.\n"
                            + " §a┃ §f§7(menu du plugin, pas dans Mumble)\n"
                            + "\n"
                            + " §a☰ §fStatut : %mumble_tags_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §aactiver ou désactiver§f.",
                    "%mumble_tags_active%", onOff(player, uhc.isMumbleShowTeamTags())),
                    () -> uhc.setMumbleShowTeamTags(!uhc.isMumbleShowTeamTags())));

            addItem(toggle(this, 16, tile(this, ID, "spectator-notifications",
                    new ItemCreator(Material.SIGN).setGlow(uhc.isSpectatorNotifications()),
                    "§8┃ §dNotifications spectateurs",
                    "§7» Accès §dHost\n"
                            + "\n"
                            + " §d┃ §fInformez les spectateurs des pouvoirs,\n"
                            + " §d┃ §fcombats, commandes et grands groupes.\n"
                            + "\n"
                            + " §d☰ §fStatut : %spectator_notifications_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §dactiver ou désactiver§f.",
                    "%spectator_notifications_active%", onOff(player, uhc.isSpectatorNotifications())),
                    () -> uhc.setSpectatorNotifications(!uhc.isSpectatorNotifications())));

            addItem(submenu(22, tile(this, ID, "live-messages", new ItemCreator(Material.BOOK_AND_QUILL),
                    "§8┃ §5Messages en direct",
                    "§7» Accès §5Host\n"
                            + "\n"
                            + " §5┃ §fPréparez et diffusez des annonces\n"
                            + " §5┃ §fen jeu sans utiliser de commande.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §5accéder§f."),
                    () -> new LiveMessages(player)));

            addItem(submenu(27, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.LIGHT_BLUE)), () -> new AdvancedOptions(player)));
        }
    }

    public static class DayNightCycle extends CustomInventory {

        private static final String ID = "cycle-jour-nuit";

        public DayNightCycle(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Cycle Jour / Nuit"));
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
            Player player = getPlayer();
            UHCManager uhc = UHCManager.get();

            UiItems.panes(this, DyeColor.YELLOW, 0, 8, 26);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

            addItem(toggle(this, 11, tile(this, ID, "custom-cycle",
                    new ItemCreator(Material.WATCH).setGlow(uhc.isCustomDayCycle()),
                    "§8┃ §aCycle personnalisé",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fRemplacez le cycle vanilla par les durées\n"
                            + " §a┃ §fconfigurées dans ce menu.\n"
                            + "\n"
                            + " §a☰ §fStatut : %custom_day_cycle_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §aactiver ou désactiver§f.",
                    "%custom_day_cycle_active%", onOff(player, uhc.isCustomDayCycle())),
                    () -> uhc.setCustomDayCycle(!uhc.isCustomDayCycle())));

            addItem(toggle(this, 12, tile(this, ID, "announce",
                    new ItemCreator(Material.NOTE_BLOCK).setGlow(uhc.isAnnounceDayNight()),
                    "§8┃ §cAnnonce jour/nuit",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fAnnoncez chaque transition du cycle\n"
                            + " §c┃ §fdans le chat des joueurs.\n"
                            + "\n"
                            + " §c☰ §fStatut : %announce_day_night_active%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cactiver ou désactiver§f.",
                    "%announce_day_night_active%", onOff(player, uhc.isAnnounceDayNight())),
                    () -> uhc.setAnnounceDayNight(!uhc.isAnnounceDayNight())));

            addItem(number(this, 14, tile(this, ID, "day-duration", new ItemCreator(Material.GLOWSTONE_DUST),
                    "§8┃ §eDurée du jour",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fRéglez la durée de la phase de jour\n"
                            + " §e┃ §fdu cycle personnalisé.\n"
                            + "\n"
                            + " §e☰ §fDurée : %day_duration%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §emodifier§f.",
                    "%day_duration%", String.valueOf(uhc.getDayDuration())),
                    uhc.getDayDuration(), 120, 60, 10, 10, 3600, VariableType.INTEGER,
                    v -> uhc.setDayDuration(v.intValue())));

            addItem(number(this, 15, tile(this, ID, "night-duration", new ItemCreator(Material.COAL),
                    "§8┃ §bDurée de la nuit",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fRéglez la durée de la phase de nuit\n"
                            + " §b┃ §fdu cycle personnalisé.\n"
                            + "\n"
                            + " §b☰ §fDurée : %night_duration%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §bmodifier§f.",
                    "%night_duration%", String.valueOf(uhc.getNightDuration())),
                    uhc.getNightDuration(), 120, 60, 10, 10, 3600, VariableType.INTEGER,
                    v -> uhc.setNightDuration(v.intValue())));

            addItem(submenu(18, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.YELLOW)), () -> new AdvancedOptions(player)));
        }
    }

    public static class UhcRoles extends CustomInventory {

        private static final String ID = "reglages-uhc-roles";

        public UhcRoles(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Réglages UHC à rôles"));
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
            Player player = getPlayer();
            UHCManager uhc = UHCManager.get();
            ScenarioRole<?> roleMode = KPIBuilder.activeScenarioRole();

            UiItems.panes(this, DyeColor.MAGENTA, 0, 8, 26);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

            if (roleMode != null) {
                addItem(number(this, 2, tile(this, ID, "roles-timer", new ItemCreator(Material.WATCH),
                        "§8┃ §6Distribution des rôles",
                        "§7» Accès §6Host\n"
                                + "\n"
                                + " §6┃ §fMoment où les rôles sont distribués\n"
                                + " §6┃ §faux joueurs. 0 = aligné sur le PvP.\n"
                                + "\n"
                                + " §6☰ §fTimer : %roles_timer%\n"
                                + "\n"
                                + "§8» §fCliquez pour §6modifier§f.",
                        "%roles_timer%", roleMode.getRolesTimer() == 0
                                ? t(UiLang.ROLES_TIMER_ALIGNED_PVP)
                                : TextUtils.getFormattedTime(roleMode.getRolesTimer())),
                        roleMode.getRolesTimer(), 600, 60, 30, 0, 14400, VariableType.TIME,
                        v -> roleMode.setRolesTimer(v.intValue())));
            }

            addItem(number(this, 4, tile(this, ID, "episode", new ItemCreator(Material.BOOK),
                    "§8┃ §aDurée des épisodes",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fDéfinissez la durée d’un épisode\n"
                            + " §a┃ §fpour les modes qui utilisent ce rythme.\n"
                            + "\n"
                            + " §a☰ §fDurée : %episode_duration%s\n"
                            + "\n"
                            + "§8» §fCliquez pour §amodifier§f.",
                    "%episode_duration%", String.valueOf(uhc.getEpisodeDuration())),
                    uhc.getEpisodeDuration(), 300, 60, 10, 0, 7200, VariableType.INTEGER,
                    v -> uhc.setEpisodeDuration(v.intValue())));

            addItem(toggle(this, 11, tile(this, ID, "ability-drop",
                    new ItemCreator(Material.NETHER_STAR).setGlow(uhc.isAbilityItemsBlockDrop()),
                    "§8┃ §cDrop des items de pouvoir",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fAutorisez ou bloquez le jet\n"
                            + " §c┃ §fdes items liés aux pouvoirs.\n"
                            + "\n"
                            + " §c☰ §fStatut : %ability_item_drop_status%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cactiver ou désactiver§f.",
                    "%ability_item_drop_status%", onOff(player, uhc.isAbilityItemsBlockDrop())),
                    () -> uhc.setAbilityItemsBlockDrop(!uhc.isAbilityItemsBlockDrop())));

            addItem(toggle(this, 12, tile(this, ID, "ability-visibility",
                    UiItems.statePane(uhc.isAbilityItemsHideFromOthers()),
                    "§8┃ §eVisibilité des items",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fCachez aux autres joueurs les items\n"
                            + " §e┃ §fportant un pouvoir spécial.\n"
                            + "\n"
                            + " §e☰ §fStatut : %ability_item_visibility%\n"
                            + "\n"
                            + "§8» §fCliquez pour §eactiver ou désactiver§f.",
                    "%ability_item_visibility%", onOff(player, uhc.isAbilityItemsHideFromOthers())),
                    () -> uhc.setAbilityItemsHideFromOthers(!uhc.isAbilityItemsHideFromOthers())));

            int resurrection = (int) Math.round(uhc.getResurrectionHealthPercent() * 100);
            addItem(number(this, 14, tile(this, ID, "resurrection-health", new ItemCreator(Material.GHAST_TEAR),
                    "§8┃ §bVie à la résurrection",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fDéfinissez la vie rendue lorsqu’une\n"
                            + " §b┃ §fmort différée est annulée.\n"
                            + "\n"
                            + " §b☰ §fVie : %resurrection_health_percent%%\n"
                            + "\n"
                            + "§8» §fCliquez pour §bmodifier§f.",
                    "%resurrection_health_percent%", String.valueOf(resurrection)),
                    resurrection, 25, 10, 5, 0, 100, VariableType.INTEGER,
                    v -> uhc.setResurrectionHealthPercent(v.intValue() / 100.0)));

            addItem(toggle(this, 15, tile(this, ID, "mirrored-effects",
                    new ItemCreator(Material.POTION)
                            .setDurability((short) 8233)
                            .addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                            .setGlow(uhc.isMirroredEffects()),
                    "§8┃ §dEffets affichés",
                    "§7» Accès §dHost\n"
                            + "\n"
                            + " §d┃ §fAlignez l’affichage des niveaux d’effets\n"
                            + " §d┃ §fsur les pourcentages accordés par le rôle.\n"
                            + "\n"
                            + " §d☰ §fStatut : %mirrored_effects_status%\n"
                            + "\n"
                            + "§8» §fCliquez pour §dactiver ou désactiver§f.",
                    "%mirrored_effects_status%", onOff(player, uhc.isMirroredEffects())),
                    () -> uhc.setMirroredEffects(!uhc.isMirroredEffects())));

            addItem(submenu(18, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.MAGENTA)), () -> new AdvancedOptions(player)));
        }
    }

    public static class Timers extends CustomInventory {

        private static final String ID = "timers-partie";

        public Timers(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Gestion des Timers"));
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
            Player player = getPlayer();
            UHCManager uhc = UHCManager.get();
            ScheduledScenarios.FinalHeal finalHeal =
                    ScenarioManager.get().getScenario(ScheduledScenarios.FinalHeal.class);

            UiItems.panes(this, DyeColor.LIME, 0, 8, 35);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26, 28, 34);

            addItem(number(this, 2, tile(this, ID, "pvp", new ItemCreator(Material.DIAMOND_SWORD),
                    "§8┃ §aActivation du PvP",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fDéfinissez le moment où les joueurs\n"
                            + " §a┃ §fpeuvent commencer à se combattre.\n"
                            + "\n"
                            + " §a☰ §fTimer : %pvp_timer%\n"
                            + "\n"
                            + "§8» §fCliquez pour §amodifier§f.",
                    "%pvp_timer%", TextUtils.getFormattedTime(uhc.getPvpTimer())),
                    uhc.getPvpTimer(), 600, 60, 30, 0, 7200, VariableType.TIME,
                    v -> uhc.setPvpTimer(v.intValue())));

            addItem(number(this, 4, tile(this, ID, "border", new ItemCreator(Material.STAINED_GLASS),
                    "§8┃ §cActivation de la bordure",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fDéfinissez le moment où la bordure\n"
                            + " §c┃ §fcommence à se réduire.\n"
                            + "\n"
                            + " §c☰ §fTimer : %border_timer%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cmodifier§f.",
                    "%border_timer%", TextUtils.getFormattedTime(uhc.getBorderTimer())),
                    uhc.getBorderTimer(), 600, 60, 30, 0, 14400, VariableType.TIME,
                    v -> uhc.setBorderTimer(v.intValue())));

            if (finalHeal != null) {
                addItem(toggle(this, 6, tile(this, ID, "final-heal",
                        new ItemCreator(Material.SPECKLED_MELON).setGlow(finalHeal.isActive()),
                        "§8┃ §eScénario Final Heal",
                        "§7» Accès §eHost\n"
                                + "\n"
                                + " §e┃ §fActivez le scénario de soin général\n"
                                + " §e┃ §fsans le confondre avec le timer PvP.\n"
                                + "\n"
                                + " §e☰ §fStatut : %final_heal_status%\n"
                                + "\n"
                                + "§8» §fCliquez pour §eactiver ou désactiver§f.",
                        "%final_heal_status%", onOff(player, finalHeal.isActive())),
                        () -> finalHeal.setActive(!finalHeal.isActive())));

                addItem(number(this, 12, tile(this, ID, "final-heal-1", new ItemCreator(Material.GOLDEN_APPLE),
                        "§8┃ §bPremier soin",
                        "§7» Accès §bHost\n"
                                + "\n"
                                + " §b┃ §fDéfinissez le moment du premier\n"
                                + " §b┃ §fsoin général de la partie.\n"
                                + "\n"
                                + " §b☰ §fTimer : %final_heal_time_1%\n"
                                + "\n"
                                + "§8» §fCliquez pour §bmodifier§f.",
                        "%final_heal_time_1%", TextUtils.getFormattedTime(finalHeal.getHealTime1())),
                        finalHeal.getHealTime1(), 600, 60, 30, 0, 14400, VariableType.TIME,
                        v -> finalHeal.setHealTime1(v.intValue())));

                addItem(number(this, 13, tile(this, ID, "final-heal-2",
                        new ItemCreator(Material.GOLDEN_APPLE).setAmount(2),
                        "§8┃ §dSecond soin",
                        "§7» Accès §dHost\n"
                                + "\n"
                                + " §d┃ §fDéfinissez le moment du second soin\n"
                                + " §d┃ §fou désactivez cette étape si elle est inutile.\n"
                                + "\n"
                                + " §d☰ §fTimer : %final_heal_time_2%\n"
                                + "\n"
                                + "§8» §fCliquez pour §dmodifier§f.",
                        "%final_heal_time_2%", TextUtils.getFormattedTime(finalHeal.getHealTime2())),
                        finalHeal.getHealTime2(), 600, 60, 30, 0, 14400, VariableType.TIME,
                        v -> finalHeal.setHealTime2(v.intValue())));

                addItem(toggle(this, 14, tile(this, ID, "final-heal-food",
                        new ItemCreator(Material.COOKED_BEEF).setGlow(finalHeal.isHealFood()),
                        "§8┃ §6Restaurer la nourriture",
                        "§7» Accès §6Host\n"
                                + "\n"
                                + " §6┃ §fChoisissez si chaque Final Heal restaure\n"
                                + " §6┃ §faussi la faim et la saturation.\n"
                                + "\n"
                                + " §6☰ §fStatut : %final_heal_food_status%\n"
                                + "\n"
                                + "§8» §fCliquez pour §6activer ou désactiver§f.",
                        "%final_heal_food_status%", onOff(player, finalHeal.isHealFood())),
                        () -> finalHeal.setHealFood(!finalHeal.isHealFood())));
            }

            addItem(submenu(27, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.LIME)), () -> new GameUi(player)));
        }
    }

    public static class LiveMessages extends CustomInventory {

        private static final String ID = "messages-en-direct";
        private static final Map<UUID, Draft> DRAFTS = new ConcurrentHashMap<>();

        public LiveMessages(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu." + ID + ".title", "» Messages en Direct"));
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
            Player player = getPlayer();
            Draft draft = DRAFTS.computeIfAbsent(player.getUniqueId(), id -> new Draft());

            UiItems.panes(this, DyeColor.PURPLE, 0, 8, 26);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

            addItem(new ActionItem(11, tile(this, ID, "content", new ItemCreator(Material.BOOK_AND_QUILL),
                    "§8┃ §aContenu du message",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fSaisissez le texte à diffuser\n"
                            + " §a┃ §fsans passer par une commande Host.\n"
                            + "\n"
                            + " §a☰ §fMessage : %live_message_preview%\n"
                            + "\n"
                            + "§8» §fCliquez pour §autiliser§f.",
                    "%live_message_preview%", draft.text.isEmpty() ? t(UiLang.LIVEMSG_EMPTY) : draft.text)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new AnvilUi(player, LiveMessages.this, event -> {
                        if (event.getSlot() != AnvilUi.AnvilSlot.OUTPUT) return;
                        draft.text = event.getName() == null ? "" : event.getName();
                        open();
                    }).setSlot(draft.text.isEmpty() ? " " : draft.text).open();
                }
            });

            addItem(new ActionItem(13, tile(this, ID, "channel", new ItemCreator(Material.NAME_TAG),
                    "§8┃ §cAffichage",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fChoisissez entre le chat, un titre\n"
                            + " §c┃ §fou la barre d’action.\n"
                            + "\n"
                            + " §c☰ §fCanal : %live_message_channel%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cutiliser§f.",
                    "%live_message_channel%", t(draft.channel.label))) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    draft.channel = draft.channel.next();
                    openAll();
                }
            });

            addItem(new ActionItem(15, tile(this, ID, "audience", head((short) 3),
                    "§8┃ §eDestinataires",
                    "§7» Accès §eHost\n"
                            + "\n"
                            + " §e┃ §fCiblez tous les joueurs, les participants,\n"
                            + " §e┃ §fles spectateurs ou uniquement le staff.\n"
                            + "\n"
                            + " §e☰ §fCible : %live_message_audience%\n"
                            + "\n"
                            + "§8» §fCliquez pour §eutiliser§f.",
                    "%live_message_audience%", t(draft.audience.label))) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    draft.audience = draft.audience.next();
                    openAll();
                }
            });

            addItem(new ActionItem(20, tile(this, ID, "preview", new ItemCreator(Material.EYE_OF_ENDER),
                    "§8┃ §bPrévisualiser",
                    "§7» Accès §bHost\n"
                            + "\n"
                            + " §b┃ §fAffichez le rendu uniquement au Host\n"
                            + " §b┃ §favant la diffusion définitive.\n"
                            + "\n"
                            + "§8» §fCliquez pour §butiliser§f.")) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (rejectEmpty(draft)) return;
                    draft.channel.deliver(player, color(draft.text));
                }
            });

            addItem(new ActionItem(22, tile(this, ID, "send", new ItemCreator(Material.EMERALD),
                    "§8┃ §dDiffuser maintenant",
                    "§7» Accès §dHost\n"
                            + "\n"
                            + " §d┃ §fEnvoyez le message avec le canal\n"
                            + " §d┃ §fet la cible actuellement sélectionnés.\n"
                            + "\n"
                            + "§8» §fCliquez pour §dutiliser§f.")) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if (rejectEmpty(draft)) return;
                    String message = color(draft.text);
                    for (Player target : draft.audience.resolve()) draft.channel.deliver(target, message);
                    LangManager.get().send(UiLang.LIVEMSG_SENT, player);
                    player.closeInventory();
                }
            });

            addItem(submenu(18, backIcon(this, ID, MenuHeads.of(Shape.BACK, DyeColor.PURPLE)), () -> new CommunicationInterface(player)));
        }

        private boolean rejectEmpty(Draft draft) {
            if (!draft.text.isEmpty()) return false;
            LangManager.get().send(UiLang.LIVEMSG_NO_CONTENT, getPlayer());
            return true;
        }

        private static String color(String raw) {
            return ChatColor.translateAlternateColorCodes('&', raw);
        }

        private static final class Draft {
            private String text = "";
            private Channel channel = Channel.CHAT;
            private Audience audience = Audience.ALL;
        }

        private enum Channel {
            CHAT(UiLang.LIVEMSG_CHANNEL_CHAT),
            TITLE(UiLang.LIVEMSG_CHANNEL_TITLE),
            ACTION_BAR(UiLang.LIVEMSG_CHANNEL_ACTIONBAR);

            private final UiLang label;

            Channel(UiLang label) {
                this.label = label;
            }

            Channel next() {
                return values()[(ordinal() + 1) % values().length];
            }

            void deliver(Player target, String message) {
                switch (this) {
                    case TITLE -> DisplayService.title(target, message, "", 60);
                    case ACTION_BAR -> DisplayService.actionBar(target, message);
                    default -> target.sendMessage(message);
                }
            }
        }

        private enum Audience {
            ALL(UiLang.LIVEMSG_AUDIENCE_ALL, up -> true),
            PLAYING(UiLang.LIVEMSG_AUDIENCE_PLAYING, UHCPlayer::isPlaying),
            SPECTATORS(UiLang.LIVEMSG_AUDIENCE_SPECTATORS, up -> !up.isPlaying()),
            STAFF(UiLang.LIVEMSG_AUDIENCE_STAFF, up -> up.getPlayer().hasPermission("novauhc.host"));

            private final UiLang label;
            private final Predicate<UHCPlayer> filter;

            Audience(UiLang label, Predicate<UHCPlayer> filter) {
                this.label = label;
                this.filter = filter;
            }

            Audience next() {
                return values()[(ordinal() + 1) % values().length];
            }

            List<Player> resolve() {
                List<Player> targets = new ArrayList<>();
                for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getOnlineUHCPlayers()) {
                    Player player = uhcPlayer.getPlayer();
                    if (player != null && player.isOnline() && filter.test(uhcPlayer)) targets.add(player);
                }
                return targets;
            }
        }
    }
}
