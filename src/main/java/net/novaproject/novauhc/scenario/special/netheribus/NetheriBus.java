package net.novaproject.novauhc.scenario.special.netheribus;

import net.novaproject.novauhc.UHCManager;
import net.novaproject.novauhc.scenario.Scenario;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;
import net.novaproject.novauhc.uhcteam.UHCTeam;
import net.novaproject.novauhc.uhcteam.UHCTeamManager;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class NetheriBus extends Scenario {
    @Override
    public String getName() {
        return "NetheriBus";
    }

    @Override
    public String getDescription() {
        return "Modifie l'expérience du Nether avec des mécaniques spéciales.";
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.MINECART);
    }

    @Override
    public void onSec(Player p) {
        int timer = UHCManager.get().getTimer();
        if (timer > UHCManager.get().getTimerborder()) {
            for (UHCPlayer uPlayer : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
                if (!uPlayer.getPlayer().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                    uPlayer.getPlayer().damage(2);
                }
            }
        }
    }

    @Override
    public void scatter(UHCPlayer uhcPlayer, Location location, HashMap<UHCTeam, Location> teamloc) {
        if (UHCManager.get().getTeam_size() != 1) {
            UHCTeamManager.get().scatterTeam(uhcPlayer, teamloc);
        } else {
            uhcPlayer.getPlayer().teleport(location);
        }
    }

    @Override
    public boolean isSpecial() {
        return true;
    }


}
