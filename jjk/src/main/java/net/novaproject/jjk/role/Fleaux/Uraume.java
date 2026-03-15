package net.novaproject.jjk.role.Fleaux;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Uraume extends JJKRole {

    public Uraume() {
        setCamp(JJKCamp.FLEAUX);
    }

    @Override
    public String getName() {
        return "Uraume";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SNOW_BALL);
    }

    @Override
    public void onSetup() {
    }


}
