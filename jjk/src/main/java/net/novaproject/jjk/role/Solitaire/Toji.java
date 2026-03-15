package net.novaproject.jjk.role.Solitaire;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Toji extends JJKRole {

    public Toji() {
        setCamp(JJKCamp.SOLITAIRE);
    }

    @Override
    public String getName() {
        return "Toji";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.STONE_SWORD);
    }

    @Override
    public void onSetup() {
    }


}
