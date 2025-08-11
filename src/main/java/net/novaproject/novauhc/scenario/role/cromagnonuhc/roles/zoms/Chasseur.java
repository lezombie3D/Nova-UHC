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

public class Chasseur extends CromagnonRole {
    @Override
    public String getName() {
        return "chasseur";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "Le Chasseur§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous pouvez renifler une personne toute les 20 minutes si vous avez passé un certain temps avec celle-ci, cette action vous permettra de savoir si cette personne est votre ennemi ou non ( les roles solitaire be sont pas considérés comme des ennemis par votre flair).\n" +
                "§fDescription du role : " + ChatColor.GREEN + "Vous êtes un Zoms de base sans autres particularitées.\n" +

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
    public ItemCreator getItem() {
        return new ItemCreator(Material.BOW);
    }

    @Override
    public PotionEffect[] getEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
        };
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);

    }
}
