package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.Main;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lobby.HotbarManager;
import net.novaproject.novauhc.lobby.RankManager.Rank;
import net.novaproject.novauhc.lobby.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class CohostSub extends HostSub {

    @Override
    protected void run(Player player, CommandArguments args) {
        if (!player.hasPermission("novauhc.host")) {
            LangManager.get().send(CoreLang.CMD_COHOST_ONLY_HOST, player);
            return;
        }
        if (args.size() < 2) {
            LangManager.get().send(CoreLang.COMMON_HOST_COHOST_USAGE, player);
            return;
        }
        String action = args.get(0, "").toLowerCase();
        Player target = Bukkit.getPlayer(args.get(1, ""));
        if (target == null) {
            LangManager.get().send(CoreLang.COMMON_PLAYER_NOT_FOUND, player);
            return;
        }
        switch (action) {
            case "add":
                if (target == player) return;
                target.addAttachment(Main.get(), "novauhc.cohost", true);
                LangManager.get().send(CoreLang.COMMON_COHOST_ADDED, player, Map.of("%player%", target.getName()));
                if (UHCManager.get().isLobby()) {
                    RankManager.get().applyTag(target, Rank.COHOST);
                    HotbarManager.get().giveHotbar(target);
                }
                break;
            case "remove":
                if (target == player) return;
                target.addAttachment(Main.get(), "novauhc.cohost", false);
                LangManager.get().send(CoreLang.COMMON_COHOST_REMOVED, player, Map.of("%player%", target.getName()));
                if (UHCManager.get().isLobby()) {
                    RankManager.get().applyTag(target, Rank.PLAYER);
                    HotbarManager.get().giveHotbar(target);
                }
                break;
            default:
                LangManager.get().send(CoreLang.COMMON_HOST_COHOST_USAGE, player);
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getStrings(args, "add", "remove");
        if (args.size() == 2) return getOnlinePlayers(args);
        return List.of();
    }
}

