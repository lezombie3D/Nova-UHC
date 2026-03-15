package net.novaproject.jjk.role.Exorcistes;

import net.novaproject.jjk.JJKCamp;
import net.novaproject.jjk.JJKRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Megumi extends JJKRole {

    public Megumi() {
        setCamp(JJKCamp.EXORCISTES);
    }

    @Override
    public String getName() {
        return "Megumi";
    }

    @Override
    public void sendDescription(Player player) {
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.INK_SACK);
    }

    @Override
    public void onSetup() {
    }


}
