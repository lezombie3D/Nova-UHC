package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;

import org.bukkit.entity.Player;

import java.util.Map;

public class TeamCordCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        LangManager lm = LangManager.get();
        if (!(args.getSender() instanceof Player player)) return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null || uhcPlayer.getTeam().isEmpty()) {
            player.sendMessage(lm.get(CoreLang.CMD_TEAMCORD_NO_TEAM, player));
            return;
        }
        UHCTeam team = uhcPlayer.getTeam().get();
        String coordsMessage = player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ();
        String teamMessage = lm.get(CoreLang.CMD_TEAMCORD_FORMAT, player, Map.of(
                "%team%", team.name(), "%player%", player.getName(), "%coords%", coordsMessage));
        team.getPlayers().stream()
                .filter(UHCPlayer::isOnline)
                .forEach(p -> p.getPlayer().sendMessage(teamMessage));
    }
}

