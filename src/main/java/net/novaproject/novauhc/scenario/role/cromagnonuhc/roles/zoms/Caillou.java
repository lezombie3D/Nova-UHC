package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Caillou extends CromagnonRole {
    private UHCPlayer playerRole;

    @Override
    public String getName() {
        return "Caillou";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "Le Caillou§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possedez un léger effet de getForce le jour.\n" +
                "§fDescription du role : " + ChatColor.GREEN + "Vous êtes un puissant guerrier de votre clan, votre role est de protéger les autres zoms .\n" +
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
    public PotionEffect[] getNightEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
        };
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        playerRole = uhcPlayer;
        super.onGive(uhcPlayer);

    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.STONE);
    }
}
