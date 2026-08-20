package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.utils.variable.Var;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.lang.LangManager;

public class TeamInv extends Scenario {

    @Override
    public Family getFamily() { return Family.LOOT; }
    public static HashMap<UHCTeam, Inventory> inventory = new HashMap<>();

    @Var(name = "Lignes de l'inventaire", desc = "Nombre de lignes de 9 slots de l'inventaire partagé.", type = VariableType.INTEGER, min = 1, max = 6)
    private int sharedRows = 3;

    @Override
    public String getName() {
        return "TeamInventory";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.TEAM_INV, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CHEST);
    }

    public static Inventory createShared() {
        for (Scenario scenario : ScenarioManager.get().getScenarios()) {
            if (scenario instanceof TeamInv teamInv) {
                return Bukkit.createInventory(null, teamInv.sharedRows * 9, "Team Inventory");
            }
        }
        return Bukkit.createInventory(null, 27, "Team Inventory");
    }

    @Override
    public void onStart(Player player) {
        for (UHCTeam t : UHCTeamManager.get().getTeams())
            inventory.computeIfAbsent(t, team -> createShared());
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (!isActive()) return;
        uhcPlayer.getTeam().filter(team -> !team.isAlive()).ifPresent(team -> {
            Inventory shared = inventory.remove(team);
            if (shared == null || uhcPlayer.getPlayer() == null) return;
            Location loc = uhcPlayer.getPlayer().getLocation();
            for (ItemStack item : shared.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    loc.getWorld().dropItemNaturally(loc, item);
                }
            }
            shared.clear();
        });
    }

    @Override
    public void onStop() {
        inventory.clear();
    }

}

