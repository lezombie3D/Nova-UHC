package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.cmd.host.ReviveSub;
import net.novaproject.novauhc.command.cmd.host.SpecSub;
import net.novaproject.novauhc.event.UhcRoleEvents.UhcTeamEliminatedEvent;
import net.novaproject.novauhc.game.SpectatorNotifier;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.Lang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lobby.FreezeManager;
import net.novaproject.novauhc.lobby.VanishManager;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.player.utils.InventorySnapshots;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.role.Bonds.VictoryLinks;
import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.camps.Camps;
import net.novaproject.novauhc.scenario.role.reveal.KPIBuilder;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static net.novaproject.novauhc.ui.UiItems.onOff;

public final class ModerationMenus {

    private ModerationMenus() {
    }

    private static final int PLAYERS_PER_PAGE = 28;

    private static final Lang STATUS_PLAYING = DynamicLang.of("ui.moderation.status_playing", "§aEn jeu");
    private static final Lang STATUS_SPECTATOR = DynamicLang.of("ui.moderation.status_spectator", "§7Spectateur");
    private static final Lang STATUS_ELIMINATED = DynamicLang.of("ui.moderation.status_eliminated", "§cÉliminé");
    private static final Lang STATUS_OFFLINE = DynamicLang.of("ui.moderation.status_offline", "§8Hors ligne");
    private static final Lang SEARCH_HINT = DynamicLang.of("ui.moderation.search_hint", "Nom du joueur");
    private static final Lang SEARCH_NOT_FOUND = DynamicLang.of("ui.moderation.search_not_found",
            "§c✗ Aucun joueur connecté ne correspond à §f%player%§c.");
    private static final Lang TARGET_OFFLINE = DynamicLang.of("ui.moderation.target_offline",
            "§c✗ §f%player% §cn'est plus connecté.");
    private static final Lang HEALED = DynamicLang.of("ui.moderation.healed", "§d✦ §f%player% §da été soigné.");
    private static final Lang MOD_HEALED = DynamicLang.of("ui.specnotify.healed",
            "§d▸ §7%staff% §7a §dsoigné §f%player%");
    private static final Lang MOD_EXCLUDED = DynamicLang.of("ui.specnotify.excluded",
            "§4▸ §7%staff% §7a §4exclu §f%player% §7de la partie");
    private static final Lang CONFIRM_EXCLUDE = DynamicLang.of("ui.moderation.confirm_exclude",
            "§cÊtes-vous sûr de vouloir exclure %player% de la partie ?");
    private static final Lang CONFIRM_REVIVE = DynamicLang.of("ui.moderation.confirm_revive",
            "§6Êtes-vous sûr de vouloir réanimer %player% ?");

    private static ItemStack playerHead() {
        return new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
    }

    private static String nameOf(UHCPlayer target) {
        Player online = target.getPlayer();
        if (online != null) return online.getName();
        String offline = Bukkit.getOfflinePlayer(target.getUniqueId()).getName();
        return offline != null ? offline : target.getUniqueId().toString();
    }

    private static String status(Player viewer, UHCPlayer target) {
        if (target.getPlayer() == null) return LangManager.get().get(STATUS_OFFLINE, viewer);
        if (target.isSpec()) return LangManager.get().get(STATUS_SPECTATOR, viewer);
        if (target.isPlaying()) return LangManager.get().get(STATUS_PLAYING, viewer);
        return LangManager.get().get(STATUS_ELIMINATED, viewer);
    }

    private static void exclude(Player staff, UHCPlayer target) {
        Player tp = target.getPlayer();
        if (tp == null) {
            LangManager.get().send(TARGET_OFFLINE, staff, Map.of("%player%", nameOf(target)));
            return;
        }
        UUID uuid = tp.getUniqueId();
        String name = tp.getName();
        boolean eliminate = !UHCManager.get().isLobby() && target.isPlaying();
        Map<String, ItemStack[]> snapshot = InventorySnapshots.savePlayerInventory(tp);
        Location location = tp.getLocation();
        UHCTeam team = target.getTeam().orElse(null);

        target.setPlaying(false);
        target.setTeam(Optional.empty());

        if (eliminate) {
            VictoryLinks.onDeath(uuid);
            InventorySnapshots.dropAll(snapshot, location);
            InventorySnapshots.clearPlayerInventory(tp);
            UHCManager.get().getStatsTracker().addDeath(uuid);
            if (team != null && !team.isAlive()) {
                Bukkit.getPluginManager().callEvent(new UhcTeamEliminatedEvent(team));
            }
        }

        tp.kickPlayer(LangManager.get().get(CoreLang.COMMON_KICKED, tp));
        Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_KICKED_MESSAGE,
                Map.of("%player%", name)));
        Main.get().getLogger().info("[MODERATION] " + staff.getName() + " a exclu " + name + " de la partie");
        SpectatorNotifier.notifySpectators(MOD_EXCLUDED, Map.of(
                "%staff%", staff.getName(), "%player%", name), null);

        if (eliminate) UHCManager.get().checkVictory();
    }

    public static class Game extends CustomInventory {

        public Game(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.moderation-partie.title", "» Modération de la Partie"));
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

            ItemCreator silence = new ItemCreator(Material.NOTE_BLOCK)
                    .setName(t(DynamicLang.of("menu.moderation-partie.silence.name", "§8┃ §aSilence global")))
                    .setGlow(!uhc.isChatDisabled());
            silence.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-partie.silence.lore",
                    "§7» Accès §aHost / Co-Host\n"
                            + "\n"
                            + " §a┃ §fCoupez ou rétablissez le chat global\n"
                            + " §a┃ §favec les commandes Host existantes.\n"
                            + "\n"
                            + " §a☰ §fStatut : %global_silence_status%\n"
                            + "\n"
                            + "§8» §fCliquez pour §aactiver ou désactiver§f."))
                    .replace("%global_silence_status%", onOff(player, uhc.isChatDisabled()))
                    .split("\n", -1)));

            addItem(new ActionItem(11, silence) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    uhc.setChatDisabled(!uhc.isChatDisabled());
                    openAll();
                }
            });

            ItemCreator players = new ItemCreator(playerHead())
                    .setName(t(DynamicLang.of("menu.moderation-partie.players.name", "§8┃ §cGérer les joueurs")));
            players.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-partie.players.lore",
                    "§7» Accès §cHost / Co-Host\n"
                            + "\n"
                            + " §c┃ §fSélectionnez un joueur puis appliquez\n"
                            + " §c┃ §fune action de modération ciblée.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §caccéder§f.")).split("\n", -1)));

            addItem(new ActionItem(13, players) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new Players(player).open();
                }
            });

            ItemCreator vanish = new ItemCreator(Material.POTION)
                    .setDurability((short) 8206)
                    .addItemFlags(ItemFlag.HIDE_POTION_EFFECTS)
                    .setName(t(DynamicLang.of("menu.moderation-partie.vanish.name", "§8┃ §eVanish du staff")))
                    .setGlow(VanishManager.isVanished(player.getUniqueId()));
            vanish.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-partie.vanish.lore",
                    "§7» Accès §eHost / Co-Host\n"
                            + "\n"
                            + " §e┃ §fRendez-vous invisible aux joueurs\n"
                            + " §e┃ §ftout en restant visible par le staff.\n"
                            + "\n"
                            + " §e☰ §fStatut : %staff_vanish_status%\n"
                            + "\n"
                            + "§8» §fCliquez pour §eactiver ou désactiver§f."))
                    .replace("%staff_vanish_status%", onOff(player, VanishManager.isVanished(player.getUniqueId())))
                    .split("\n", -1)));

            addItem(new ActionItem(15, vanish) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    VanishManager.toggle(player);
                    openAll();
                }
            });

            addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.ORANGE),
                    t(DynamicLang.of("menu.moderation-partie.back.name", "§8┃ §7§lRetour")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new GameUi(player).open();
                }
            });
        }
    }

    public static class Players extends CustomInventory {

        private int pages = 1;

        public Players(Player player) {
            super(player);
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.moderation-joueurs.title", "» Joueurs à Modérer"));
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
            Player player = getPlayer();

            UiItems.panes(this, DyeColor.RED, 0, 8, 53);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

            ItemCreator search = new ItemCreator(Material.COMPASS)
                    .setName(t(DynamicLang.of("menu.moderation-joueurs.search.name", "§8┃ §aRechercher un joueur")));
            search.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueurs.search.lore",
                    "§7» Accès §aHost / Co-Host\n"
                            + "\n"
                            + " §a┃ §fSaisissez un pseudo pour retrouver\n"
                            + " §a┃ §frapidement un joueur connecté.\n"
                            + "\n"
                            + "§8» §fCliquez pour §autiliser§f.")).split("\n", -1)));

            addItem(new ActionItem(4, search) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new AnvilUi(player, event -> {
                        if (event.getSlot() != AnvilUi.AnvilSlot.OUTPUT) {
                            open();
                            return;
                        }
                        Player found = Bukkit.getPlayer(event.getName());
                        UHCPlayer target = found == null ? null : UHCPlayerManager.get().getPlayer(found);
                        if (target == null) {
                            LangManager.get().send(SEARCH_NOT_FOUND, player, Map.of("%player%", event.getName()));
                            open();
                            return;
                        }
                        new PlayerActions(player, target.getUniqueId()).open();
                    }).setSlot(LangManager.get().get(SEARCH_HINT, player)).open();
                }
            });

            String entryName = t(DynamicLang.of("menu.moderation-joueurs.player-dummy.name", "§8┃ §e%pseudo%"));
            String entryLore = t(DynamicLang.of("menu.moderation-joueurs.player-dummy.lore",
                    "§7» Élément §edynamique\n"
                            + "\n"
                            + " §e┃ §fStatut : %player_moderation_status%\n"
                            + " §e┃ §fCliquez pour ouvrir ses actions.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §eaccéder§f."));

            List<UHCPlayer> targets = new ArrayList<>(UHCPlayerManager.get().getOnlineUHCPlayers());

            pages = paginate(targets, PLAYERS_PER_PAGE,
                    n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                    (target, slot) -> {
                        String name = nameOf(target);
                        ItemCreator icon = new ItemCreator(playerHead())
                                .setOwner(name)
                                .setName(entryName.replace("%pseudo%", name));
                        icon.setLores(Arrays.asList(entryLore
                                .replace("%player_moderation_status%", status(player, target))
                                .split("\n", -1)));

                        addItem(new ActionItem(slot, icon) {
                            @Override
                            public void onClick(InventoryClickEvent e) {
                                new PlayerActions(player, target.getUniqueId()).open();
                            }
                        });
                    });

            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.RED),
                    t(DynamicLang.of("menu.moderation-joueurs.previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });

            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.RED),
                    t(DynamicLang.of("menu.moderation-joueurs.next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });

            addItem(new ActionItem(45, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.RED),
                    t(DynamicLang.of("menu.moderation-joueurs.back.name", "§8┃ §7§lRetour")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new Game(player).open();
                }
            });
        }
    }

    public static class SpectatorActions extends CustomInventory {

        private final UUID targetUuid;

        public SpectatorActions(Player player, UUID targetUuid) {
            super(player);
            this.targetUuid = targetUuid;
        }

        private UHCPlayer target() {
            return UHCPlayerManager.get().getPlayer(targetUuid);
        }

        private String label() {
            UHCPlayer target = target();
            if (target != null) return nameOf(target);
            String offline = Bukkit.getOfflinePlayer(targetUuid).getName();
            return offline != null ? offline : targetUuid.toString();
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.spectateur-joueur.title", "» %player%"))
                    .replace("%player%", label());
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
        public String broadcastKey() {
            return getClass().getName() + ":" + targetUuid;
        }

        @Override
        public void setup() {
            Player player = getPlayer();
            UHCPlayer target = target();
            Player online = target == null ? null : target.getPlayer();
            if (online == null || !online.isOnline()) {
                LangManager.get().send(TARGET_OFFLINE, player, Map.of("%player%", label()));
                player.closeInventory();
                return;
            }

            UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 26);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26);

            Role role = KPIBuilder.roleOf(target);
            Camps camp = role == null ? null : role.getCamp();

            ItemCreator head = new ItemCreator(playerHead())
                    .setOwner(nameOf(target))
                    .setName(t(DynamicLang.of("menu.spectateur-joueur.player.name", "§8┃ §b%player%"))
                            .replace("%player%", label()));
            head.setLores(Arrays.asList(t(DynamicLang.of("menu.spectateur-joueur.player.lore",
                    "§7» Élément §bdynamique\n"
                            + "\n"
                            + " §b┃ §fÉtat : %player_moderation_status%\n"
                            + " §b┃ §fRôle : %player_role%\n"
                            + " §b┃ §fCamp : %player_camp%\n"
                            + " §b┃ §fVie : §c%player_health% ❤\n"))
                    .replace("%player_moderation_status%", status(player, target))
                    .replace("%player_role%", role == null ? "§7—" : role.getName())
                    .replace("%player_camp%", camp == null ? "§7—" : camp.getColor() + camp.getName())
                    .replace("%player_health%", String.format("%.1f", online.getHealth() / 2.0))
                    .split("\n", -1)));
            addItem(new StaticItem(4, head));

            ItemCreator sheet = new ItemCreator(Material.BOOK)
                    .setName(t(DynamicLang.of("menu.spectateur-joueur.sheet.name", "§8┃ §aFiche complète")));
            sheet.setLores(Arrays.asList(t(DynamicLang.of("menu.spectateur-joueur.sheet.lore",
                    "§7» Accès §aSpectateur\n"
                            + "\n"
                            + " §a┃ §fInventaire, armure, effets, combat,\n"
                            + " §a┃ §fpouvoirs et cooldowns en direct.\n"
                            + "\n"
                            + "§8» §fCliquez pour y §aaccéder§f.")).split("\n", -1)));

            addItem(new ActionItem(11, sheet) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new InvseeUi(player, online).open();
                }
            });

            ItemCreator teleport = new ItemCreator(Material.ENDER_PEARL)
                    .setName(t(DynamicLang.of("menu.spectateur-joueur.teleport.name", "§8┃ §bSe téléporter")));
            teleport.setLores(Arrays.asList(t(DynamicLang.of("menu.spectateur-joueur.teleport.lore",
                    "§7» Accès §bSpectateur\n"
                            + "\n"
                            + " §b┃ §fRejoignez la position actuelle\n"
                            + " §b┃ §fde la cible.\n"
                            + "\n"
                            + "§8» §fCliquez pour §butiliser§f.")).split("\n", -1)));

            addItem(new ActionItem(13, teleport) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    player.closeInventory();
                    player.teleport(online.getLocation());
                    LangManager.get().send(CoreLang.COMMON_SPEC_TELEPORTED, player,
                            Map.of("%player%", online.getName()));
                }
            });

            UHCPlayer stats = target;
            ItemCreator luck = new ItemCreator(Material.IRON_PICKAXE)
                    .setName(t(DynamicLang.of("menu.spectateur-joueur.topluck.name", "§8┃ §6TopLuck")));
            luck.setLores(Arrays.asList(t(DynamicLang.of("menu.spectateur-joueur.topluck.lore",
                    "§7» Accès §6Spectateur\n"
                            + "\n"
                            + " §6┃ §fPierre minée : §f%stone%\n"
                            + " §b┃ §fDiamant : §b%diamond% §8(§b1/%diamond_ratio%§8)\n"
                            + " §6┃ §fOr : §6%gold% §8(§61/%gold_ratio%§8)\n"
                            + " §7┃ §fFer : §f%iron% §8(§f1/%iron_ratio%§8)\n"
                            + "\n"
                            + "§8» §fCliquez pour le §6classement complet§f."))
                    .replace("%stone%", String.valueOf(stats.getMinedStone()))
                    .replace("%diamond_ratio%", TopLuckUi.ratio(stats.getMinedStone(), stats.getMinedDiamond()))
                    .replace("%diamond%", String.valueOf(stats.getMinedDiamond()))
                    .replace("%gold_ratio%", TopLuckUi.ratio(stats.getMinedStone(), stats.getMinedGold()))
                    .replace("%gold%", String.valueOf(stats.getMinedGold()))
                    .replace("%iron_ratio%", TopLuckUi.ratio(stats.getMinedStone(), stats.getMinedIron()))
                    .replace("%iron%", String.valueOf(stats.getMinedIron()))
                    .split("\n", -1)));

            addItem(new ActionItem(15, luck) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new TopLuckUi(player).open();
                }
            });

            if (!RankManager.isStaff(player)) return;

            ItemCreator freeze = new ItemCreator(Material.PACKED_ICE)
                    .setName(t(DynamicLang.of("menu.spectateur-joueur.freeze.name", "§8┃ §cFreeze / Unfreeze")))
                    .setGlow(FreezeManager.isFrozen(targetUuid));
            freeze.setLores(Arrays.asList(t(DynamicLang.of("menu.spectateur-joueur.freeze.lore",
                    "§7» Accès §cHost / Co-Host\n"
                            + "\n"
                            + " §c┃ §fGelez la cible pendant une vérification\n"
                            + " §c┃ §fou rendez-lui ses mouvements.\n"
                            + "\n"
                            + " §c☰ §fStatut : %player_freeze_status%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cactiver ou désactiver§f."))
                    .replace("%player_freeze_status%", onOff(player, FreezeManager.isFrozen(targetUuid)))
                    .split("\n", -1)));

            addItem(new ActionItem(22, freeze) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    FreezeManager.toggle(player, online);
                    openAll();
                }
            });
        }
    }

    public static class PlayerActions extends CustomInventory {

        private final UUID targetUuid;

        public PlayerActions(Player player, UUID targetUuid) {
            super(player);
            this.targetUuid = targetUuid;
        }

        private UHCPlayer target() {
            return UHCPlayerManager.get().getPlayer(targetUuid);
        }

        private String label() {
            UHCPlayer target = target();
            if (target != null) return nameOf(target);
            String offline = Bukkit.getOfflinePlayer(targetUuid).getName();
            return offline != null ? offline : targetUuid.toString();
        }

        private Player online() {
            UHCPlayer target = target();
            Player tp = target == null ? null : target.getPlayer();
            if (tp == null || !tp.isOnline()) {
                LangManager.get().send(TARGET_OFFLINE, getPlayer(), Map.of("%player%", label()));
                return null;
            }
            return tp;
        }

        @Override
        public String getTitle() {
            return t(DynamicLang.of("menu.moderation-joueur.title", "» Modération : %player%"))
                    .replace("%player%", label());
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
        public String broadcastKey() {
            return getClass().getName() + ":" + targetUuid;
        }

        @Override
        public void setup() {
            Player player = getPlayer();
            UHCPlayer target = target();
            String label = label();
            String moderationStatus = target == null
                    ? LangManager.get().get(STATUS_OFFLINE, player)
                    : status(player, target);

            UiItems.panes(this, DyeColor.YELLOW, 0, 8, 35);
            UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 18, 26, 28, 34);

            if (target != null) {
                ItemCreator head = new ItemCreator(playerHead())
                        .setOwner(nameOf(target))
                        .setName(t(DynamicLang.of("menu.moderation-joueur.player.name", "§8┃ §a%player%"))
                                .replace("%player%", label));
                head.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueur.player.lore",
                        "§7» Élément §adynamique\n"
                                + "\n"
                                + " §a┃ §fÉtat : %player_moderation_status%\n"))
                        .replace("%player_moderation_status%", moderationStatus)
                        .split("\n", -1)));
                addItem(new StaticItem(4, head));
            }

            ItemCreator freeze = new ItemCreator(Material.PACKED_ICE)
                    .setName(t(DynamicLang.of("menu.moderation-joueur.freeze.name", "§8┃ §cFreeze / Unfreeze")))
                    .setGlow(FreezeManager.isFrozen(targetUuid));
            freeze.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueur.freeze.lore",
                    "§7» Accès §cHost / Co-Host\n"
                            + "\n"
                            + " §c┃ §fGelez le joueur pendant une vérification\n"
                            + " §c┃ §fou rendez-lui ses mouvements.\n"
                            + "\n"
                            + " §c☰ §fStatut : %player_freeze_status%\n"
                            + "\n"
                            + "§8» §fCliquez pour §cactiver ou désactiver§f."))
                    .replace("%player_freeze_status%", onOff(player, FreezeManager.isFrozen(targetUuid)))
                    .split("\n", -1)));

            addItem(new ActionItem(11, freeze) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    Player tp = online();
                    if (tp == null) return;
                    FreezeManager.toggle(player, tp);
                    openAll();
                }
            });

            ItemCreator spectator = new ItemCreator(Material.EYE_OF_ENDER)
                    .setName(t(DynamicLang.of("menu.moderation-joueur.spectator.name", "§8┃ §eJoueur / Spectateur")))
                    .setGlow(target != null && target.isSpec());
            spectator.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueur.spectator.lore",
                    "§7» Accès §eHost / Co-Host\n"
                            + "\n"
                            + " §e┃ §fBasculez la cible entre joueur actif\n"
                            + " §e┃ §fet mode spectateur.\n"
                            + "\n"
                            + " §e☰ §fStatut : %player_spectator_status%\n"
                            + "\n"
                            + "§8» §fCliquez pour §eactiver ou désactiver§f."))
                    .replace("%player_spectator_status%", target != null && target.isSpec()
                            ? LangManager.get().get(STATUS_SPECTATOR, player)
                            : LangManager.get().get(STATUS_PLAYING, player))
                    .split("\n", -1)));

            addItem(new ActionItem(13, spectator) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    UHCPlayer up = target();
                    if (up == null || online() == null) return;
                    SpecSub.setSpectator(player, up, !up.isSpec());
                    openAll();
                }
            });

            ItemCreator teleport = new ItemCreator(Material.ENDER_PEARL)
                    .setName(t(DynamicLang.of("menu.moderation-joueur.teleport.name", "§8┃ §bSe téléporter au joueur")));
            teleport.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueur.teleport.lore",
                    "§7» Accès §bHost / Co-Host\n"
                            + "\n"
                            + " §b┃ §fTéléportez le membre du staff\n"
                            + " §b┃ §fà la position actuelle de la cible.\n"
                            + "\n"
                            + "§8» §fCliquez pour §butiliser§f.")).split("\n", -1)));

            addItem(new ActionItem(15, teleport) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    Player tp = online();
                    if (tp == null) return;
                    player.closeInventory();
                    player.teleport(tp.getLocation());
                    LangManager.get().send(CoreLang.COMMON_SPEC_TELEPORTED, player,
                            Map.of("%player%", tp.getName()));
                }
            });

            ItemCreator heal = new ItemCreator(Material.SPECKLED_MELON)
                    .setName(t(DynamicLang.of("menu.moderation-joueur.heal.name", "§8┃ §dSoigner le joueur")));
            heal.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueur.heal.lore",
                    "§7» Accès §dHost / Co-Host\n"
                            + "\n"
                            + " §d┃ §fRestaurez sa vie avec l’action Host\n"
                            + " §d┃ §fdéjà présente dans le plugin.\n"
                            + "\n"
                            + "§8» §fCliquez pour §dutiliser§f.")).split("\n", -1)));

            addItem(new ActionItem(20, heal) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    Player tp = online();
                    if (tp == null) return;
                    tp.setHealth(tp.getMaxHealth());
                    tp.setFoodLevel(20);
                    tp.setSaturation(20f);
                    tp.setFireTicks(0);
                    LangManager.get().send(HEALED, player, Map.of("%player%", tp.getName()));
                    SpectatorNotifier.notifySpectators(MOD_HEALED, Map.of(
                            "%staff%", player.getName(), "%player%", tp.getName()), tp);
                    openAll();
                }
            });

            ItemCreator revive = new ItemCreator(Material.GHAST_TEAR)
                    .setName(t(DynamicLang.of("menu.moderation-joueur.revive.name", "§8┃ §6Réanimer le joueur")));
            revive.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueur.revive.lore",
                    "§7» Accès §6Host / Co-Host\n"
                            + "\n"
                            + " §6┃ §fUtilisez la logique de revive Host\n"
                            + " §6┃ §fsi la partie autorise cette action.\n"
                            + "\n"
                            + "§8» §fCliquez pour §6utiliser§f.")).split("\n", -1)));

            addItem(new ActionItem(22, revive) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    UHCPlayer up = target();
                    if (up == null || online() == null) return;
                    new ConfirmMenu(player,
                            LangManager.get().get(CONFIRM_REVIVE, player, Map.of("%player%", label())),
                            () -> {
                                ReviveSub.revive(player, up);
                                openAll();
                            },
                            PlayerActions.this::openAll,
                            PlayerActions.this).open();
                }
            });

            ItemCreator exclude = new ItemCreator(Material.BARRIER)
                    .setName(t(DynamicLang.of("menu.moderation-joueur.exclude.name", "§8┃ §4Exclure de la partie")));
            exclude.setLores(Arrays.asList(t(DynamicLang.of("menu.moderation-joueur.exclude.lore",
                    "§7» Accès §4Host / Co-Host\n"
                            + "\n"
                            + " §4┃ §fOuvrez une confirmation avant\n"
                            + " §4┃ §fde retirer et déconnecter la cible.\n"
                            + "\n"
                            + "§8» §fCliquez pour §4utiliser§f.")).split("\n", -1)));

            addItem(new ActionItem(24, exclude) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    UHCPlayer up = target();
                    if (up == null || online() == null) return;
                    new ConfirmMenu(player,
                            LangManager.get().get(CONFIRM_EXCLUDE, player, Map.of("%player%", label())),
                            () -> {
                                ModerationMenus.exclude(player, up);
                                new Players(player).open();
                            },
                            PlayerActions.this::openAll,
                            PlayerActions.this).open();
                }
            });

            addItem(new ActionItem(27, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.BACK, DyeColor.YELLOW),
                    t(DynamicLang.of("menu.moderation-joueur.back.name", "§8┃ §7§lRetour")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    new Players(player).open();
                }
            });
        }
    }
}
