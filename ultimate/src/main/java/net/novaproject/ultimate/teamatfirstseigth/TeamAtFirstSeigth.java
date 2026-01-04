package net.novaproject.ultimate.teamatfirstseigth;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TeamAtFirstSeigth extends Scenario {

    private final List<UHCTeam> pickedTeams = new ArrayList<>();

    @Override
    public String getName() {
        return "TeamAtFirstSight";
    }

    @Override
    public String getDescription() {
        return "Les équipes se forment automatiquement entre les premiers joueurs qui se voient.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CAKE);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public void onStart(Player player) {
        pickedTeams.clear();
        for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            uhcPlayer.setTeam(Optional.empty());
        }
    }

    @Override
    public void onTeamUpdate() {
        if (UHCManager.get().getTeam_size() == 1) {
            UHCManager.get().setTeam_size(2);
            CommonString.TEAM_REDFINIED_AUTO.sendAll();
        }

    }

    @Override
    public void onSec(Player p) {
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(p);
        synchronized (uhcPlayer) {
            if (uhcPlayer.getTeam().isPresent()) return;
            Set<UHCPlayer> teamMembers = findValidTeamMembers(uhcPlayer);
            if (!teamMembers.isEmpty()) assignTeam(uhcPlayer, teamMembers);
        }
    }

    private Set<UHCPlayer> findValidTeamMembers(UHCPlayer leader) {
        Set<UHCPlayer> validMembers = new HashSet<>();
        List<UHCPlayer> availablePlayers = getNearbyPlayers(leader.getPlayer(), 20).stream()
                .filter(p -> !p.getTeam().isPresent())
                .collect(Collectors.toList());

        validMembers.add(leader);
        availablePlayers.forEach(candidate -> {
            if (validMembers.stream().allMatch(member -> isPlayerNearby(candidate.getPlayer(), member.getPlayer(), 20))) {
                validMembers.add(candidate);
            }
        });

        return validMembers.size() > 1 ? validMembers : Collections.emptySet();
    }

    private void assignTeam(UHCPlayer leader, Set<UHCPlayer> teamMembers) {
        UHCTeam team = UHCTeamManager.get().getTeams().stream()
                .filter(t -> t.getPlayers().size() < UHCManager.get().getTeam_size())
                .findFirst().orElse(null);

        if (team == null) {
            leader.getPlayer().sendMessage(ChatColor.RED + "Aucune équipe disponible !");
            return;
        }

        synchronized (team) {
            if (!teamMembers.stream().allMatch(p -> !p.getTeam().isPresent() && isPlayerNearby(leader.getPlayer(), p.getPlayer(), 20))) {
                leader.getPlayer().sendMessage(ChatColor.RED + "Les joueurs ne sont plus disponibles ou trop éloignés !");
                return;
            }

            teamMembers.forEach(member -> {
                synchronized (member) {
                    member.setTeam(Optional.of(team));
                    member.getPlayer().sendMessage(ChatColor.GREEN + "Vous avez rejoint l'équipe " + team.name() + " avec " + (teamMembers.size() - 1) + " autres joueurs !");
                }
            });
        }
    }

    private boolean isPlayerNearby(Player p1, Player p2, double radius) {
        return p1.getWorld() == p2.getWorld() && p1.getLocation().distanceSquared(p2.getLocation()) <= radius * radius;
    }

    private List<UHCPlayer> getNearbyPlayers(Player player, double radius) {
        return UHCPlayerManager.get().getPlayingOnlineUHCPlayers().stream()
                .filter(p -> !p.equals(player) && isPlayerNearby(player, p.getPlayer(), radius))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isWin() {
        return UHCTeamManager.get().getAliveTeams().size() == 1 && UHCPlayerManager.get().getPlayingOnlineUHCPlayers().size() == UHCManager.get().getTeam_size();
    }


    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        uhcPlayer.getPlayer().teleport(location);
    }
}
