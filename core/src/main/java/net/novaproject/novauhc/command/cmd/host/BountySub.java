package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.game.Systems.BountySystem;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class BountySub extends HostSub {

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 1) {
            LangManager.get().send(CoreLang.CMD_BOUNTY_USAGE, player);
            return;
        }
        String action = args.get(0, "").toLowerCase();
        if (action.equals("list")) {
            boolean any = false;
            for (UHCPlayer up : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                int amount = BountySystem.get(up.getUuid());
                if (amount > 0 && up.getPlayer() != null) {
                    LangManager.get().send(CoreLang.CMD_BOUNTY_LINE, player, Map.of("%player%", up.getPlayer().getName(), "%amount%", amount));
                    any = true;
                }
            }
            if (!any) LangManager.get().send(CoreLang.CMD_BOUNTY_NONE, player);
            return;
        }
        if (args.size() < 2) {
            LangManager.get().send(CoreLang.CMD_BOUNTY_ACTION_USAGE, player, Map.of("%action%", action));
            return;
        }
        Player target = Bukkit.getPlayerExact(args.get(1, ""));
        if (target == null) {
            LangManager.get().send(CoreLang.CMD_PLAYER_NOT_FOUND, player);
            return;
        }
        switch (action) {
            case "add", "set" -> {
                if (args.size() < 3) {
                    LangManager.get().send(CoreLang.CMD_BOUNTY_AMOUNT_USAGE, player, Map.of("%action%", action));
                    return;
                }
                try {
                    int amount = Integer.parseInt(args.get(2, ""));
                    if (action.equals("add")) BountySystem.add(target.getUniqueId(), amount);
                    else BountySystem.set(target.getUniqueId(), amount);
                    int total = BountySystem.get(target.getUniqueId());
                    LangManager.get().sendAll(CoreLang.COMMON_BOUNTY_SET,
                            Map.of("%victim%", target.getName(), "%amount%", total));
                } catch (NumberFormatException e) {
                    LangManager.get().send(CoreLang.CMD_BOUNTY_INVALID_AMOUNT, player);
                }
            }
            case "clear" -> {
                BountySystem.clear(target.getUniqueId());
                LangManager.get().send(CoreLang.CMD_BOUNTY_CLEARED, player, Map.of("%player%", target.getName()));
            }
            default -> LangManager.get().send(CoreLang.CMD_BOUNTY_USAGE, player);
        }
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getStrings(args, "add", "set", "clear", "list");
        if (args.size() == 2) return getOnlinePlayers(args);
        return List.of();
    }
}

