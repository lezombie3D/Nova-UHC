package net.novaproject.jjk.role.Zenin;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Jinichi extends JJKRole {

    public Jinichi() {
        setCamp(JJKCamp.ZENIN);
    }

    @Override
    public String getName() {
        return "Jinichi";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CHAINMAIL_CHESTPLATE);
    }

    @Override
    public void onSetup() {
    }


}
