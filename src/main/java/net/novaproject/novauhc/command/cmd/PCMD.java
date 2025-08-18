package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(CommonString.COMMAND_PLAYERS_ONLY.getMessage());
            return true;
        }


        Player player = (Player) commandSender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();


        switch (subCommand) {

            case "helpop":
                helpPopManager(args, player);
                break;
            default:
                CommonString.COMMAND_UNKNOWN.send(player);
        }
        return true;
    }

    private void helpPopManager(String[] args, Player player) {
        if (args.length < 2) {
            CommonString.PLAYER_HELPOP_USAGE.send(player);
            return;
        }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        for (UHCPlayer host : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            if (host.getPlayer().hasPermission("novauhc.host") || host.getPlayer().hasPermission("novauhc.cohost")) {
                host.getPlayer().sendMessage(CommonString.HELPOP_MESSAGE.getMessage(player) + "§f" + message);
            }
        }

    }

    private void sendHelpMessage(Player player) {
        CommonString.PLAYER_HELP_MESSAGE.send(player);
    }

}
