package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.ScenarioManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class FireForceCMD implements TabExecutor {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        return Collections.emptyList();
    }

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

        if (!ScenarioManager.get().isScenarioActive("Fire Force UHC")) {
            player.sendMessage(Common.get().getInfoTag() + "Le secnario Fire Force UHC doit etre actif pour pouvoir utiliser les commandes !");
            return false;
        }

        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onFfCMD(player, subCommand, args);
        });

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Utilisation de la commande : \n" +

                "/p helpop : Envoie un message anonyme au Host/cohost de la partie.\n"

        );
    }


}
