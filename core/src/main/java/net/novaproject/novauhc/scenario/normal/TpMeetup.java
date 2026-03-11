package net.novaproject.novauhc.scenario.normal;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.lang.lang.CommonLang;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.scenario.ScenarioVariable;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.utils.ItemCreator;
import net.novaproject.novauhc.utils.VariableType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;

public class TpMeetup extends Scenario {

    @ScenarioVariable(nameKey = "TPMEETUP_VAR_TIMER_TP_NAME", descKey = "TPMEETUP_VAR_TIMER_TP_DESC",type = VariableType.TIME)
    private int timerTP = 3600;

    @Override
    public String getName() {
        return "TP Meetup";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.TPMEETUP, player);
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ENDER_PEARL);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();

        if (!isActive()) return;

        if (timer == timerTP) {
            int y = Common.get().getArena().getHighestBlockYAt(0, 0);
            Location loc = new Location(Common.get().getArena(), 0, y, 0);
            for (UHCPlayer uhcPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                uhcPlayer.getPlayer().teleport(loc);
                LangManager.get().sendAll(CommonLang.TP_MESSAGE, Map.of("%player%", uhcPlayer.getPlayer().getName()));
            }

        }
    }
}
