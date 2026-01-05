package net.novaproject.novauhc.scenario.role.scenario.loupgarouuhc;

import net.novaproject.novauhc.Common;
import net.novaproject.novauhc.scenario.role.Role;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;


public abstract class LoupGarouRole extends Role {


    public PotionEffect[] getEffects() {
        return new PotionEffect[0];
    }


    public PotionEffect[] getDayEffects() {
        return new PotionEffect[0];
    }

    public PotionEffect[] getNightEffects() {
        return new PotionEffect[0];
    }

    public boolean isNight() {
        long time =
                Common.get().getArena().getTime();
        return time >= 13000 && time < 23500;
    }

    public boolean isDay() {
        long time = Common.get().getArena().getTime();
        return time >= 23500 || time < 13000;
    }


    @Override
    public void onSec(Player p) {
        World world = Common.get().getArena();


        for (PotionEffect activeEffect : getEffects()) {
            p.removePotionEffect(activeEffect.getType());
        }
        for (PotionEffect effect : getEffects()) {
            effect.apply(p);
        }


        if (isNight()) {
            for (PotionEffect activeEffect : getNightEffects()) {
                p.removePotionEffect(activeEffect.getType());
            }
            for (PotionEffect effect : getNightEffects()) {
                effect.apply(p);
            }
        }
        if (isDay()) {
            for (PotionEffect activeEffect : getDayEffects()) {
                p.removePotionEffect(activeEffect.getType());
            }
            for (PotionEffect effect : getDayEffects()) {
                effect.apply(p);
            }
        }

    }

}
