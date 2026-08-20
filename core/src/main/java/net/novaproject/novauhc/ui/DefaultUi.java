package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.display.DisplayService;
import net.novaproject.novauhc.display.apollo.ApolloConfigUi;
import net.novaproject.novauhc.display.apollo.VisualFeedback;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.ui.WorldUi.WorldEntityUi;
import net.novaproject.novauhc.ui.config.ScenariosUi;
import net.novaproject.novauhc.ui.config.TeamConfigUi;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.ConfigUtils;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DefaultUi extends CustomInventory {

    private static final String ID = "menu.configuration-principale.";

    public DefaultUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of(ID + "title", "» Configuration Principale ✓"));
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
        UHCManager uhc = UHCManager.get();

        Map<String, Object> ph = Map.of(
                "%segond_color%", "",
                "%slots%", String.valueOf(uhc.getSlot()),
                "%lunar_active%", UiItems.onOff(getPlayer(), VisualFeedback.isEnabled()),
                "%whitelist_active%", UiItems.onOff(getPlayer(), Bukkit.hasWhitelist()),
                "%spec_active%", UiItems.onOff(getPlayer(), uhc.isSpectator()));

        UiItems.panes(this, DyeColor.BROWN, 0, 8, 45, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 46, 52);

        addItem(new StaticItem(44, new ItemCreator(Material.STAINED_GLASS_PANE)
                .setDurability((short) DyeColor.WHITE.getData())
                .setLores(lore("btn-44", "%segond_color%", ph))));

        addItem(new ActionItem(16, tile(UiItems.createCustomButon(MenuHeads.WORLD, " ", null), "btn-16",
                "§8┃ §3Monde §f& §3Entité",
                "§7» Accès §3Host\n"
                        + "\n"
                        + " §3┃ §fPermet de §3configurer §fles paramètres\n"
                        + " §3┃ §fde la §3map§f et des §3entités§f.\n"
                        + "\n"
                        + "§8» §fClique droit pour §3modifier.", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WorldEntityUi(getPlayer()).open();
            }
        });

        addItem(new ActionItem(43, tile(UiItems.createCustomButon(MenuHeads.HEADER_SCENARIOS, " ", null), "btn-43",
                "§8┃ §5Scenario",
                "§7» Accès §5Host\n"
                        + "\n"
                        + " §5┃ §fPermet d'§5ajouter§f des scénarios qui \n"
                        + " §5┃ §fdynamiseront la §5partie§f.\n"
                        + "\n"
                        + "§8» §fClique droit pour §5modifier.", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenariosUi(getPlayer()).open();
            }
        });

        addItem(new ActionItem(31, tile(UiItems.createCustomButon(MenuHeads.GAMEMODE, " ", null), "btn-31",
                "§8┃ §cMode de Jeu",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c┃ §fPermet d'accedez au differant\n"
                        + " §c┃ §f§cMode §fde §cJeu §fdisponible\n"
                        + "\n"
                        + "§8» §fClique pour y §caccedez", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ScenariosUi(getPlayer(), true).open();
            }
        });

        addItem(new ActionItem(10, tile(new ItemCreator(Material.SKULL_ITEM).setDurability((short) 3), "btn-10",
                "§8┃ §2Slots",
                "§7» Accès §2Host\n"
                        + "\n"
                        + " §2┃ §fVous permet de §2modifier §fle nombre de §2joueurs§f autorisés.\n"
                        + " §2┃ §fà se §2connecter§f à la §2partie§f.\n"
                        + "\n"
                        + "§8» §fClique droit pour §2modifier.", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfigVarUi(getPlayer(), 10, 5, 1, 10, 5, 1,
                        uhc.getSlot(), 1, 100, DefaultUi.this, VariableType.INTEGER) {
                    @Override
                    public void onChange(Number newValue) {
                        uhc.setSlot(newValue.intValue());
                    }
                }.open();
            }
        });

        boolean started = uhc.isStarted();
        ItemCreator startIcon = started
                ? tile(new ItemCreator(Material.INK_SACK).setDurability((short) DyeColor.GRAY.getDyeData()),
                "btn-49-cancel",
                "§8┃ §c§lAnnuler le Lancement",
                "§7» Accès §cHost\n"
                        + "\n"
                        + " §c☰ §fPas sûr §7?\n"
                        + "\n"
                        + " §c┃ §fPermet d'annuler le lancement de la §cPartie\n"
                        + "\n"
                        + "§8» §fClique pour §cannuler.", ph)
                : tile(new ItemCreator(Material.INK_SACK).setDurability((short) DyeColor.LIME.getDyeData()),
                "btn-49",
                "§8┃ §a§lLancer la Partie",
                "§7» Accès §aHost\n"
                        + "\n"
                        + " §a☰ §fTout est §aPrêt §7?\n"
                        + "\n"
                        + " §a┃ §fPermet de lancer la §aPartie\n"
                        + "\n"
                        + "§8» §fClique pour y §aactiver.", ph);

        addItem(new ActionItem(49, startIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (!uhc.isStarted()) {
                    uhc.setCanceled(false);
                    uhc.onStart();
                    getPlayer().closeInventory();
                    uhc.setStarted(true);
                    return;
                }
                uhc.setCanceled(true);
                getPlayer().closeInventory();
                uhc.setStarted(false);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    DisplayService.title(p,
                            ConfigUtils.getLangConfig().getString("start_canceled.title"),
                            ConfigUtils.getLangConfig().getString("start_canceled.subtitle"),
                            ConfigUtils.getLangConfig().getInt("start_canceled.duration"));
                    p.setLevel(0);
                    p.setExp(0f);
                }
            }
        });

        addItem(new ActionItem(22, tile(new ItemCreator(Material.ITEM_FRAME), "btn-22",
                "§8┃ §9Option de la partie",
                "§7» Accès §9Host\n"
                        + "\n"
                        + " §9┃ §fPermet d'accedez au §9regles§f/§9option \n"
                        + " §9┃ §fde votre partie.\n"
                        + "\n"
                        + "§8» §fClique pour y §9accedez", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new GameUi(getPlayer()).open();
            }
        });

        addItem(new ActionItem(51, tile(new ItemCreator(Material.BOOK_AND_QUILL), "btn-51",
                "§8┃ §f§lTemplate",
                "§7» Accès §f§lHost\n"
                        + "\n"
                        + " §f§l┃ §fPermet d'accedez a vos §f§lTemplate\n"
                        + " §f§l┃ §fafin de preparez vos partie plus rapidement\n"
                        + "\n"
                        + "§8» §fClique droit pour §f§lmodifier.", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new PreconfigUi(getPlayer(), DefaultUi.this).open();
            }
        });

        addItem(new ActionItem(2, tile(new ItemCreator(Material.BARRIER), "btn-2",
                "§8┃ §4§lArret du Serveur",
                "§7» Accès §4Host\n"
                        + "\n"
                        + " §4┃ §fPermet d'§4§lArretez §fl'host. \n"
                        + " §4┃ §fUne confirmation vous seras §4§limposez§f.\n"
                        + "\n"
                        + "§8» §fClique droit pour §4modifier.", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new ConfirmMenu(getPlayer(),
                        t(UiLang.MAIN_STOP_CONFIRM),
                        Bukkit::shutdown,
                        () -> {
                        },
                        DefaultUi.this).open();
            }
        });

        addItem(new ActionItem(47, tile(new ItemCreator(Material.WATCH), "btn-47",
                "§8┃ §eAccessibilité §fde la §ePartie",
                "§7» Accès §eHost\n"
                        + "\n"
                        + " §e┃ §fPermet §ad'activé §f/ §cdesactivé\n"
                        + " §e┃ §fla §eWhitelist §fet les §eSpectateur.\n"
                        + "\n"
                        + " §e☰ §fStatus whitelist : %whitelist_active%\n"
                        + " §e☰ §fStatus spectateur: %spec_active%\n"
                        + "\n"
                        + "§8» §fClique pour y §eaccedez", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new WhiteListUi(getPlayer()).open();
            }
        });

        addItem(new ActionItem(37, tile(new ItemCreator(Material.BEACON)
                .setGlow(VisualFeedback.isEnabled()), "btn-37",
                "§8┃ §bLunar",
                "§7» Accès §bHost\n"
                        + "\n"
                        + " §b┃ §fPermet §ad'activé §f/ §cdesactivé\n"
                        + " §b┃ §fles visuels §bLunar.\n"
                        + "\n"
                        + " §b☰ §fStatus : %lunar_active%\n"
                        + "\n"
                        + "§8» §fClique droit pour §bmodifier.\n"
                        + "§8» §fClique gauche pour §baccedez.", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (e.isRightClick()) {
                    VisualFeedback.setEnabled(!VisualFeedback.isEnabled());
                    openAll();
                    return;
                }
                new ApolloConfigUi(getPlayer()).open();
            }
        });

        boolean atRules = uhc.getWaitState().equals(UHCManager.WaitState.WAIT_STATE);
        ItemCreator lobbyIcon = atRules
                ? tile(new ItemCreator(Material.BED), "btn-6-lobby",
                "§8┃ §7Retour au Lobby",
                "§7» Accès §7Host\n"
                        + "\n"
                        + " §7┃ §fLes joueurs sont dans la §7salle des règles§f.\n"
                        + " §7┃ §fRamenez-les tous au §7lobby§f.\n"
                        + "\n"
                        + "§8» §fCliquez pour §7téléporter§f.", ph)
                : tile(new ItemCreator(Material.WRITTEN_BOOK), "btn-6",
                "§8┃ §7Salle des règles",
                "§7» Accès §7Host\n"
                        + "\n"
                        + " §7┃ §fLes joueurs sont au §7lobby§f.\n"
                        + " §7┃ §fEnvoyez-les tous en §7salle des règles§f.\n"
                        + "\n"
                        + "§8» §fCliquez pour §7téléporter§f.", ph);

        addItem(new ActionItem(6, lobbyIcon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if (atRules) {
                    uhc.setWaitState(UHCManager.WaitState.LOBBY_STATE);
                    Bukkit.getOnlinePlayers().forEach(p -> p.teleport(Common.get().getLobbySpawn()));
                } else {
                    uhc.setWaitState(UHCManager.WaitState.WAIT_STATE);
                    Bukkit.getOnlinePlayers().forEach(p -> p.teleport(Common.get().getRulesSpawn()));
                }
                openAll();
            }
        });

        addItem(new ActionItem(4, tile(new ItemCreator(Material.BANNER)
                .setDurability((short) DyeColor.ORANGE.getDyeData()), "btn-4",
                "§8┃ §6Teams",
                "§8» §fAccès : §6Host\n"
                        + "\n"
                        + " §6┃ §fPermet de gérer les §6options§f des équipes.\n"
                        + "       \n"
                        + "§8» §fCliquez pour y §6accéder§f.", ph)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new TeamConfigUi(getPlayer()).open();
            }
        });
    }

    private ItemCreator tile(ItemCreator item, String key, String name, String lore, Map<String, Object> ph) {
        return item
                .setName(t(DynamicLang.of(ID + key + ".name", name), ph))
                .setLores(lore(key, lore, ph));
    }

    private List<String> lore(String key, String lore, Map<String, Object> ph) {
        return Arrays.asList(t(DynamicLang.of(ID + key + ".lore", lore), ph).split("\n", -1));
    }
}
