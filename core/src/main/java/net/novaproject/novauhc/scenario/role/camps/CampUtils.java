package net.novaproject.novauhc.scenario.role.camps;


import net.novaproject.novauhc.scenario.role.Role;
import net.novaproject.novauhc.scenario.role.ScenarioRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.uhcplayer.UHCPlayerManager;

import java.util.ArrayList;
import java.util.List;

public class CampUtils {

    public static boolean hasSubCamps(Camps parent, Camps[] allCamps) {
        for (Camps c : allCamps) {
            if (!c.isMainCamp() && c.getParent() == parent) {
                return true;
            }
        }
        return false;
    }


    public static List<Camps> getSubCamps(Camps parent, Camps[] allCamps) {
        List<Camps> list = new ArrayList<>();
        for (Camps c : allCamps) {
            if (!c.isMainCamp() && c.getParent() == parent) {
                list.add(c);
            }
        }
        return list;
    }

    public static List<UHCPlayer> getPlayersInCamp(Camps camp, ScenarioRole scenarioRole) {
        List<UHCPlayer> list = new ArrayList<>();
        for (UHCPlayer player : UHCPlayerManager.get().getPlayingOnlineUHCPlayers()) {
            Role role = scenarioRole.getRoleByUHCPlayer(player);
            if (role.getCamp() == camp) list.add(player);
        }
        return list;
    }
}
