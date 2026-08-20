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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MsgCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {

        if (args.size() < 2) {
            LangManager.get().send(CoreLang.COMMON_MSG_USAGE, player);
            return;
        }

        if (UHCManager.get().isChatDisabled()) {
            LangManager.get().send(CoreLang.COMMON_CHAT_DISABLED, player);
            return;
        }

        Player target = Bukkit.getPlayerExact(args.getArgument(0));
        if (target == null || !target.isOnline()) {
            LangManager.get().send(CoreLang.COMMON_MSG_PLAYER_OFFLINE, player);
            return;
        }
        if (target == player) {
            LangManager.get().send(CoreLang.COMMON_MSG_CANNOT_MESSAGE_SELF, player);
            return;
        }

        String message = args.join(1);

        Map<String, Object> senderPlaceholders = new HashMap<>();
        senderPlaceholders.put("%target%", target.getName());
        senderPlaceholders.put("%message%", message);
        LangManager.get().send(CoreLang.COMMON_MSG_SENT_FORMAT, player, senderPlaceholders);

        Map<String, Object> targetPlaceholders = new HashMap<>();
        targetPlaceholders.put("%sender%", player.getName());
        targetPlaceholders.put("%message%", message);
        LangManager.get().send(CoreLang.COMMON_MSG_RECEIVED_FORMAT, target, targetPlaceholders);

        MessageManager.setLastMessage(player.getUniqueId(), target.getUniqueId());
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() != 1) return Collections.emptyList();
        String prefix = args.getArgument(0).toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                .collect(Collectors.toList());
    }
}

