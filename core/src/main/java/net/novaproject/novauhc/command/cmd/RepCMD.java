package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.utils.chat.ChatManager.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RepCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {

        LangManager lm = LangManager.get();
        if (args.size() < 1) {
            player.sendMessage(lm.get(CoreLang.CMD_REP_USAGE, player));
            return;
        }

        if (UHCManager.get().isChatDisabled()) {
            lm.send(CoreLang.COMMON_CHAT_DISABLED, player);
            return;
        }

        UUID targetId = MessageManager.getLastMessage(player.getUniqueId());
        if (targetId == null) {
            player.sendMessage(lm.get(CoreLang.CMD_REP_NO_TARGET, player));
            return;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target == null || !target.isOnline()) {
            player.sendMessage(lm.get(CoreLang.CMD_REP_OFFLINE, player));
            return;
        }

        String message = args.join(0);
        player.sendMessage(lm.get(CoreLang.CMD_REP_SENT, player, Map.of("%target%", target.getName(), "%message%", message)));
        target.sendMessage(lm.get(CoreLang.CMD_REP_RECEIVED, target, Map.of("%sender%", player.getName(), "%message%", message)));

        MessageManager.setLastMessage(player.getUniqueId(), target.getUniqueId());
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return Collections.emptyList();
    }
}

