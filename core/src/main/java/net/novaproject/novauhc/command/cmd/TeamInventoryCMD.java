package net.novaproject.novauhc.command.cmd;

import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.command.Command;
import net.novaproject.novauhc.command.CommandArguments;
import net.novaproject.novauhc.lang.lang.CoreLang;
import net.novaproject.novauhc.scenario.ScenarioManager;
import net.novaproject.novauhc.scenario.normal.TeamInv;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import org.bukkit.entity.Player;

public class TeamInventoryCMD extends Command {

    @Override
    public void execute(CommandArguments args) {
        if (!(args.getSender() instanceof Player player)) return;
        UHCPlayer uhcPlayer = UHCPlayerManager.get().getPlayer(player);
        if (uhcPlayer == null) return;
        if (ScenarioManager.get().isScenarioActive("TeamInventory")) {
            if (uhcPlayer.getTeam().isPresent() && uhcPlayer.isPlaying() && UHCManager.get().getGameState() == UHCManager.GameState.INGAME) {
                player.openInventory(TeamInv.inventory.get(uhcPlayer.getTeam().get()));
            } else {
                LangManager.get().send(CoreLang.COMMON_DISABLE_ACTION, player);
            }
        }
    }
}

