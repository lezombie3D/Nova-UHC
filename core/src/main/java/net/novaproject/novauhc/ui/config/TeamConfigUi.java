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
import net.novaproject.novauhc.utils.UHCUtils;
import net.novaproject.novauhc.utils.ui.AnvilUi;
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

        ItemCreator updateTeam = new ItemCreator(Material.BOOK).setName("§8┃ §fMettre a jour les équipes")
                .addLore("")
                .addLore("  §8┃ §fMet a jours "+Common.get().getMainColor()+"toutes")
                .addLore("  §8┃ §fles équipes en fonction de la")
                .addLore("  §8┃ §fnouvelle taille définie.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");

        ItemCreator teamSize = new ItemCreator(Material.PAPER).setName("§8┃ §fTaille des équipes actuel : §e§l"+ UHCUtils.formatValue(UHCManager.get().getTeam_size(),1));
        ItemCreator custom = new ItemCreator(Material.REDSTONE_TORCH_ON).setName("§8┃ §fTaille des equipe §6§lCustom")
                .addLore("")
                .addLore("  §8┃ §fPerment de modifier le "+Common.get().getMainColor()+"nombre")
                .addLore("  §8┃ §fde §6§ljoueur §fdans les equipes")
                .addLore("  §8┃ §fcomme vous le §c§lsouhaitez.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");

        ItemCreator desac = new ItemCreator(Material.BARRIER).setName("§8┃ §fDésactiver les équipes")
                .addLore("")
                .addLore("  §8┃ §fPermet de desactiver les "+Common.get().getMainColor()+"equipes.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");

        addItem(new StaticItem(4, teamSize));
        addItem(new ActionItem(22, custom) {
            @Override
            public void onClick(InventoryClickEvent e) {
                new AnvilUi(getPlayer(), event -> {
                    if (event.getSlot() == AnvilUi.AnvilSlot.OUTPUT) {
                        int enteredText = Integer.parseInt(event.getName());
                        enteredText = Math.max(1, enteredText);
                        UHCManager.get().setTeam_size(enteredText);
                        CommonString.SUCCESSFUL_MODIFICATION.send(getPlayer());
                        CommonString.TEAM_UPDATED.sendAll();
                        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                            scenario.onTeamUpdate();
                        });

                        openAll();
                    }

                }).setSlot("Nombre de joueur").open();
            }
        });
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
        addTeamItem(10,2,DyeColor.LIGHT_BLUE);
        addTeamItem(11,3,DyeColor.GREEN);
        addTeamItem(12,4,DyeColor.RED);
        addTeamItem(13,5,DyeColor.YELLOW);
        addTeamItem(14,6,DyeColor.PURPLE);
        addTeamItem(15,7,DyeColor.ORANGE);
        addTeamItem(16,8,DyeColor.WHITE);
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


    public void addTeamItem(int slot, int team_size,DyeColor color){
        ItemCreator item = new ItemCreator(Material.BANNER).setName("§8┃ §fJoueur par equipe : §e§l"+team_size)
                .setBasecolor(color).addallItemsflags()
                .addLore("")
                .addLore("  §8┃ §fPerment de modifier le "+Common.get().getMainColor()+"nombre")
                .addLore("  §8┃ §fde §6§ljoueur §fdans les equipes.")
                .addLore("")
                .addLore(CommonString.CLICK_HERE_TO_MODIFY.getMessage())
                .addLore("");

        addItem(new ActionItem(slot,item) {
            @Override
            public void onClick(InventoryClickEvent e) {
                UHCManager.get().setTeam_size(team_size);
                openAll();
                CommonString.TEAM_UPDATED.sendAll();
                ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                    scenario.onTeamUpdate();
                });
            }
        });
    }


}
