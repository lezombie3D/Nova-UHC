package net.novaproject.novauhc.scenario.special.legend;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.scenario.ScenarioLangManager;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.special.legend.core.LegendData;
import net.novaproject.novauhc.scenario.special.legend.ui.ChooseUi;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class LdCMD extends Command {

    private Legend legend;

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.DARK_PURPLE + "Utilisation de la commande : \n" +

                "/ld choose : Uniquement disponible au début de la partie, vous permet de choisir un classe.\n" +
                "/ld pouvoir : Vous permet de vous redonnez vos item de pouvoir (si vous en possédez).\n"

        );
    }


    @Override
    public void execute(CommandArguments args) {
        Player player = (Player) args.getSender();
        legend = ScenarioManager.get().getScenario(Legend.class);
        if (args.getArguments().length == 0) {
            sendHelpMessage(player);
            return;
        }

        if (!legend.isActive()) {
            player.sendMessage(Common.get().getInfoTag() + "Le secnario UHC Legends doit etre actif pour pouvoir utiliser les commandes !");
            return;
        }
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);

        if (legend.isPuppet(uhcPlayer)) {
            ScenarioLangManager.send(player, LegendLang.MARIONNETTISTE_PUPPET_RESTRICTION_COMMAND);
            return;
        }

        switch (args.getLastArgument().toLowerCase()) {
            case "choose":
                if (legend.isCanChooseClass() && UHCManager.get().isGame()) {
                    new ChooseUi(player).open();
                } else {
                    player.sendMessage(ChatColor.RED + "Vous ne pouvez plus choisir de classe");
                }
                break;

            case "pouvoir":
                LegendData data = legend.getPlayerData().get(uhcPlayer);
                if (data != null && data.getLegendClass().hasPower()) {
                    player.sendMessage(ChatColor.GREEN + "Utilisez votre pouvoir avec l'item correspondant !");
                } else {
                    player.sendMessage(ChatColor.RED + "Votre classe n'a pas de pouvoir activable");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Commande inconnue. Utilisez :");
                player.sendMessage(ChatColor.YELLOW + "/ld choose - Choisir une classe");
                player.sendMessage(ChatColor.YELLOW + "/ld pouvoir - Informations sur votre pouvoir");
        }
    }

    @Override
    public java.util.List<String> tabComplete(CommandArguments args) {
        if (args.getArguments().length == 2) {
            return getStrings(args, "pouvoir", "chooe");
        }
        return java.util.Collections.emptyList();
    }
}
