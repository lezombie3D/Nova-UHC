package net.novaproject.jjk.role.Fleaux;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Jogo extends JJKRole {

    public Jogo() {
        setCamp(JJKCamp.FLEAUX);
    }

    @Override
    public String getName() {
        return "Jogo";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BLAZE_POWDER);
    }

    @Override
    public void onSetup() {
    }


}
