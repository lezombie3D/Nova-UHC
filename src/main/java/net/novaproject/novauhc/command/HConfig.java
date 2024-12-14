package net.novaproject.novauhc.command;

import net.novaproject.novauhc.ui.ScenariosUi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HConfig implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player player = (Player) commandSender;

        if (player.hasPermission("novauhc.host")){

            new ScenariosUi(player).open();

        }

        return false;
    }
}
