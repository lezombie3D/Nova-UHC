package net.novaproject.jjk.role.Solitaire;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Sukuna extends JJKRole {

    public Sukuna() {
        setCamp(JJKCamp.SOLITAIRE);
    }

    @Override
    public String getName() {
        return "Sukuna";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SKULL_ITEM);
    }

    @Override
    public void onSetup() {
    }


}
