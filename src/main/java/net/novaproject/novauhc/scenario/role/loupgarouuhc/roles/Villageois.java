package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

public class Villageois extends LoupGarouRole {

    public Villageois() {
        super();
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
    public String getCamps() {
        return "village";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GREEN;
    }

    @Override
    public List<Integer> getPowerUse() {
        return Collections.emptyList();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.EMERALD);
    }

}
