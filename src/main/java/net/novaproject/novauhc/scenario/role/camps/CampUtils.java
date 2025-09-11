package net.novaproject.novauhc.scenario.role.camps;


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
}
