package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LGCamps;
import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class Villageois extends LoupGarouRole {


    public Villageois() {
        setCamp(LGCamps.VILLAGE);
    }

    @Override
    public String getName() {
        return "§aVillageois";
    }

    @Override
    public String getDescription() {
        String message = "§8§m---------" + ChatColor.GREEN + "Villageois§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "Gagnez avec le Village\n" +
                "§fVos Pouvoir : " + ChatColor.BLUE + "Aucun\n" +
                "§fDescription du role : " + ChatColor.DARK_PURPLE + " Vous avez aucun pouvoir. Bonne chance et Bonne survie !\n" +
                "§8§m--------------------------";
        return message;
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EMERALD);
    }

}
