package net.novaproject.novauhc.command;

import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Cette commande est réservée aux joueurs !");
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
                helpPopManager(args);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Commande inconnue. Essayez /p pour plus d'informations.");
        }
        return true;
    }

    private void helpPopManager(String[] args) {

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        for (UHCPlayer host : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            if (host.getPlayer().hasPermission("novauhc.host") || host.getPlayer().hasPermission("novauhc.cohost")) {
                host.getPlayer().sendMessage("§f[§6Helpop§f] " + message);
            }
        }

    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Utilisation de la commande : \n" +

                "/p helpop : Envoie un message anonyme au Host/cohost de la partie.\n"

        );
    }

}
