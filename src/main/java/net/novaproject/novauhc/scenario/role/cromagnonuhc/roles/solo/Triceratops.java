package net.novaproject.novauhc.scenario.role.cromagnonuhc.roles.solo;

import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonCamps;
import net.novaproject.novauhc.scenario.role.cromagnonuhc.CromagnonRole;
import net.novaproject.novauhc.uhcplayer.UHCPlayer;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Triceratops extends CromagnonRole {
    public Triceratops() {
        setCamp(CromagnonCamps.DINOS);
    }

    @Override
    public String getName() {
        return "Triceratops";
    }

    @Override
    public String getDescription() {
        return "";
    }


    @Override
    public ItemCreator getItem() {
        return new ItemCreator(Material.WOOD_SWORD);
    }

    @Override
    public void onGive(UHCPlayer uhcPlayer) {
        super.onGive(uhcPlayer);
        ItemCreator popo = new ItemCreator(Material.POTION).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60 * 20, 3, false, false), true);
        uhcPlayer.getPlayer().getInventory().addItem(popo.getItemstack());

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
                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 80, 0, false, false)
        };
    }
}
