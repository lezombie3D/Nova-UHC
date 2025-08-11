package net.novaproject.novauhc.command;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamInventoryCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande est réservée aux joueurs !");
            return true;
        }

        Player player = (Player) sender;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (ScenarioManager.get().isScenarioActive("TeamInventory")) {
            if (uhcPlayer.getTeam().isPresent() && uhcPlayer.isPlaying() && UHCManager.get().getGameState() == UHCManager.GameState.INGAME) {
                player.openInventory(TeamInv.inventory.get(uhcPlayer.getTeam().get()));
            } else {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas d'équipe, vous êtes mort ou la partie n'a pas commencé !");
            }
        }
        return true;
    }

}
