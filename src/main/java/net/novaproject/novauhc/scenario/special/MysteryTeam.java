package net.novaproject.novauhc.scenario.special;

import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioLang;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.lang.MysteryTeamLang;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.TeamsTagsManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class MysteryTeam extends Scenario {
    @Override
    public String getName() {
        return "MysteryTeam";
    }

    @Override
    public String getDescription() {
        return "Des équipes mystérieuses sont formées, découvrez vos coéquipiers !";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BANNER);
    }

    @Override
    public String getPath() {
        return "special/mysteryteam";
    }

    @Override
    public ScenarioLang[] getLang() {
        return MysteryTeamLang.values();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onStart(Player player) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        ScenarioLangManager.send(player, MysteryTeamLang.FIND_TEAMMATES);
        if (uhcPlayer.getTeam().isPresent()) {
            player.getInventory().addItem(uhcPlayer.getTeam().get().getBanner().getItemstack());
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        uhcPlayer.getPlayer().teleport(location);
        uhcPlayer.setTeam(Optional.empty());
        List<UHCTeam> teams = UHCTeamManager.get().getTeams();
        chooseTeam(uhcPlayer, teams);
    }

    private void chooseTeam(UHCPlayer uhcPlayer, List<UHCTeam> teams) {
        List<UHCTeam> availableTeams = teams.stream()
                .filter(team -> team.getPlayers().size() < team.getTeamSize()).collect(Collectors.toList());
        if (availableTeams.isEmpty()) {
            return;
        }

        UHCTeam randomTeam = availableTeams.get(new Random().nextInt(availableTeams.size()));

        uhcPlayer.setTeam(Optional.of(randomTeam));
        TeamsTagsManager.setNameTag(uhcPlayer.getPlayer(), "", "", "");
    }

}