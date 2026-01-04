package net.novaproject.novauhc.scenario.role.loupgarouuhc.roles;

import net.novaproject.novauhc.scenario.role.loupgarouuhc.LGCamps;
import net.novaproject.novauhc.scenario.role.loupgarouuhc.LoupGarouRole;
import net.novaproject.novauhc.utils.ItemCreator;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Assasin extends LoupGarouRole {
    public Assasin() {
        setCamp(LGCamps.ASSASIN);
    }

    @Override
    public String getName() {
        return "Assasin";
    }

    @Override
    public String getDescription() {
        return "";
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
