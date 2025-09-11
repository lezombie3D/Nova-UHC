package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.zoms;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonCamps;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Patriarche extends CromagnonRole {
    private UHCPlayer playerRole;
    private final List<Integer> list = new ArrayList<>();

    public Patriarche() {
        setCamp(CromagnonCamps.ZOMS);
    }

    @Override
    public String getName() {
        return "patriarche";
    }

    @Override
    public String getDescription() {
        return "§8§m---------" + ChatColor.GREEN + "Le Patriarche§8§m----------§r\n" +
                "§fVotre Objectif : " + ChatColor.GREEN + "est des gagner avec les autres Zoms.\n" +
                "§fVos Pouvoir : " + ChatColor.GOLD + "Vous possedez le pouvoir de ressusciter un joueur mort OU de vous ressusciter après un défaite.\n" +
                "§fDescription du role : " + ChatColor.GREEN + "vous bénéficiez aussi de 25 pourcents de chance d'avoir un cœur d'absorbation en plus pour chaque pomme d'or consommé .\n" +
                "§8§m--------------------------";
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.SKULL_ITEM);
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        list.clear();
        playerRole = uhcPlayer;
        list.add(1);
    }

    @Override
    public void onConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {
        if (item.getType() == Material.GOLDEN_APPLE) {
            Random random = new Random();
            if (random.nextDouble() < 0.25) {
                player.sendMessage(ChatColor.GOLD + "Vous avez reçu un bonus d'absorption !");
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 1, false, true));
            }
        }
    }
}
