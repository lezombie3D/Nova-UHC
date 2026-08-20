package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.utils.item.PendingItemsManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FullCMD extends Command.PlayerCommand {

    @Override
    protected void run(Player player, CommandArguments args) {

        if (!PendingItemsManager.hasPending(player.getUniqueId())) {
            LangManager.get().send(CoreLang.COMMON_FULL_NOTHING_PENDING, player);
            return;
        }

        int claimed = PendingItemsManager.claim(player);
        if (claimed > 0) {
            LangManager.get().send(CoreLang.COMMON_FULL_CLAIMED, player, Map.of("%count%", claimed));
            player.playSound(player.getLocation(), Sound.ITEM_PICKUP, 1f, 1f);
        }

        int remaining = PendingItemsManager.getPendingCount(player.getUniqueId());
        if (remaining > 0) {
            LangManager.get().send(CoreLang.COMMON_FULL_STILL_PENDING, player, Map.of("%count%", remaining));
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return Collections.emptyList();
    }
}

