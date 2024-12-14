package net.novaproject.novauhc.scenario.role.loupgarouuhc;

import net.novaproject.novauhc.scenario.role.Role;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public abstract class LoupGarouRole extends Role<LoupGarouRole> {

    public PotionEffect[] getEffects() {
        return new PotionEffect[0];
    }

    public PotionEffect[] getDayEffects() {
        return new PotionEffect[0];
    }

    public PotionEffect[] getNightEffects() {
        return new PotionEffect[0];
    }

    @Override
    public void onSec(Player p) {
        World world = Bukkit.getWorld("world");


        for (PotionEffect effect : getEffects()) {

            p.removePotionEffect(effect.getType());
            effect.apply(p);

        }


    }
}
