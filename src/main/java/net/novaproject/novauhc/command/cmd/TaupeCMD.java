package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TaupeCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("Cette commande est réservée aux joueurs !");
            return true;
        }

        Player player = (Player) commandSender;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }
        if (!ScenarioManager.get().isScenarioActive("TaupeGun")) {
            CommonString.DISABLE_ACTION.send(player);
            return false;
        }
        String subCommand = args[0].toLowerCase();
        ScenarioManager.get().getActiveScenarios().forEach(scenario -> {
            scenario.onTaupeCMD(player, subCommand, args);
        });

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Utilisation de la commande : \n" +

                "/t tc : Envoie vos coordonnée à votre équipe de pas Taupe.\n" +
                "/t ti : Ouvrir l'inventaire de team de l'équipe de pas Taupe.\n" +
                "/t kit : Vous permet de récupérer votre kit.\n" +
                "/t reveal : Vous affiche en tant que Taupe au yeux de tout les joueur.\n" +
                "/tc : Si vous êtes une taupe vous permet d'envoyer vous coordonnée a votre équipe de taupe.\n" +
                "/ti : Si vous êtes une taupe vous permet d'accédez au TI de votre équipe de taupe.\n"

        );

    }

}
