package net.novaproject.novauhc.command;

import net.novaproject.novauhc.ui.ScenariosUi;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HConfig implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Cette commande est réservée aux joueurs !");
            return true;
        }

        Player player = (Player) commandSender;
        new ScenariosUi(player).open();

        return true;
    }
}
