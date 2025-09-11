package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonCamps;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GagGag extends CromagnonRole {


    public GagGag() {
        setCamp(CromagnonCamps.ZOMS);
    }

    @Override
    public String getName() {
        return "GagGag";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.RED + "Gag-Gag§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.RED + "est des gagner avec les mammouths.\n" +
                "§fVos Pouvoir : " + ChatColor.RED+ "Vous possedez un effet de getForce durant la nuit.\n" +
                "§fDescription du role : "+ ChatColor.RED +"Vous êtes le Mammouth de base , aucun pouvoir particulier vous ai donné .\n"+
                "§8§m--------------------------";

    }



    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.REDSTONE);
    }


    @Override
    public PotionEffect[] getNightEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false)
        };
    }


    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);

    }
}
