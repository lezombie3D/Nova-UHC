package net.novaproject.novauhc.command.cmd.host;

import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ForceTeamSub extends HostSub {

    @Override
    protected void run(Player player, CommandArguments args) {
        if (args.size() < 2) {
            LangManager.get().send(CoreLang.CMD_FORCETEAM_USAGE, player);
            return;
        }
        Player bukkitPlayer = Bukkit.getPlayer(args.get(0, ""));
        if (bukkitPlayer == null) {
            LangManager.get().send(CoreLang.CMD_PLAYER_NOT_FOUND, player);
            return;
        }
        UHCPlayer target = UHCPlayerManager.get().getPlayer(bukkitPlayer);
        if (target == null) {
            LangManager.get().send(CoreLang.CMD_PLAYER_NOT_PLAYING, player);
            return;
        }
        String teamName = args.get(1, "").replace("&", "§").replace("_", " ");
        Optional<UHCTeam> team = UHCTeamManager.get()
                .getTeams()
                .stream()
                .filter(t -> t.getTeam().getName().equalsIgnoreCase(teamName))
                .findFirst();
        if (!team.isPresent()) {
            LangManager.get().send(CoreLang.CMD_TEAM_NOT_FOUND, player);
            return;
        }
        target.forceSetTeam(team);
        LangManager.get().send(CoreLang.CMD_FORCETEAM_SUCCESS, player, Map.of("%player%", target.getPlayer().getName(), "%team%", teamName));
    }

    @Override
    public List<String> tabComplete(CommandArguments args) {
        if (args.size() == 1) return getOnlinePlayers(args);
        if (args.size() == 2) return getTeams(args);
        return List.of();
    }
}

