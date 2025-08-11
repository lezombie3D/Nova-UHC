package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class Putride extends CromagnonRole {

    private final List<Integer> list = new ArrayList<>();
    private UHCPlayer playerRole;

    @Override
    public String getName() {
        return "Putride";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getCamps() {
        return "mamouth";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.RED;
    }

    @Override
    public List<Integer> getPowerUse() {
        return list;
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.COAL);
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);

        list.clear();
        playerRole = uhcPlayer;
        list.add(1);
    }

    @Override
    public PotionEffect[] getEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false)
        };
    }
}
