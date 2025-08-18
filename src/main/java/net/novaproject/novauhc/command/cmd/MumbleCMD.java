package net.novaproject.novauhc.command.cmd;


import net.novaproject.novauhc.ui.MumbleUi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MumbleCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Cette commande est réservée aux joueurs !");
            return true;
        }
        if (args.length > 0) {
            return true;
        }

        Player player = (Player) sender;

        new MumbleUi(player).open();

        return true;
    }
}
