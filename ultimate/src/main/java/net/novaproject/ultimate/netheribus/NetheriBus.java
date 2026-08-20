package net.novaproject.ultimate.netheribus;
import net.novaproject.novauhc.utils.variable.Var;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.lang.lang.ScenarioDescLang;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.player.UHCPlayer;
import net.novaproject.novauhc.player.UHCPlayerManager;
import net.novaproject.novauhc.team.UHCTeam;
import net.novaproject.novauhc.team.UHCTeamManager;
import net.novaproject.novauhc.utils.item.ItemCreator;
import net.novaproject.novauhc.lang.LangManager;
import net.novaproject.novauhc.utils.variable.VariableType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class NetheriBus extends Scenario {

    @Var(name = "Start timer", desc = "Time before players outside Nether start taking damage.", type = VariableType.TIME)
    private int startTimer = 600;

    @Var(name = "Damage outside Nether", desc = "Damage dealt to players outside Nether after timer.", type = VariableType.INTEGER)
    private int damageOutsideNether = 2;

    @Override
    public String getName() {
        return "NetheriBus";
    }

    @Override
    public String getDescription(Player player) {
        return LangManager.get().get(ScenarioDescLang.NETHERIBUS, player, Map.of(
                "%time%", startTimer,
                "%damage%", damageOutsideNether
        ));
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.MINECART);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        if (timer > startTimer) {
            for (UHCPlayer uPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                if (!uPlayer.getPlayer().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                    uPlayer.getPlayer().damage(damageOutsideNether);
                }
            }
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if(UHCManager.get().getTeam_size() > 1){
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
            return;
        }
        uhcPlayer.getPlayer().teleport(location);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

}

