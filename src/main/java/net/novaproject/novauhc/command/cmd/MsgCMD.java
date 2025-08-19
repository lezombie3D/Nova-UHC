package net.novaproject.novauhc.command.cmd;


import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MsgCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (args.length < 2) {
            CommonString.MSG_USAGE.send(player);
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !target.isOnline()) {
            CommonString.MSG_PLAYER_OFFLINE.send(player);
            return true;
        }

        if (target == player) {
            CommonString.MSG_CANNOT_MESSAGE_SELF.send(player);
            return true;
        }

        if (UHCManager.get().isChatdisbale()) {
            CommonString.CHAT_DISABLED.send(player);
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        // Envoyer le message au sender
        Map<String, Object> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("%target%", target.getName());
        senderPlaceholders.put("%message%", message);
        CommonString.MSG_SENT_FORMAT.send(player, senderPlaceholders);

        // Envoyer le message au destinataire
        Map<String, Object> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("%sender%", player.getName());
        targetPlaceholders.put("%message%", message);
        CommonString.MSG_RECEIVED_FORMAT.send(target, targetPlaceholders);

        MessageManager.setLastMessage(player.getUniqueId(), target.getUniqueId());
        return true;
    }
}
