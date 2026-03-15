package net.novaproject.jjk.role.Fleaux;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Mahito extends JJKRole {

    public Mahito() {
        setCamp(JJKCamp.FLEAUX);
    }

    @Override
    public String getName() {
        return "Mahito";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CLAY_BALL);
    }

    @Override
    public void onSetup() {
    }


}
