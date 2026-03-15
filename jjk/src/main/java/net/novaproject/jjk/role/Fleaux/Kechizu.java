package net.novaproject.jjk.role.Fleaux;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Kechizu extends JJKRole {

    public Kechizu() {
        setCamp(JJKCamp.FLEAUX);
    }

    @Override
    public String getName() {
        return "Kechizu";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.ROTTEN_FLESH);
    }

    @Override
    public void onSetup() {
    }


}
