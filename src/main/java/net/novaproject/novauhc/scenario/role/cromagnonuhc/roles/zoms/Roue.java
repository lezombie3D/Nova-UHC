package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Roue extends CromagnonRole {


    private PotionEffect[] effect;

    @Override
    public String getName() {
        return "Roue";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "La Roue§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possedez un effet de speed tant que vous avez 5 coeurs ou plus .\n" +
                "§fDescription du role : " + ChatColor.GREEN + "Vous êtes le Zom de base , aucun autre pouvoir particulier vous ai donné .\n" +
                "§8§m--------------------------";

    }

    @Override
    public String getCamps() {
        return "zoms";
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
    public void onSec(Player p) {
        if (p.getHealth() < 11) {
            effect = new PotionEffect[]{
                    new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false),
            };
        } else {
            effect = new PotionEffect[]{};
        }
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOAT);
    }

    @Override
    public PotionEffect[] getEffects() {
        return effect;
    }
}
