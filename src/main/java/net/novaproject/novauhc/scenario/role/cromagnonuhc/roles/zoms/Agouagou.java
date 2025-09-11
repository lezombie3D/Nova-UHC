package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonCamps;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class Agouagou extends CromagnonRole {
    public Agouagou() {
        setCamp(CromagnonCamps.ZOMS);
    }

    @Override
    public String getName() {

        return "Agouagou";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "Agou-Agou§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GREEN + "Vous possedez aucun effet.\n" +
                "§fDescription du role : " + ChatColor.GREEN + "Vous êtes le Zom de base , aucun pouvoir particulier vous ai donné .\n" +

                "§8§m--------------------------";
    }



    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.CAKE);
    }
}
