package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.utils.VariableType;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Compensation extends Scenario {

    @ScenarioVariable(
            name = "hearts_per_death",
            description = "Nombre de cœurs que chaque membre d'équipe reçoit à la mort d'un joueur",
            type = VariableType.DOUBLE
    )
    private double heartsPerDeath = 2.0;

    @Override
    public String getName() {
        return "Compensation";
    }

    @Override
    public String getDescription() {
        return "À la mort d'un joueur, tous les membres de son équipe gagnent " + heartsPerDeath + " cœurs.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();
            double healthToAdd = heartsPerDeath * 2;
            team.getPlayers().forEach(uhcP -> uhcP.getPlayer().setMaxHealth(uhcP.getPlayer().getMaxHealth() + healthToAdd));
        }
    }
}
