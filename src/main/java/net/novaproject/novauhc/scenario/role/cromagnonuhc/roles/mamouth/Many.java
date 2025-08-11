package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.mamouth;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.List;

public class Many extends CromagnonRole {
    @Override
    public String getName() {
        return "Many";
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
        return Collections.emptyList();
    }

    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.RECORD_7);
    }

    @Override
    public PotionEffect[] getEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false)
        };
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);

    }
}
