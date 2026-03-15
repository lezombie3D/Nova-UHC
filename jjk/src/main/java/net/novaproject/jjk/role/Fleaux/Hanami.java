package net.novaproject.jjk.role.Fleaux;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Hanami extends JJKRole {

    public Hanami() {
        setCamp(JJKCamp.FLEAUX);
    }

    @Override
    public String getName() {
        return "Hanami";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SAPLING);
    }

    @Override
    public void onSetup() {
    }


}
