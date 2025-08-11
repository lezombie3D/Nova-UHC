package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class LoupGarou extends LoupGarouRole {

    public LoupGarou() {
        super();
    }

    @Override
    public String getName() {
        return "§4Loup Garou";
    }

    @Override
    public String getCamps() {
        return "loup";
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
    public ChatColor getColor() {
        return ChatColor.RED;
    }

    @Override
    public List<Integer> getPowerUse() {
        return Collections.emptyList();
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
