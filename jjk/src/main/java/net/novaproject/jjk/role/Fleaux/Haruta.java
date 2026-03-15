package net.novaproject.jjk.role.Fleaux;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Haruta extends JJKRole {

    public Haruta() {
        setCamp(JJKCamp.FLEAUX);
    }

    @Override
    public String getName() {
        return "Haruta";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SHEARS);
    }

    @Override
    public void onSetup() {
    }


}
