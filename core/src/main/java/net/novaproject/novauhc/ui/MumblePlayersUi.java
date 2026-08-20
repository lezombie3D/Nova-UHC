package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.MumbleUiLang;
import net.novaproject.novauhc.mumble.MumbleManager;
import net.novaproject.novauhc.mumble.MumbleManager.LiveUser;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class MumblePlayersUi extends CustomInventory {

    private static final int FILL_FROM = 0;
    private static final int FILL_TO = 44;
    private static final int PER_PAGE = FILL_TO - FILL_FROM + 1;

    // %status_dot% : pastille d'état lisible d'un coup d'œil dans la grille de têtes —
    // le lore ne se lit qu'au survol, un host qui scanne 40 joueurs ne survole pas 40 têtes.
    private static final String PLAYER_NAME = "§8┃ §f%team_tag%%pseudo% %status_dot%";
    private static final String PLAYER_LORE = "§7» Élément §fdynamique\n\n §f┃ §fMicro : %mute_status%\n §f┃ §fÉcoute : %deaf_status%\n §f┃ §fMod vocal : %link_status%\n\n§8» §fCliquez pour §fbasculer son micro§f.";

    private final List<LiveUser> users;
    private final String menuId;
    private int pages = 1;

    private MumblePlayersUi(Player player, List<LiveUser> users) {
        super(player);
        this.users = users;
        this.menuId = users.isEmpty() ? "vocal-moderation-vide" : "vocal-moderation";
    }

    public static void open(Player player) {
        // Garde ici et pas seulement dans MumbleUi : tout futur appelant (commande, item,
        // autre menu) passe forcément par cette porte.
        if (!MumbleUi.isHost(player)) {
            LangManager.get().send(CoreLang.CMD_NO_PERMISSION, player);
            return;
        }
        if (!MumbleManager.get().hasSession()) {
            player.sendMessage(LangManager.get().get(MumbleUiLang.NO_SESSION, player));
            return;
        }
        // Le sondage d'état entretient déjà un cache de quelques secondes : on dessine
        // immédiatement plutôt que d'imposer un aller-retour HTTP + Ice à chaque ouverture.
        List<LiveUser> cached = MumbleManager.get().getCachedLiveUsers();
        if (!cached.isEmpty()) {
            new MumblePlayersUi(player, cached).open();
            return;
        }
        reload(player);
    }

    /** Relit l'état réel puis (ré)ouvre. Utilisé au premier affichage et par « Rafraîchir ». */
    private static void reload(Player player) {
        MumbleManager.get().fetchLiveUsers()
                .thenAccept(list -> Bukkit.getScheduler().runTask(Main.get(), () -> {
                    MumbleManager.get().patchCache(list);
                    new MumblePlayersUi(player, list).open();
                }))
                .exceptionally(ex -> {
                    Bukkit.getScheduler().runTask(Main.get(),
                            () -> player.sendMessage(LangManager.get().get(MumbleUiLang.ERROR, player)));
                    return null;
                });
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + menuId + ".title", "» Vocal — Modération"));
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
    public String broadcastKey() {
        return getClass().getName() + ":" + menuId;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.LIGHT_BLUE, 0, 8, 45, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);
        if (users.isEmpty()) {
            pages = 1;
            addItem(new StaticItem(22, new ItemCreator(Material.BARRIER)
                    .setName(name("no-players", "§8┃ §6Aucun joueur connecté"))
                    .setLores(lore("no-players", "§7» Accès §6Host\n\n §6┃ §fLe salon vocal est ouvert mais\n §6┃ §fpersonne ne l’a encore rejoint.\n"))));
        } else {
            pages = paginate(users, PER_PAGE,
                    n -> MenuGrid.linear(FILL_FROM, FILL_TO, n),
                    (user, slot) -> addItem(new ActionItem(slot, head(user)) {
                        @Override
                        public void onClick(InventoryClickEvent e) {
                            moderate(user.mute() ? "unmuteUser" : "muteUser", user.name(), null, null);
                        }
                    }));
        }

        button(45, Material.REDSTONE_BLOCK, "mute-all", "§8┃ §cTout mute",
                "§7» Accès §cHost\n\n §c┃ §fCoupez le micro de tous les joueurs\n §c┃ §fsauf le vôtre.\n\n§8» §fCliquez pour §cutiliser§f.",
                e -> moderate("muteAll", null, null, List.of(getPlayer().getName())));

        button(46, Material.EMERALD_BLOCK, "unmute-all", "§8┃ §aTout unmute",
                "§7» Accès §aHost\n\n §a┃ §fRendez le micro à tous les joueurs.\n\n§8» §fCliquez pour §autiliser§f.",
                e -> moderate("unmuteAll", null, null, null));

        button(47, Material.COAL_BLOCK, "deafen-all", "§8┃ §5Tout deafen",
                "§7» Accès §5Host\n\n §5┃ §fCoupez l’écoute de tous les joueurs\n §5┃ §fsauf la vôtre.\n\n§8» §fCliquez pour §5utiliser§f.",
                e -> moderate("deafenAll", null, null, List.of(getPlayer().getName())));

        button(48, Material.LAPIS_BLOCK, "undeafen-all", "§8┃ §9Tout undeafen",
                "§7» Accès §9Host\n\n §9┃ §fRendez l’écoute à tous les joueurs.\n\n§8» §fCliquez pour §9utiliser§f.",
                e -> moderate("undeafenAll", null, null, null));

        button(49, Material.COMPASS, "move-lobby", "§8┃ §bDéplacer au lobby",
                "§7» Accès §bHost\n\n §b┃ §fRenvoyez tout le monde dans le salon\n §b┃ §fvocal du lobby.\n\n§8» §fCliquez pour §butiliser§f.",
                e -> moderate("moveAll", null, "lobby", null));

        // Rattrape ceux restés au Lobby : non liés laissés derrière à la bascule, ou joueur
        // ayant relancé son client Mumble en cours de partie — ils ne peuvent pas entrer seuls.
        button(50, Material.BEACON, "move-game", "§8┃ §aDéplacer en partie",
                "§7» Accès §aHost\n\n §a┃ §fAmenez tout le monde dans le salon\n §a┃ §fvocal de la partie.\n\n§8» §fCliquez pour §autiliser§f.",
                e -> moderate("moveAll", null, "game", null));

        button(52, Material.NETHER_STAR, "refresh", "§8┃ §eRafraîchir",
                "§7» Accès §eHost\n\n §e┃ §fRechargez immédiatement l’état\n §e┃ §fdes joueurs connectés au vocal.\n\n§8» §fCliquez pour §eutiliser§f.",
                e -> open(getPlayer()));

        button(53, Material.WOOD_DOOR, "close", "§8┃ §7Fermer",
                "§7» Accès §7Host\n\n §7┃ §fFermez la modération du salon vocal.\n\n§8» §fCliquez pour §7utiliser§f.",
                e -> getPlayer().closeInventory());
    }

    private void button(int slot, Material material, String key, String frName, String frLore,
                        Consumer<InventoryClickEvent> handler) {
        ItemCreator icon = new ItemCreator(material)
                .setName(name(key, frName))
                .setLores(lore(key, frLore));
        addItem(new ActionItem(slot, icon) {
            @Override
            public void onClick(InventoryClickEvent e) {
                handler.accept(e);
            }
        });
    }

    private ItemCreator head(LiveUser user) {
        Map<String, String> vars = Map.of(
                "%pseudo%", user.name(),
                "%team_tag%", teamTag(user.name()),
                "%status_dot%", t(user.linked() ? MumbleUiLang.DOT_LINKED : MumbleUiLang.DOT_UNLINKED),
                "%mute_status%", t(user.mute() ? MumbleUiLang.STATE_MUTED : MumbleUiLang.STATE_OK),
                "%deaf_status%", t(user.deaf() ? MumbleUiLang.STATE_DEAF : MumbleUiLang.STATE_OK),
                "%link_status%", t(user.linked() ? MumbleUiLang.STATE_LINKED : MumbleUiLang.STATE_UNLINKED));

        ItemCreator icon = new ItemCreator(Material.SKULL_ITEM)
                .setDurability((short) 3)
                .setOwner(user.name())
                .setName(apply(name("player-dummy", PLAYER_NAME), vars));

        List<String> out = new ArrayList<>();
        for (String line : lore("player-dummy", PLAYER_LORE)) out.add(apply(line, vars));
        return icon.setLores(out);
    }

    /**
     * Préfixe d'équipe, si l'option host est active. Purement local : Murmur n'a pas de nom
     * d'affichage modifiable et le pseudo y est figé à la connexion — le tag n'apparaît donc
     * que dans ce menu, pas dans le client Mumble.
     * On passe par UHCPlayer.getTeam() joueur par joueur : UHCTeam.getPlayers() n'inclut ni les
     * morts ni les déconnectés, qui sont justement ceux qu'on veut voir ici.
     */
    private String teamTag(String playerName) {
        if (!UHCManager.get().isMumbleShowTeamTags()) return "";
        Player bukkit = Bukkit.getPlayerExact(playerName);
        if (bukkit == null) return "";
        UHCPlayer uhc = UHCPlayerManager.get().getPlayer(bukkit);
        if (uhc == null) return "";
        return uhc.getTeam().map(team -> team.prefix() + " ").orElse("");
    }

    private String name(String key, String fr) {
        return t(DynamicLang.of("menu." + menuId + "." + key + ".name", fr));
    }

    private List<String> lore(String key, String fr) {
        return Arrays.asList(t(DynamicLang.of("menu." + menuId + "." + key + ".lore", fr)).split("\n", -1));
    }

    private static String apply(String text, Map<String, String> vars) {
        if (text == null) return null;
        String out = text;
        for (Map.Entry<String, String> var : vars.entrySet()) out = out.replace(var.getKey(), var.getValue());
        return out;
    }

    /**
     * Applique une action et redessine TOUT DE SUITE l'effet attendu, sans attendre la réponse.
     *
     * Un clic coûtait deux allers-retours en série — l'action, puis une relecture complète pour
     * redessiner — soit le double de la latence réseau avant que le menu ne bouge. On prédit
     * localement (les actions de modération sont déterministes), et on ne relit que si l'appel
     * a échoué, cas où l'affichage optimiste mentirait.
     */
    private void moderate(String action, String playerName, String channel, List<String> except) {
        List<LiveUser> predicted = predict(action, playerName, except);
        MumbleManager.get().patchCache(predicted);
        new MumblePlayersUi(getPlayer(), predicted).open();

        MumbleManager.get().moderate(action, playerName, channel, except)
                .exceptionally(ex -> {
                    Bukkit.getScheduler().runTask(Main.get(), () -> reload(getPlayer()));
                    return null;
                });
    }

    /** Effet attendu d'une action sur la liste affichée. `linked` n'est jamais touché. */
    private List<LiveUser> predict(String action, String playerName, List<String> except) {
        Set<String> spared = new HashSet<>();
        if (except != null) for (String n : except) spared.add(n.toLowerCase());

        List<LiveUser> out = new ArrayList<>(users.size());
        for (LiveUser u : users) {
            boolean target = playerName != null
                    ? u.name().equalsIgnoreCase(playerName)
                    : !spared.contains(u.name().toLowerCase());
            if (!target) {
                out.add(u);
                continue;
            }
            switch (action) {
                case "muteUser", "muteAll" -> out.add(new LiveUser(u.name(), true, u.deaf(), u.linked(), u.channel()));
                case "unmuteUser", "unmuteAll" -> out.add(new LiveUser(u.name(), false, u.deaf(), u.linked(), u.channel()));
                // deafen implique mute côté Mumble : le refléter évite un menu incohérent
                // pendant les quelques secondes avant le prochain relevé.
                case "deafenAll" -> out.add(new LiveUser(u.name(), true, true, u.linked(), u.channel()));
                case "undeafenAll" -> out.add(new LiveUser(u.name(), u.mute(), false, u.linked(), u.channel()));
                default -> out.add(u);
            }
        }
        return out;
    }
}
