package net.novaproject.jjk.role.Duo;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Yuta extends JJKRole {



    public Yuta() {
        setCamp(JJKCamp.DUO);
    }

    @Override
    public String getName() {
        return "Yuta";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.DIAMOND_SWORD);
    }

    @Override
    public void onSetup() {
    }


}
