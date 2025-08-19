package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Compensation extends Scenario {
    @Override
    public String getName() {
        return "Compensation";
    }

    @Override
    public String getDescription() {
        return "A la mort d'un joueur, tous les membres de son équipe gagnent 2 cœurs.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND);
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (uhcPlayer.getTeam().isPresent()) {
            UHCTeam team = uhcPlayer.getTeam().get();
            team.getPlayers().forEach(uhcP -> uhcP.getPlayer().setMaxHealth(uhcP.getPlayer().getMaxHealth() + 4));
        }
    }
}
