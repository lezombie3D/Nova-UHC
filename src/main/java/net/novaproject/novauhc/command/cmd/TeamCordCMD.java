package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class TeamCordCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        Player player = (Player) args.getSender();
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (!uhcPlayer.getTeam().isPresent() || !uhcPlayer.isPlaying() ||
                UHCManager.get().getGameState() != UHCManager.GameState.INGAME) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas d'équipe ou la partie n'a pas commencé !");
            return;
        }

        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        String coordsMessage = ChatColor.GREEN + "Coord : x: " + x + " y: " + y + " z: " + z;
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onTeamCordCMD(player, x, y, z, coordsMessage);
        });
        if (ScenarioManager.get().isScenarioActive("TaupeGun")) {
            ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
                scenario.onTaupeTcCMD(player, x, y, z, coordsMessage);
            });
        } else {

            String teamMessage =
                    "§7[§6%team%§7] §f" + player.getName() + " §8» §f " + coordsMessage;

            uhcPlayer.getTeam().get().getPlayers().forEach(teamPlayer ->
                    teamPlayer.getPlayer().sendMessage(teamMessage));
        }
    }
}