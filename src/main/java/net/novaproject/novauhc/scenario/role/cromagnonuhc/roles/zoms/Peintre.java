package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;


public class Peintre extends CromagnonRole {
    private UHCPlayer playerRole;
    private final List<Integer> list = new ArrayList<>();

    @Override
    public String getName() {

        return "Peintre";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "Le Peintre§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + " Une fois dans la partie, vous pourrez connaître le role de deux autres joueurs de votre choix, si l'un deux est mammouth alors vous gagnerez un effet de résistance.\n" +
                "§fDescription du role : " + ChatColor.GREEN + "Vous êtes un Zoms de base sans autres particularitées .\n" +

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
        return list;
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        list.clear();
        playerRole = uhcPlayer;
        list.add(1);

    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOL).setDurability((short) 7);
    }
}
