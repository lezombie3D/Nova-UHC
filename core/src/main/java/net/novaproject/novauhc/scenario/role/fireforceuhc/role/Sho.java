package net.novaproject.novauhc.scenario.role.fireforceuhc.role;

import net.novaproject.novauhc.scenario.role.fireforceuhc.FireForceRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Random;

public class Sho extends FireForceRole {

    private double trans;
    private double force;
    private boolean istrans;
    private double maxforce;

    @Override
    public String getName() {
        return "Sho";
    }

    @Override
    public boolean hasFragment() {
        return true;
    }

    @Override
    public String getDescription() {
        return "test\n" + ChatColor.AQUA + getForceLevel() + ChatColor.GRAY + "\n" + ChatColor.AQUA + getTransLevel() + ChatColor.GRAY + "\n" + ChatColor.AQUA + getCamp();
    }



    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOD_SWORD);
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        trans = new Random().nextInt(100);

        force = 200;
        maxforce = 200;
        super.onGive(uhcPlayer);
    }


    @Override
    public double getTransLevel() {
        return trans;
    }

    @Override
    public void setTransLevel(double value) {
        this.trans = value;
    }

    @Override
    public double getForceLevel() {
        return force;
    }

    @Override
    public void setForceLevel(double value) {
        this.force = value;
    }

    @Override
    public double getMaxForce() {
        return maxforce;
    }

    @Override
    public void setMaxForce(int value) {
        this.maxforce = value;
    }

    @Override
    public void onFfCMD(UHCPlayer player1, String subCommand, String[] args) {

    }

    @Override
    public void onSec(Player p) {
        trans += 1;
        if (force < 201) {
            force += 1;
        }
        if (!istrans) {
            if (trans > 100) {
                trans = 101;
                istrans = true;
                setCamps("trans");
                p.sendMessage("Tu as été transformé !");
            }
        }

    }

}
