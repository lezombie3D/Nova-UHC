package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import org.bukkit.entity.Player;

public class HelpOp extends Command {
    @Override
    public void execute(CommandArguments args) {
        if (args.getArguments().length < 2) {
            return;
        }
        Player player = (Player) args.getSender();
        String message = String.join(" ", java.util.Arrays.copyOfRange(args.getArguments(), 1, args.getArguments().length));
        for (UHCPlayer host : UHCPlayerManager.get().getOnlineUHCPlayers()) {
            if (host.getPlayer().hasPermission("novauhc.host") || host.getPlayer().hasPermission("novauhc.cohost")) {
                host.getPlayer().sendMessage(CommonString.HELPOP_MESSAGE.getMessage(player) + "§f" + message);
            }
        }
    }
}
