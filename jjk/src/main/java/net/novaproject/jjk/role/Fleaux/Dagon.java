package net.novaproject.jjk.role.Fleaux;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Dagon extends JJKRole {

    public Dagon() {
        setCamp(JJKCamp.FLEAUX);
    }

    @Override
    public String getName() {
        return "Dagon";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WATER_BUCKET);
    }

    @Override
    public void onSetup() {
       System.out.println("hello");
    }


}
