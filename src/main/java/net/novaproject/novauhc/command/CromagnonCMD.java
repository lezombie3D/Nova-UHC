package net.novaproject.novauhc.command;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CromagnonCMD implements CommandExecutor {
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

        if (!ScenarioManager.get().isScenarioActive("Cromagnon")) {
            player.sendMessage(Common.get().getInfoTag() + "Le secnario Cromagnon doit etre actif pour pouvoir utiliser les commandes !");
            return false;
        }
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onCroCMD(player, subCommand, args);
        });

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Utilisation de la commande : \n" +

                "/p helpop : Envoie un message anonyme au Host/cohost de la partie.\n"

        );
    }


}
