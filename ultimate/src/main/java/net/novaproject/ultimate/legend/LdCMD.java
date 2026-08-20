package net.novaproject.ultimate.legend;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.ultimate.legend.LegendLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;

import java.util.Collections;

public class LdCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        Player player = (Player) args.getSender();
        Legend legend = Legend.get();

        if (args.getArguments().length == 0) {
            sendHelpMessage(player);
            return;
        }

        if (legend == null || !legend.isActive()) {
            LangManager.get().send(LegendLang.CMD_SCENARIO_NOT_ACTIVE, player);
            return;
        }

        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;

        switch (args.getLastArgument().toLowerCase()) {
            case "choose":
                if (legend.isCanChooseClass() && UHCManager.get().isGame()) {
                    new ChooseUi(player).open();
                } else {
                    LangManager.get().send(LegendLang.CMD_CHOOSE_EXPIRED, player);
                }
                break;

            case "info":
                LegendRole role = legend.getRoleByUHCPlayer(uhcPlayer);
                if (role != null) {
                    role.sendDescription(player);
                } else {
                    LangManager.get().send(LegendLang.CMD_NO_POWER, player);
                }
                break;

            default:
                sendHelpMessage(player);
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("");
        player.sendMessage("  §5§l/LD");
        player.sendMessage("  §8│ §f/ld choose §8— §7Choisir une classe (début de partie)");
        player.sendMessage("  §8│ §f/ld info §8— §7Voir la description de votre classe");
    }

    @Override
    public java.util.List<String> tabComplete(CommandArguments args) {
        if (args.getArguments().length == 2) {
            return getStrings(args, "choose", "info");
        }
        return Collections.emptyList();
    }
}

