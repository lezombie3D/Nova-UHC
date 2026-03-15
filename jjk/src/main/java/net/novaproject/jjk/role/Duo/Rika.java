package net.novaproject.jjk.role.Duo;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Rika extends JJKRole {

    public Rika() {
        setCamp(JJKCamp.DUO);
    }

    @Override
    public String getName() {
        return "Rika";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.NETHER_STAR);
    }

    @Override
    public void onSetup() {
    }


}
