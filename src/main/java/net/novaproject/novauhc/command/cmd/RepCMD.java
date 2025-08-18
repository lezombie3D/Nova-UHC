package net.novaproject.novauhc.command.cmd;


import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RepCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (args.length < 1) {
            CommonString.REP_USAGE.send(player);
            return true;
        }

        UUID targetUUID = MessageManager.getLastMessage(player.getUniqueId());

        if (targetUUID == null) {
            CommonString.REP_NO_ONE_TO_REPLY.send(player);
            return true;
        }

        Player target = Bukkit.getPlayer(targetUUID);

        if (target == null || !target.isOnline()) {
            CommonString.REP_PLAYER_OFFLINE.send(player);
            return true;
        }

        if (UHCManager.get().isChatdisbale()) {
            CommonString.CHAT_DISABLED.send(player);
            return true;
        }

        String message = String.join(" ", args);

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("%target%", target.getName());
        placeholders.put("%message%", message);
        CommonString.MSG_SENT_FORMAT.send(player, placeholders);

        placeholders.put("%sender%", player.getName());
        CommonString.MSG_RECEIVED_FORMAT.send(target, placeholders);

        MessageManager.setLastMessage(player.getUniqueId(), target.getUniqueId());
        return true;
    }
}
