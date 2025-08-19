package net.novaproject.novauhc.command.cmd;


import net.novaproject.novauhc.CommonString;
import net.novaproject.novauhc.UHCManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class MsgCMD implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUsage: /msg <joueur> <message>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !target.isOnline()) {
            player.sendMessage("§cCe joueur est hors ligne.");
            return true;
        }

        if (target == player) {
            player.sendMessage("§cTu ne peux pas t'envoyer de message !");
            return true;
        }

        if (UHCManager.get().isChatdisbale()) {
            CommonString.CHAT_DISABLED.send(player);
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        player.sendMessage("§8│ §7§lMoi §7→ §7§l" + target.getName() + " §f" + message);
        target.sendMessage("§8│ §7§l" + player.getName() + " → Moi §f" + message);

        MessageManager.setLastMessage(player.getUniqueId(), target.getUniqueId());
        return true;
    }
}
