package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class MaxHealthSub extends HostSub {

    private static final int MIN_HALF_HEARTS = 1;
    private static final int MAX_HALF_HEARTS = 80;

    @Override
    protected void run(Player player, CommandArguments args) {
        if (!UHCManager.get().isGame()) {
            LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            return;
        }
        if (args.size() < 2) {
            LangManager.get().send(CoreLang.CMD_MAXHEALTH_USAGE, player);
            return;
        }
        Player target = Bukkit.getPlayer(args.get(0, ""));
        if (target == null) {
            LangManager.get().send(CoreLang.CMD_INVALID_PLAYER, player);
            return;
        }
        int halfHearts = args.getInt(1, 0);
        if (halfHearts < MIN_HALF_HEARTS) {
            LangManager.get().send(CoreLang.CMD_MAXHEALTH_INVALID, player);
            return;
        }
        if (halfHearts > MAX_HALF_HEARTS) halfHearts = MAX_HALF_HEARTS;
        target.setMaxHealth(halfHearts);
        if (target.getHealth() > halfHearts) {
            target.setHealth(halfHearts);
        }
        UHCPlayer targetUp = UHCPlayerManager.get().getPlayer(target);
        if (targetUp != null) {
            targetUp.setLastMaxHealth(halfHearts);
            if (!targetUp.isPlaying()) {
                LangManager.get().send(CoreLang.CMD_MAXHEALTH_NOT_PLAYING, player,
                        Map.of("%player%", target.getName()));
            }
        }
        String hearts = formatHearts(halfHearts);
        LangManager.get().send(CoreLang.CMD_MAXHEALTH_DONE, player,
                Map.of("%player%", target.getName(), "%half%", halfHearts, "%hearts%", hearts));
        LangManager.get().send(CoreLang.COMMON_HOST_MAXHEALTH_SET, target, Map.of("%hearts%", hearts));
    }

    private String formatHearts(int halfHearts) {
        return halfHearts % 2 == 0 ? String.valueOf(halfHearts / 2) : (halfHearts / 2) + ".5";
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getOnlinePlayers(args);
        if (args.size() == 2) return getNumbers(args, 16, 20, 24, 40);
        return List.of();
    }
}
