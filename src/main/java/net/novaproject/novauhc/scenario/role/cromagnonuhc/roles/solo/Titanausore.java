package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.solo;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Titanausore extends CromagnonRole {
    @Override
    public String getName() {
        return "Titanausore";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getCamps() {
        return "solo3";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.GOLD;
    }

    @Override
    public List<Integer> getPowerUse() {
        return Collections.emptyList();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.YELLOW_FLOWER);
    }

    @Override
    public PotionEffect[] getNightEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.SPEED, 80, 0, false, false)
        };
    }

    @Override
    public PotionEffect[] getEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false)
        };
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);

    }
}
