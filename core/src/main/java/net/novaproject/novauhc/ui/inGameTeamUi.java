package net.novaproject.novauhc.ui;

import net.novaproject.novauhc.lobby.RankManager;
import net.novaproject.novauhc.lobby.RankManager.Rank;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.listener.PlayerConnectionEvent;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class inGameTeamUi extends CustomInventory {

    private static final int PER_PAGE = 28;

    private int pages = 1;

    public inGameTeamUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.selection-d-equipe.title", "» Sélection d’Équipe"));
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
        return getClass().getName() + ":selection-d-equipe";
    }

    @Override
    public void setup() {
        UiItems.panes(this, DyeColor.ORANGE, 0, 8, 45, 53);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 36, 44, 46, 52);

        List<UHCTeam> teams = UHCTeamManager.get().getTeams();

        addItem(new ActionItem(2, new ItemCreator(Material.NETHER_STAR)
                .setName(t(DynamicLang.of("menu.selection-d-equipe.random.name", "§8┃ §aÉquipe aléatoire")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.selection-d-equipe.random.lore",
                        "§7» Accès §aHost\n\n §a┃ §fLaissez le plugin vous placer dans\n §a┃ §fune équipe encore disponible.\n\n§8» §fCliquez pour §autiliser§f."))
                        .split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getUHCPlayer().setTeam(Optional.empty());
                if (getPlayer() == PlayerConnectionEvent.getHost().getPlayer()) {
                    RankManager.get().applyTag(getPlayer(), Rank.HOST);
                }
                openAll();
            }
        });

        addItem(new ActionItem(6, new ItemCreator(Material.WOOD_DOOR)
                .setName(t(DynamicLang.of("menu.selection-d-equipe.close.name", "§8┃ §7Fermer")))
                .setLores(Arrays.asList(t(DynamicLang.of("menu.selection-d-equipe.close.lore",
                        "§7» Accès §7Host\n\n §7┃ §fFermez la sélection d’équipe.\n\n§8» §fCliquez pour §7utiliser§f."))
                        .split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getPlayer().closeInventory();
            }
        });

        pages = paginate(teams, PER_PAGE,
                n -> MenuGrid.grid(1, 4, 1, 7, false, n),
                (team, slot) -> addItem(new ActionItem(slot, teamIcon(team)) {
                    @Override
                    public void onClick(InventoryClickEvent e) {
                        getUHCPlayer().setTeam(Optional.of(team));
                        openAll();
                    }
                }));

        if (teams.size() > PER_PAGE) {
            addItem(new ActionItem(47, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.LEFT, DyeColor.ORANGE),
                    t(DynamicLang.of("menu.selection-d-equipe.previous.name", "§8┃ §cPrécédent")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    previousCategory();
                    open();
                }
            });
            addItem(new ActionItem(51, UiItems.createCustomButon(MenuHeads.of(MenuHeads.Shape.RIGHT, DyeColor.ORANGE),
                    t(DynamicLang.of("menu.selection-d-equipe.next.name", "§8┃ §aSuivant")), null)) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    nextCategory();
                    open();
                }
            });
        }
    }

    private ItemCreator teamIcon(UHCTeam team) {
        ItemCreator icon = new ItemCreator(Material.BANNER)
                .setLores(Arrays.asList(t(DynamicLang.of("menu.selection-d-equipe.team-dummy.lore",
                        "§7» Élément §fdynamique\n\n §f┃ §fUne entrée par équipe, avec sa bannière\n §f┃ §fet sa couleur réelles.\n\n§8» §fCliquez pour §fla rejoindre§f."))
                        .split("\n", -1)));

        BannerMeta meta = icon.getBannerMeta();
        if (meta != null) {
            List<Pattern> patterns = new ArrayList<>();
            for (Pattern pattern : team.patterns()) {
                patterns.add(new Pattern(pattern.getColor() == DyeColor.BLACK ? team.dyeColor() : pattern.getColor(),
                        pattern.getPattern()));
            }
            meta.setBaseColor(team.dyeColor());
            meta.setPatterns(patterns);
            icon.setBannerMeta(meta);
        }
        icon.setDurability((short) team.dyeColor().getDyeData());
        icon.setName("§8┃ " + team.prefix());

        List<UHCPlayer> players = team.getPlayers();
        icon.addLore("");
        if (team.teamSize() <= 8) {
            for (int i = 0; i < team.teamSize(); i++) {
                icon.addLore("§b➤ " + (players.size() < i + 1 ? "" : players.get(i).getPlayer().getName()));
            }
        } else {
            icon.addLore(LangManager.get().get(CoreLang.COMMON_TEAM_PLAYER_COUNT,
                    Map.of("%count%", players.size(), "%max%", team.teamSize())));
        }
        return icon;
    }

    @Override
    public void open() {
        if (UHCTeamManager.get().isRandomMode() && !RankManager.isStaff(getPlayer())) {
            LangManager.get().send(CoreLang.COMMON_TEAM_RANDOM_MODE, getPlayer());
            return;
        }
        if (ScenarioManager.get().getActiveScenarios().stream().anyMatch(s -> !s.canOpenInGameTeamUi())) {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, getPlayer());
            return;
        }
        if (UHCTeamManager.get().getTeams().isEmpty()) {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, getPlayer());
            return;
        }
        super.open();
    }
}
