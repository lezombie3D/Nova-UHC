package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.DynamicLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CoreLang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class ForceSub extends HostSub {

    private static final DynamicLang USAGE = DynamicLang.of("cmd.FORCE_USAGE",
            "§c✖ §7Usage: §f/h force <pvp|roles|mtp|event|team>");

    public ForceSub() {
        sub("pvp", HostSub.of((player, args) -> {
            UHCManager.get().setPvpTimer(UHCManager.get().getTimer() + 1);
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.CMD_FORCE_PVP));
        }));
        sub("roles", new HostSubs.ForceRolesSub());
        sub("mtp", HostSub.of((player, args) -> {
            UHCManager.get().setBorderTimer(UHCManager.get().getTimer() + 1);
            Bukkit.broadcastMessage(LangManager.get().get(CoreLang.CMD_FORCE_MEETUP));
        }));
        sub("event", new ForceEventSub());
        sub("team", new ForceTeamSub());
    }

    @Override
    protected void run(Player player, CommandArguments args) {
        if (dispatchSub(args)) return;
        LangManager.get().send(USAGE, player);
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        return tabCompleteSub(args);
    }
}
