package net.novaproject.novauhc.scenario.special.king;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class King extends Scenario {
    List<UHCPlayer> kings = new ArrayList<>();

    @Override
    public String getName() {
        return "King";
    }

    @Override
    public String getDescription() {
        return "Un joueur devient roi et doit être protégé par son équipe.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.GOLD_BLOCK);
    }

    @Override
    public void onStart(Player player) {
        for (UHCTeam team : UHCTeamManager.get().getTeams()) {
            UHCPlayer king = team.getPlayers().get(new Random().nextInt(team.getPlayers().size()));
            kings.add(king);
            king.getPlayer().sendMessage("[§aKing§r] Vous êtes le roi de cette équipe");
            king.getPlayer().setMaxHealth(40);
            king.getPlayer().setHealth(40);
            king.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999, 0, false, false));
            king.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 99999, 0, false, false));
            king.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 99999, 0, false, false));
        }


    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() != 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            uhcPlayer.getPlayer().teleport(location);
        }
    }

    @Override
    public void onDeath(UHCPlayer uhcPlayer, UHCPlayer killer, PlayerDeathEvent event) {
        if (kings.contains(UHCPlayerManager.get().getPlayer(uhcPlayer.getPlayer()))) {
            if (!uhcPlayer.getTeam().get().getPlayers().isEmpty()) {
                for (UHCPlayer other : uhcPlayer.getTeam().get().getPlayers()) {
                    other.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 2 * 20 * 60, 0, false, false));
                }
            }
        }

    }


    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            CommonString.TEAM_REDFINIED_AUTO.sendAll();
        }

    }

    @Override
    public void toggleActive() {
        super.toggleActive();
        if (isActive()) {
            UHCManager.get().setTeam_size(2);
        } else {
            UHCTeamManager.get().deleteTeams();
        }
    }

}
