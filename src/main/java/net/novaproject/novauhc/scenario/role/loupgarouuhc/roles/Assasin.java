package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Assasin extends LoupGarouRole {
    @Override
    public String getName() {
        return "Assasin";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getCamps() {
        return "solo1";
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
        return new ItemCreator(Material.DIAMOND_SWORD);
    }

    @Override
    public PotionEffect[] getDayEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 80, 0, false, false),
        };
    }
}
