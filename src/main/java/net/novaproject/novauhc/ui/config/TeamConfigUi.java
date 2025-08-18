package net.novaproject.novauhc.ui.config;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.ui.DefaultUi;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import net.novaproject.novauhc.utils.ui.item.StaticItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Optional;

public class TeamConfigUi extends CustomInventory {

    public TeamConfigUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillCadre(0);
        addReturn(18, new DefaultUi(getPlayer()));

        ItemCreator updateTeam = new ItemCreator(Material.BOOK).setName(ChatColor.GOLD + "Update Team");
        ItemCreator teamSize = new ItemCreator(Material.PAPER).setName(ChatColor.GRAY + "Nombre de joueur par equipe : " + formatValue(UHCManager.get().getTeam_size()));
        ItemCreator desac = new ItemCreator(Material.BARRIER).setName(ChatColor.RED + "Désactiver les équipe.");
        ItemCreator to2 = new ItemCreator(Material.BANNER).setName(ChatColor.DARK_PURPLE + "Equipe de 2.").setBasecolor(DyeColor.MAGENTA);
        ItemCreator to3 = new ItemCreator(Material.BANNER).setName(ChatColor.DARK_GREEN + "Equipe de 3.").setBasecolor(DyeColor.GREEN);
        ItemCreator to4 = new ItemCreator(Material.BANNER).setName(ChatColor.GREEN + "Equipe de 4.").setBasecolor(DyeColor.LIME);
        ItemCreator to5 = new ItemCreator(Material.BANNER).setName(ChatColor.AQUA + "Equipe de 5.").setBasecolor(DyeColor.LIGHT_BLUE);
        ItemCreator to6 = new ItemCreator(Material.BANNER).setName(ChatColor.BLUE + "Equipe de 6.").setBasecolor(DyeColor.BLUE);
        ItemCreator to7 = new ItemCreator(Material.BANNER).setName(ChatColor.LIGHT_PURPLE + "Equipe de 7.").setBasecolor(DyeColor.CYAN);

        addItem(new StaticItem(4, teamSize));
        addItem(new ActionItem(5, desac) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(1);
                openAll();
                Bukkit.broadcastMessage(CommonString.TEAM_DESACTIVATED.getMessage(getPlayer()));
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });

        addItem(new ActionItem(10, to2
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic pour activer " + ChatColor.GREEN + "TO2")
                .addLore("")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(2);
                openAll();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.YELLOW + "Les teams de 2 sont maintenant activées");
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
        addItem(new ActionItem(11, to3
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic pour activer " + ChatColor.GREEN + "TO3")
                .addLore("")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(3);
                openAll();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.YELLOW + "Les teams de 3 sont maintenant activées");
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
        addItem(new ActionItem(12, to4
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic pour activer " + ChatColor.GREEN + "TO4")
                .addLore("")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(4);
                openAll();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.YELLOW + "Les teams de 4 sont maintenant activées");
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
        addItem(new ActionItem(14, to5
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic pour activer " + ChatColor.GREEN + "TO5")
                .addLore("")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(5);
                openAll();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.YELLOW + "Les teams de 5 sont maintenant activées");
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
        addItem(new ActionItem(15, to6
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic pour activer " + ChatColor.GREEN + "TO6")
                .addLore("")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(6);
                openAll();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.YELLOW + "Les teams de 6 sont maintenant activées");
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
        addItem(new ActionItem(16, to7
                .addLore("")
                .addLore(ChatColor.YELLOW + "► Clic pour activer " + ChatColor.GREEN + "TO7")
                .addLore("")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(7);
                openAll();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.YELLOW + "Les teams de 7 sont maintenant activées");
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
        addItem(new ActionItem(3, updateTeam) {
            @Override
            public void onClick(InventoryClickEvent e) {
                for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                    player.setTeam(Optional.empty());
                }
                UHCTeamManager.get().createTeams();
                openAll();
                Bukkit.broadcastMessage(Common.get().getInfoTag() + ChatColor.YELLOW + "Mise a jour des équipes");
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
    }

    @Override
    public String getTitle() {
        return "§6Menu de configuration : Team";
    }

    @Override
    public int getLines() {
        return 3;
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    private String formatValue(int value) {
        if (value == 1) {
            return ChatColor.RED + "Désactivé";
        }
        return ChatColor.GREEN + String.valueOf(value);
    }

}
