package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.UiLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.ui.AnvilUi;
import net.novaproject.novauhc.ui.CustomInventory;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.ui.MenuHeads;
import net.novaproject.novauhc.ui.MenuHeads.Shape;
import net.novaproject.novauhc.ui.UiItems;
import net.novaproject.novauhc.ui.item.ActionItem;
import net.novaproject.novauhc.utils.item.ItemCreator;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Arrays;
import java.util.Optional;

public class TeamConfigUi extends CustomInventory {

    private static final String TEAM_2_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTAyZWJhODNhNmE5NTM5MTdmMWQ5OTFiYjM3OWIzNmU3ZmYwNGE0OTZkMTdmNTQyNGQwNGFhMTJlZmJhYiJ9fX0=";
    private static final String TEAM_3_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWY4ZTdhY2Y3MjQyYWZhNTg2YzNkMDk1Zjg3ZmU5ZGU3YjdjYTI0YzRhMjhhNTYwNDAzNDdjNjU3OTYwZTM2In19fQ==";
    private static final String TEAM_4_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWYyZDI2MDFmOWMxYWNlZmZjYzgyMzY1N2UyMjU5ZDM1OGJiNTJiZDQ2MzEyYmYxYzc2ZWUyM2QzYjE3In19fQ==";
    private static final String TEAM_5_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ZmODdlZGI5ODJkNzM3MmFjZGU5MmM0NTI0YTIyNDU1NzFmNWFhMWFlOTQwZDgxN2JmZmJjMmEyODgifX19";
    private static final String TEAM_6_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODNlOGEyYzhjM2Y1ZWE0YWUxNTk5ZTBjMTcwNTJjNTQ0M2U3MzZhMjMzYjRlZTNjOTY0MWYzYjljYzlkIn19fQ==";
    private static final String TEAM_7_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODkxNDE5NTJhYWJjYTQ5ODdiZGRmMjA4YzVlMTVjYTZhNWJmY2UyMzlhZjM5NTg0YWViNWVlODc2ZDg3In19fQ==";
    private static final String TEAM_8_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZiNTZjYjBiNDhkOWM5ZWRkMTk4NGFmZTE3MTNiOThlZWE5NmQyOWJlNWIyYjU4ZGE0NDQ0YTEwMThlOTVkIn19fQ==";
    private static final String DISABLE_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQyNDU0ZTRjNjdiMzIzZDViZTk1M2I1YjNkNTQxNzRhYTI3MTQ2MDM3NGVlMjg0MTBjNWFlYWUyYzExZjUifX19";
    private static final String CUSTOM_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGM5MjY5NDQ4YWZmZmY0NDQ1MTc3NTJmNzI1YWJiNDliOTFlZjM4MGU5YmQ3M2Y5YmY5ZDgzNzA0ZWYzZDZiNSJ9fX0=";

    public TeamConfigUi(Player player) {
        super(player);
    }

    @Override
    public String getTitle() {
        return t(DynamicLang.of("menu.teams.title", "» Teams✓"));
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
        UiItems.panes(this, DyeColor.ORANGE, 0, 8, 26);
        UiItems.panes(this, DyeColor.WHITE, 1, 7, 9, 17, 19, 25);

        String mode = t(UHCTeamManager.get().isRandomMode()
                ? UiLang.TEAMCONFIG_MODE_RANDOM : UiLang.TEAMCONFIG_MODE_CHOSEN);

        ItemCreator composition = new ItemCreator(Material.NAME_TAG)
                .setName(t(DynamicLang.of("menu.teams.btn-2.name", "§8┃ §2Mode de composition"))
                        .replace("%mode_aleatoire%", mode))
                .setGlow(UHCTeamManager.get().isRandomMode());
        composition.setLores(Arrays.asList(t(DynamicLang.of("menu.teams.btn-2.lore",
                "§7» Accès §2Host\n"
                        + "\n"
                        + " §2┃ §fChoisies : les joueurs se rangent librement.\n"
                        + " §2┃ §fAléatoires : équipes tirées au sort au lancement.\n"
                        + "\n"
                        + " §2☰ §fStatus : %mode_aleatoire%§f%\n"
                        + "        \n"
                        + "§8» §fCliquez pour §2§lmodifier§f."))
                .replace("%mode_aleatoire%", mode).split("\n", -1)));

        addItem(new ActionItem(2, composition) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCTeamManager.get().setSelectionMode(UHCTeamManager.get().getSelectionMode().next());
                LangManager.get().sendAll(UHCTeamManager.get().isRandomMode()
                        ? CoreLang.COMMON_TEAM_MODE_RANDOM_SET : CoreLang.COMMON_TEAM_MODE_CHOSEN_SET);
                openAll();
            }
        });

        addItem(new ActionItem(4, UiItems.createCustomButon(DISABLE_TEXTURE,
                t(DynamicLang.of("menu.teams.btn-4.name", "§8┃ §c§lDesactivé les Equipes")),
                Arrays.asList(t(DynamicLang.of("menu.teams.btn-4.lore",
                        "§7» Accès §cHost\n"
                                + "\n"
                                + " §c┃ §fPermet de §cdésactivé §fles §céquipes\n"
                                + " §c┃ §fLes jouer se retrouvent donc §cSolo§f.\n"
                                + "\n"
                                + "§8» §fClique pour §cl'activer")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(1);
                openAll();
                Bukkit.broadcastMessage(LangManager.get().get(CoreLang.COMMON_TEAM_DESACTIVATED, getPlayer()));
                notifyTeamUpdate();
            }
        });

        ItemCreator dissolve = new ItemCreator(Material.REDSTONE_TORCH_ON)
                .setName(t(DynamicLang.of("menu.teams.btn-6.name", "§8┃ §3Dissoudre les Equipes")));
        dissolve.setLores(Arrays.asList(t(DynamicLang.of("menu.teams.btn-6.lore",
                "§7» Accès §3Host\n"
                        + "\n"
                        + " §3┃ §fPermet d'enlevez tous les §3Joueurs\n"
                        + " §3┃ §fdes leur §3Equipe §fsans changer leurs tailles.\n"
                        + "\n"
                        + "§8» §fClique pour §3l'activer")).split("\n", -1)));

        addItem(new ActionItem(6, dissolve) {
            @Override
            public void onClick(InventoryClickEvent e) {
                for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    player.setTeam(Optional.empty());
                }
                UHCTeamManager.get().createTeams();
                openAll();
                Bukkit.broadcastMessage(t(UiLang.TEAMCONFIG_UPDATE_BROADCAST));
                notifyTeamUpdate();
            }
        });

        teamSize(10, 2, TEAM_2_TEXTURE, "§b", "§b", "Acces");
        teamSize(11, 3, TEAM_3_TEXTURE, "§6", "§6", "Accès");
        teamSize(12, 4, TEAM_4_TEXTURE, "§e", "§e", "Accès");
        teamSize(13, 5, TEAM_5_TEXTURE, "§d", "§d", "Accès");
        teamSize(14, 6, TEAM_6_TEXTURE, "§7", "§7§l", "Accès");
        teamSize(15, 7, TEAM_7_TEXTURE, "§5", "§5", "Accès");
        teamSize(16, 8, TEAM_8_TEXTURE, "§9", "§9", "Accès");

        addItem(new ActionItem(18, UiItems.createCustomButon(MenuHeads.of(Shape.BACK, DyeColor.ORANGE),
                t(DynamicLang.of("menu.teams.btn-18.name", "§8┃ §7§lRetour")), null)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new DefaultUi(getPlayer()).open();
            }
        });

        addItem(new ActionItem(22, UiItems.createCustomButon(CUSTOM_TEXTURE,
                t(DynamicLang.of("menu.teams.btn-22.name", "§8┃ §a§lTaille d'Equipe Custom")),
                Arrays.asList(t(DynamicLang.of("menu.teams.btn-22.lore",
                        "§7» Accès §aHost\n"
                                + "\n"
                                + " §a┃ §fPermet de définir une taille X d'équipe.\n"
                                + "\n"
                                + "§8» §fClique pour §al'activer")).split("\n", -1)))) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() != AnvilUi.AnvilSlot.OUTPUT) return;
                    int entered;
                    try {
                        entered = Integer.parseInt(event.getName());
                    } catch (NumberFormatException ex) {
                        return;
                    }
                    UHCManager.get().setTeam_size(Math.max(1, entered));
                    LangManager.get().send(CoreLang.COMMON_SUCCESSFUL_MODIFICATION, getPlayer());
                    LangManager.get().sendAll(CoreLang.COMMON_TEAM_UPDATED);
                    notifyTeamUpdate();
                    openAll();
                }).setSlot(t(UiLang.TEAMCONFIG_CUSTOM_ANVIL_HINT)).open();
            }
        });
    }

    private void teamSize(int slot, int size, String texture, String color, String accent, String access) {
        String key = "menu.teams.btn-" + slot;

        String name = t(DynamicLang.of(key + ".name", "§8┃ " + color + "Equipes de " + size));

        String lore = t(DynamicLang.of(key + ".lore",
                "§7» " + access + " " + accent + "Host\n"
                        + "\n"
                        + " " + color + "┃ §fDefinie la taille des " + accent + "Equipes à " + size + "\n"
                        + "\n"
                        + "§8» §fClique pour " + accent + "l'activer"));

        addItem(new ActionItem(slot, UiItems.createCustomButon(texture, name,
                Arrays.asList(lore.split("\n", -1)))
                .setGlow(UHCManager.get().getTeam_size() == size)) {
            @Override
            public void onClick(InventoryClickEvent e) {
                applyTeamSize(size);
            }
        });
    }

    private void applyTeamSize(int teamSize) {
        UHCManager.get().setTeam_size(teamSize);
        openAll();
        LangManager.get().sendAll(CoreLang.COMMON_TEAM_UPDATED);
        notifyTeamUpdate();
    }

    private void notifyTeamUpdate() {
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> scenario.onTeamUpdate());
    }
}
