package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Renard extends LoupGarouRole {

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.FEATHER);
    }

    @Override
    public String getName() {
        return "§aRenard";
    }

    @Override
    public String getDescription() {
        String message = "§8§m---------" + ChatColor.GREEN + "Renard§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "Gagnez avec le Village\n" +
                "§fVos Pouvoir : " + ChatColor.BLUE + "Vous possedez le pouvoir de flairer, et l'effet Speed 1 de nuit.\n" +
                "§fDescription du role : " + ChatColor.DARK_PURPLE + " Vos Flaire sont \n" +
                "-Utilisable 3x par partie avec la commande /lg flairer.\n" +
                "-Vous pouvez flairer uniquement la nuit et dans un rayon de 20 block.\n" +
                "-Les joueur flairer seront prévenu le jour suivant.\n" +
                "-Vous devez rester proche de la cible 5min\n" +
                " -Vos flaire vous permettent de connaitre si le jouer appartient au camps des " + ChatColor.RED + "LoupGarou§r." +
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
        List<Integer> list = new ArrayList<>();
        list.add(3);
        return list;
    }

    @Override
    public PotionEffect[] getNightEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false),
        };
    }

}
