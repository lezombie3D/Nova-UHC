package net.novaproject.novauhc.ui.player;


import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.listener.player.PlayerConnectionEvent;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import net.novaproject.novauhc.utils.ui.CustomInventory;
import net.novaproject.novauhc.utils.ui.item.ActionItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Optional;

public class inGameTeamUi extends CustomInventory {

    public inGameTeamUi(Player player) {
        super(player);
    }

    @Override
    public void setup() {
        fillLine(1, getConfig().getInt("menu.teams.ingame.color"));
        addClose(8);


        addItem(new ActionItem(0, new ItemCreator(Material.PAPER).setName(ChatColor.WHITE + "Equipes aléatoire")) {
            @Override
            public void onClick(InventoryClickEvent e) {
                getUHCPlayer().setTeam(Optional.empty());
                if (getPlayer() == PlayerConnectionEvent.getHost()) {
                    TeamsTagsManager.setNameTag(getPlayer(), "HOST", "§f[§5Host§f] ", "");
                }
                openAll();
            }
        });

        int totalTeams = UHCTeamManager.get().getTeams().size();
        int teamsPerPage = 45;
        int totalCategories = (int) Math.ceil((double) totalTeams / teamsPerPage);

        if (totalCategories > 1) {
            addPage(4);
        }

        int currentTeam = 0;

        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            currentTeam++;
            int categoryForThisItem = (int) Math.ceil((double) currentTeam / teamsPerPage);

            int positionInCategory = (currentTeam - 1) % teamsPerPage;

            int slot = 9 + positionInCategory;

            addItem(new ActionItem(categoryForThisItem, slot, team.getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    getUHCPlayer().setTeam(Optional.of(team));
                    openAll();
                }
            });
        }
    }


    @Override
    public int getCategories() {
        int totalTeams = UHCTeamManager.get().getTeams().size();
        int teamsPerPage = 45;
        return (int) Math.ceil((double) totalTeams / teamsPerPage);
    }

    @Override
    public String getTitle() {
        return getConfig().getString("menu.teams.ingame.title");
    }

    @Override
    public int getLines() {
        return Math.min((UHCTeamManager.get().getTeams().size() / 9) + 2, 6);
    }

    @Override
    public boolean isRefreshAuto() {
        return false;
    }

    @Override
    public void open() {
        if (ScenarioManager.get().getScenarioByName("SkyDef").map(Scenario::isActive).orElse(false)) {
            super.open();
            return;
        } else if (ScenarioManager.get().getScenarioByName("BeatTheSanta").map(Scenario::isActive).orElse(false)) {
            super.open();
            return;
        } else if (UHCManager.get().getTeam_size() == 1) {
            CommonString.DISABLE_ACTION.send(getPlayer());
            return;
        }
        super.open();

    }

}