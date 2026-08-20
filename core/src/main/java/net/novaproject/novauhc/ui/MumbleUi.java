package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.mumble.MumbleManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.ui.item.StaticItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;

public class MumbleUi extends CustomInventory {

    private final String menuId;

    public MumbleUi(Player player) {
        super(player);
        this.menuId = menuId();
    }

    private static String menuId() {
        MumbleManager mm = MumbleManager.get();
        return mm.isEnabled() && mm.hasSession() ? "mumble" : "mumble-ferme";
    }

    /**
     * Qui pilote le vocal. DÉFINITION UNIQUE — MumblePlayersUi s'y réfère aussi.
     *
     * On teste la PERMISSION (via RankManager.isStaffOrOp = novauhc.host / novauhc.cohost,
     * ce que fait déjà Command.isHost pour toutes les commandes host) et non le Rank affiché :
     * `RankManager.getRank()` ne peut renvoyer HOST/COHOST que dans sa branche `!inGame`
     * (RankManager:41), donc dès le lancement de la partie tout le monde retombait en
     * PLAYER/SPECTATOR et le bouton de modération disparaissait — y compris pour le vrai host,
     * au moment précis où il en a besoin.
     */
    public static boolean isHost(Player p) {
        return RankManager.isStaffOrOp(p);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu." + menuId + ".title", "» Mumble"));
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
        return getClass().getName() + ":" + menuId;
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.PURPLE, 0, 8, 18, 26);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

        if ("mumble".equals(menuId)) {
            addItem(new ActionItem(11, icon(Material.JUKEBOX, "join", "§8┃ §aRejoindre le vocal",
                    "§7» Accès §aHost\n"
                            + "\n"
                            + " §a┃ §fRecevez le lien de connexion du salon\n"
                            + " §a┃ §fvocal de la partie en cours.\n"
                            + "\n"
                            + "§8» §fCliquez pour §autiliser§f.")) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    getPlayer().closeInventory();
                    MumbleManager.get().sendJoinLink(getPlayer());
                }
            });

            if (isHost(getPlayer())) {
                addItem(new ActionItem(15, icon(Material.COMMAND, "manage", "§8┃ §bGérer le salon",
                        "§7» Accès §bHost / Co-Host\n"
                                + "\n"
                                + " §b┃ §fOuvrez la modération vocale du salon.\n"
                                + " §b┃ §fRéservée au Host et aux co-hosts.\n"
                                + "\n"
                                + "§8» §fCliquez pour y §baccéder§f.")) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        MumblePlayersUi.open(getPlayer());
                    }
                });
            }
        } else {
            addItem(new StaticItem(13, icon(Material.BARRIER, "closed", "§8┃ §cSession fermée",
                    "§7» Accès §cHost\n"
                            + "\n"
                            + " §c┃ §fAucun salon vocal n’est ouvert pour\n"
                            + " §c┃ §fcette partie pour le moment.\n")));
        }

        addItem(new ActionItem(22, icon(Material.WOOD_DOOR, "close", "§8┃ §7Fermer",
                "§7» Accès §7Host\n"
                        + "\n"
                        + " §7┃ §fFermez le menu vocal.\n"
                        + "\n"
                        + "§8» §fCliquez pour §7utiliser§f.")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().closeInventory();
            }
        });
    }

    private ItemCreator icon(Material material, String tile, String name, String lore) {
        return new ItemCreator(material)
                .setName(t(DynamicLang.of("menu." + menuId + "." + tile + ".name", name)))
                .setLores(Arrays.asList(t(DynamicLang.of("menu." + menuId + "." + tile + ".lore", lore))
                        .split("\n", -1)));
    }
}
