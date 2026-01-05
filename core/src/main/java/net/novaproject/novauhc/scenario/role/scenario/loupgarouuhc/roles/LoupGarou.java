package net.novaproject.novauhc.scenario.role.scenario.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.scenario.loupgarouuhc.LGCamps;
import net.novaproject.novauhc.scenario.role.scenario.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LoupGarou extends LoupGarouRole {


    public LoupGarou() {
        setCamp(LGCamps.LOUP_GAROU);
    }

    @Override
    public String getName() {
        return "§4Loup Garou";
    }


    @Override
    public String getDescription() {
        String message = "§8§m---------" + ChatColor.RED + "Loup-Garouf§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.RED + "Gagnez avec les Loup-Garou\n" +
                "§fVos Pouvoir : " + ChatColor.BLUE + "Vous possédez Force 1 de nuit.\n" +
                "§fDescription du role : " + ChatColor.DARK_PURPLE + "Vous possédez la liste de vos coéquipier.\n" +
                "§8§m--------------------------";
        return message;
    }



    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.BONE);
    }

    @Override
    public PotionEffect[] getNightEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, 0, false, false),
        };
    }
}
